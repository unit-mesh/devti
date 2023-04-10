package cc.unitmesh.importer

import cc.unitmesh.importer.processor.CodeSnippetContext
import cc.unitmesh.importer.processor.KotlinCodeProcessor
import cc.unitmesh.importer.processor.allMethods
import cc.unitmesh.importer.model.CodeSnippet
import cc.unitmesh.importer.model.PackageUtil
import cc.unitmesh.importer.model.RawDump
import cc.unitmesh.importer.processor.classToConstructorText
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import java.io.File

fun snippetsFromFile(outputFile: File): MutableList<CodeSnippet> {
    val results: MutableList<CodeSnippet> = mutableListOf();
    val dumpList = Json.decodeFromString<List<RawDump>>(outputFile.readText())
    dumpList.forEach { rawDump ->
        val snippet: CodeSnippetContext
        try {
            snippet = CodeSnippetContext.createUnitContext(rawDump.toCode())
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

fun promptText(code: String, types: List<String>): String {
    return """请编写用户故事，能覆盖下面的代码功能，要求：1. 突出重点 2. 你返回的内容只有： 我想 xxx。

|###
${types.joinToString("\n")}
${code}
###
        """.trimMargin()
}

fun snippetToPrompts(
    typesDump: List<RawDump>,
    snippets: List<CodeSnippet>
): List<String> {
    val typeMapByIdentifier: MutableMap<String, RawDump> = mutableMapOf()
    typesDump.forEach { type ->
        val createUnitContext = CodeSnippetContext.createUnitContext(type.toCode())
        val processor = KotlinCodeProcessor(createUnitContext.rootNode, type.content)
        processor.allClassNodes().forEach { classNode ->
            val packageName = processor.packageName()
            val className = processor.className(classNode)
            val identifierName = """$packageName.$className"""
            typeMapByIdentifier[identifierName] = type
        }
    }

    val prompts = snippets.flatMap { snippet ->
        snippet.requiredType.mapNotNull { type ->
            typeMapByIdentifier[type]
        }.map {
            val createUnitContext = CodeSnippetContext.createUnitContext(it.toCode())
            val processor = KotlinCodeProcessor(createUnitContext.rootNode, it.content)
            processor.allClassNodes()[0].classToConstructorText()
        }
    }

    return prompts
}
