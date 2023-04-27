package cc.unitmesh.processor.api

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.io.read
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = UnitConnector()
//    .subcommands(Prompting())
    .main(args)

val logger: Logger = LoggerFactory.getLogger(UnitConnector::class.java)

class UnitConnector : CliktCommand() {
    private val source by argument().file().help("Source CSV file").default(File("source.csv"))
    private val prompt by argument().file().default(File("prompt.txt"))
    private val outputDir by argument().file().default(File("output"))

    override fun run() {
        logger.info("Unit Connector Started")
        val frame = DataFrame.read(source.absolutePath)
        val items = frame.rows().toList()

        val columnNames = frame.columnNames()

        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()

        val promptDir = File(outputDir.absolutePath, "prepare")
        promptDir.mkdirs()
        val promptText = prompt.readText()
        items.forEachIndexed { index, row ->
            var newPrompt = ""
            columnNames.forEach { columnName ->
                val value = row[columnName]
                newPrompt = promptText.replace("{$columnName}", value.toString())
            }

            val outputFile = File(promptDir, "prompt-$index.txt")
            outputFile.writeText(newPrompt)
        }
    }
}
