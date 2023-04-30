package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.base.Instruction
import cc.unitmesh.processor.api.render.MarkdownTableRender
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
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random

fun main(args: Array<String>) = Generating()
//    .subcommands(Prompting())
    .main(args)

val logger: Logger = LoggerFactory.getLogger(UnitApi::class.java)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

class Generating : CliktCommand() {
    private val inputDir by argument().file().help("Input directory").default(File("input"))
    override fun run() {
        val outputDIr = File("output", "markdown")
        if (!outputDIr.exists()) {
            outputDIr.mkdirs()
        }

        inputDir.walk().forEach { file ->
            if (file.isFile) {
                var processor: ApiProcessor? = null
                try {
                    processor = ApiProcessorDetector.detectApiProcessor(file)
                } catch (e: Exception) {
                    logger.info("Failed to parse ${file.absolutePath}", e)
                }

                if (processor == null) {
                    return@forEach
                }

                // get file parent name
                val parentName = file.parentFile.name

                try {
                    val instructions = processor.convertApi()
                    val output = MarkdownTableRender().render(instructions)

                    val outputFile = File(outputDIr, "$parentName-${file.nameWithoutExtension}.md")
                    val maybeAGoodApi = 256
                    if (output.length > maybeAGoodApi) {
                        outputFile.writeText(output)
                    }
                    outputFile.writeText(output)
                } catch (e: Exception) {
                    logger.error("Failed to parse ${file.absolutePath}", e)
                }
            }
        }
    }
}

class Prompting : CliktCommand() {
    private val source by argument().file().help("Source CSV file").default(File("source.csv"))
    private val prompt by argument().file().default(File("prompt.txt"))
    private val prompt2 by argument().file().default(File("prompt2.txt"))
    private val domain by argument().file().default(File("domains.csv"))
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
                val outputFile = outputMarkdown(markdownApiOutputDir, index, bank, it)

                if (outputFile.exists()) {
                    return@forEach
                }

                try {
                    val output = prompter.prompt(newPrompt)
                    outputFile.writeText(output)
                } catch (e: Exception) {
                    Thread.sleep(1000)
                    logger.error("Error sleeping", e)
                }
            }
        }

        val serviceNameMap = mutableMapOf<String, Boolean>()
        // prompt to jsonl
        val jsonlApiFile = File(outputDir.absolutePath, "apis.jsonl")
        val serviceMap: MutableMap<String, String> = mutableMapOf()
        val instructions: MutableList<Instruction> = mutableListOf()

        val domainTranslation = mutableMapOf<String, String>()
        if (domain.exists()) {
            val domainFrame = DataFrame.readCSV(domain.absolutePath)
            domainFrame.rows().forEach { row ->
                val values = row.values() as List<String>
                domainTranslation[values[0]] = values[1] + "服务"
            }
        }

        banks.forEachIndexed { index, bank ->
            bank.openApiService.forEach { service ->
                val outputFile = outputMarkdown(markdownApiOutputDir, index, bank, bank.openApiService.first())
                val output = outputFile.readText()

                var serviceName = service.name
                    .replace(" Services", "")
                    .replace(" Service", "")
                    .replace(" API", "")
                    .replace("API", "")
                    .replace("服务", "")

                serviceNameMap[serviceName] = true

                if (domainTranslation.containsKey(serviceName)) {
                    serviceName = domainTranslation[serviceName]!!
                }

                val instruction = "帮我设计一个银行的 API:"
                val input = serviceName

                serviceMap[serviceName] = output

                instructions += Instruction(instruction = instruction, input = input, output = output)
            }
        }

        createServiceNameMap(serviceNameMap)

        // repeat 500 time, to randomize take 3~5 items from serviceMap
        repeat(500) {
            val serviceNames = serviceMap.keys.toList().shuffled().take(Random.nextInt(3, 5))
            // 帮我设计一组 API，需要包含：{serviceName}、{serviceName}、{serviceName}
            val instruction = "帮我设计一组银行的 API，需要包含："
            val input = serviceNames.joinToString(separator = "、")
            val output = serviceNames.joinToString(separator = "\n") { serviceMap[it]!! }
            instructions += Instruction(instruction = instruction, input = input, output = output)
        }

        jsonlApiFile.writeText("")
        instructions.forEach {
            jsonlApiFile.appendText(Json { isLenient = true }.encodeToString(it))
            jsonlApiFile.appendText("\n")
        }
    }

    private fun createServiceNameMap(serviceNameMap: MutableMap<String, Boolean>) {
        // write serviceNameMap to csv file
        val serviceNameMapFile = File(outputDir.absolutePath, "serviceNameMap.csv")
        serviceNameMapFile.writeText("")
        serviceNameMap.forEach { (serviceName, _) ->
            serviceNameMapFile.appendText(serviceName)
            serviceNameMapFile.appendText("\n")
        }
    }
}

private fun outputMarkdown(
    markdownApiOutputDir: File,
    index: Int,
    bank: Bank,
    service: OpenApiService
) = File(markdownApiOutputDir, "$index-${bank.name}-${service.name}.md")

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
