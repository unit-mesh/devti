package cc.unitmesh.importer

import cc.unitmesh.importer.filter.CodeSnippetContext
import cc.unitmesh.importer.filter.KotlinCodeProcessor
import cc.unitmesh.importer.model.RawDump
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        val jsonFiles = File("datasets" + File.separator + "rawdump").walkTopDown().filter { file ->
            file.name.endsWith(".json")
        }.toList()

        val codes: List<RawDump> = jsonFiles.flatMap(File::readLines).map(Json.Default::decodeFromString)

        var count = 0
        codes.map { code ->
            val snippet: CodeSnippetContext
            try {
                snippet = CodeSnippetContext.createUnitContext(code.toCode())
            } catch (e: KtLintParseException) {
                return@map
            }

            val processor = KotlinCodeProcessor(snippet.rootNode, code.content)
            processor.getMethodByAnnotationName("Query").map {
                count++
            }
        }

        println(count)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    }
}