package cc.unitmesh.processor.api

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
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
    private val prompt2 by argument().file().default(File("prompt2.txt"))
    private val outputDir by argument().file().default(File("output"))

    override fun run() {
        logger.info("loading dotenv")
        val dotenv = Dotenv.load()
        val proxy = dotenv.get("OPEN_AI_PROXY")
        val key = dotenv.get("OPEN_AI_KEY")

        logger.info("key: $key, proxy: $proxy")

        logger.info("Unit Connector Started")
        val frame = DataFrame.read(source.absolutePath)
        val items = frame.rows().toList()

        val columnNames = frame.columnNames()

//        if (outputDir.exists()) {
//            outputDir.deleteRecursively()
//        }
        outputDir.mkdirs()

        val promptDir = File(outputDir.absolutePath, "prepare")
        promptDir.mkdirs()
        val promptText = prompt.readText()
        val actualPrompts = mutableListOf<String>()
        items.forEachIndexed { index, row ->
            var newPrompt = ""
            columnNames.forEach { columnName ->
                val value = row[columnName]
                newPrompt = promptText.replace("{$columnName}", value.toString())
            }

            actualPrompts.add(newPrompt)
            val outputFile = File(promptDir, "prompt-$index.txt")
            outputFile.writeText(newPrompt)
        }

        val prompter = OpenAiProxyPrompter(key, proxy)
        val prompterOutputDir = File(outputDir.absolutePath, "prompter")
        prompterOutputDir.mkdirs()

        actualPrompts.forEachIndexed { index, prompt ->
            val outputFile = File(prompterOutputDir, "output-$index.txt")
            if (outputFile.exists()) {
                return@forEachIndexed
            }

            logger.info("Prompting $index")
            try {
                val output = prompter.prompt(prompt)
                outputFile.writeText(output)
            } catch (e: Exception) {
                Thread.sleep(1000)
                logger.error("Error sleeping", e)
            }
        }

        // walkdir prompterOutputDir and merge to jsonl
        val jsonlFile = File(outputDir.absolutePath, "prompter.jsonl")
        jsonlFile.writeText("")
        val banks = mutableListOf<Bank>()
        prompterOutputDir.walk().forEach { file ->
            if (file.isFile) {
                val text = file.readText()
                try {
                    val output = Json.decodeFromString<Bank>(text)
                    banks.add(output)
                    jsonlFile.appendText(Json { isLenient = true }.encodeToString(output))
                    jsonlFile.appendText("\n")
                } catch (e: Exception) {
                    logger.error("Error parsing $file", e)
                    throw e
                }
            }
        }

        // prompt with new
        val markdownApiOutputDir = File(outputDir.absolutePath, "apis")
        markdownApiOutputDir.mkdirs()

        val prompt2 = prompt2.readText()
        banks.forEachIndexed { index, bank ->
            bank.openApiService.forEach {
                logger.info("Prompting Step 2 ${bank.name} ${it.name}")
                // replace {bankName} with bank.name, replace {serviceName} with service.name, replace {serviceDescription} with service.description
                val newPrompt = prompt2
                    .replace("{bankName}", bank.name)
                    .replace("{serviceName}", it.name)
                    .replace("{serviceDescription}", it.description)

                try {
                    val output = prompter.prompt(newPrompt)
                    val outputFile = File(markdownApiOutputDir, "$index-${bank.name}-${it.name}.md")
                    outputFile.writeText(output)
                } catch (e: Exception) {
                    Thread.sleep(1000)
                    logger.error("Error sleeping", e)
                }
            }
        }
    }
}

@Serializable
class Bank(
    val name: String,
    val fullName: String,
    val description: String,
    val openApiService: List<OpenApiService>,
    val otherService: List<OtherService>? = null,
    val bankType: String
)

@Serializable
class OpenApiService(
    val name: String,
    val description: String
)

@Serializable
class OtherService(
    val name: String,
    val description: String
)
