package cc.unitmesh.importer

import cc.unitmesh.importer.processor.KotlinParserWrapper
import cc.unitmesh.importer.processor.KotlinCodeProcessor
import cc.unitmesh.importer.processor.allMethods
import cc.unitmesh.importer.model.CodeSnippet
import cc.unitmesh.importer.model.RawDump
import cc.unitmesh.importer.processor.classToConstructorText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import java.io.File


object Snippets {
    private fun promptForOpenAI(code: String): String {
        return """用户故事来描述如下的代码。要求：1. 只返回一句话 2. 突出重点 3. 不使用技术术语

###
$code
###
""".trimMargin()
    }

    fun fromFile(outputFile: File): MutableList<CodeSnippet> {
        val results: MutableList<CodeSnippet> = mutableListOf();
        val dumpList = Json.decodeFromString<List<RawDump>>(outputFile.readText())
        dumpList.forEach { rawDump ->
            val snippet: KotlinParserWrapper
            try {
                snippet = KotlinParserWrapper.createUnitContext(rawDump.toCode())
            } catch (e: KtLintParseException) {
                return@forEach
            }

            val processor = KotlinCodeProcessor(snippet.rootNode, rawDump.content)
            val imports = processor.allImports()

            processor.allClassNodes().forEach { classNode ->
                val packageName = processor.packageName()
                val className = processor.className(classNode)

                val classes = processor.splitClassMethodsToManyClass(classNode)

                classes.map { clazz ->
                    clazz.allMethods().forEach { method ->
                        val content = method.text
                        val size = method.text.length

                        if (size > 2048) {
                            logger.info("size too large: ${rawDump.path}")
                            return@map null
                        }

                        val requiredType = processor.methodRequiredTypes(method, imports)

                        results.add(
                            CodeSnippet(
                                identifierName = """$packageName.$className""",
                                content = content,
                                path = rawDump.path + "#" + method.text.hashCode(),
                                size = size,
                                imports = imports,
                                requiredType = requiredType,
                            )
                        )
                    }

                }
            }
        }

        return results
    }

    fun toLLMPrompts(
        typesDump: List<RawDump>,
        snippets: List<CodeSnippet>
    ): List<SnippetPrompt> {
        val typeMapByIdentifier: MutableMap<String, RawDump> = mutableMapOf()
        typesDump.forEach { type ->
            val createUnitContext = KotlinParserWrapper.createUnitContext(type.toCode())
            val processor = KotlinCodeProcessor(createUnitContext.rootNode, type.content)
            processor.allClassNodes().forEach { classNode ->
                val packageName = processor.packageName()
                val className = processor.className(classNode)
                val identifierName = """$packageName.$className"""
                typeMapByIdentifier[identifierName] = type
            }
        }

        val prompts = snippets.mapIndexed { index, snippet ->
            val typeStrings = snippet.requiredType.mapNotNull { type ->
                typeMapByIdentifier[type]
            }.map {
                val createUnitContext = KotlinParserWrapper.createUnitContext(it.toCode())
                val processor = KotlinCodeProcessor(createUnitContext.rootNode, it.content)
                processor.allClassNodes()[0].classToConstructorText()
            }

            SnippetPrompt(
                id = index,
                identifierName = snippet.identifierName,
                requiredType = typeStrings,
                content = snippet.content,
                prompt = "",
            )
        }

        return prompts
    }

    fun toOpenAIPrompts(
        snippets: List<CodeSnippet>
    ): List<SnippetPrompt> {
        val typeMapByIdentifier: MutableMap<String, RawDump> = mutableMapOf()

        return snippets.mapIndexed { index, snippet ->
            snippet.requiredType.mapNotNull { type ->
                typeMapByIdentifier[type]
            }.map {
                val createUnitContext = KotlinParserWrapper.createUnitContext(it.toCode())
                val processor = KotlinCodeProcessor(createUnitContext.rootNode, it.content)
                processor.allClassNodes()[0].classToConstructorText()
            }

            val prompt = promptForOpenAI(snippet.content)

            SnippetPrompt(
                id = index,
                identifierName = snippet.identifierName,
                requiredType = snippet.requiredType,
                content = snippet.content,
                prompt = prompt,
            )
        }
    }

}

