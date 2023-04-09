package cc.unitmesh.importer

import cc.unitmesh.importer.filter.CodeSnippetContext
import cc.unitmesh.importer.filter.KotlinCodeProcessor
import cc.unitmesh.importer.model.RawDump
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeJson
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

        val outputs = codes.filter { code ->
            val snippet: CodeSnippetContext
            try {
                snippet = CodeSnippetContext.createUnitContext(code.toCode())
            } catch (e: KtLintParseException) {
                return@filter false
            }

            val processor = KotlinCodeProcessor(snippet.rootNode, code.content)
            // should contains @Query @Delete @Insert ....
            processor.getMethodByAnnotationName("Query").isNotEmpty()
        }

        val dataFrame = outputs.toDataFrame()
        dataFrame.writeJson("datasets" + File.separator + "filtered.json")

//        val outputFile = File("datasets" + File.separator + "filtered.json")
//        outputFile.writeText(Json.Default.encodeToString(outputs))
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    }
}