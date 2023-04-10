package cc.unitmesh.importer

import cc.unitmesh.importer.filter.CodeSnippetContext
import cc.unitmesh.importer.filter.KotlinCodeProcessor
import cc.unitmesh.importer.filter.allMethods
import cc.unitmesh.importer.model.CodeSnippet
import cc.unitmesh.importer.model.RawDump
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