package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.core.prompter.OpenAiPrompter
import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.render.MarkdownTableRender
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
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

fun main(args: Array<String>) = Modeling()
//    .subcommands(Prompting())
    .main(args)

val logger: Logger = LoggerFactory.getLogger(UnitApi::class.java)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

val GROUP_API_INSTRUCTION = "帮我设计一组 API："
val ONE_API_INSTRUCTION = "帮我设计一个银行的 API:"

fun randomInstruction(serviceName: String): String {
    val instructions = listOf(
        "展示${serviceName}的 API 应该如何设计？",
        "如何实现${serviceName}功能的 API？",
        "设计一个可以查询${serviceName}的 API。",
        "如何设计一个支持${serviceName}的 API？",
        "你会如何设计一个可以处理${serviceName}的 API？",
        "设计一个可以查询${serviceName}的 API。",
        "如何设计一个可以处理${serviceName}的 API？",
        "如何设计一个可以处理${serviceName}的 API？",
        "如何实现${serviceName}的 API？",
        "设计一个可以${serviceName}的 API。",
    )

    val random = Random.nextInt(0, instructions.size)
    return instructions[random]
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

        val prompter = OpenAiPrompter(key, proxy)
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

        // walk-dir prompterOutputDir and merge to jsonl
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

        // prompt to jsonl
        val jsonlApiFile = File(outputDir.absolutePath, "apis.jsonl")
        val serviceMap: MutableMap<String, String> = mutableMapOf()
        val instructions: MutableList<Instruction> = mutableListOf()

        val serviceNameMap = mutableMapOf<String, Boolean>()
        val domainTranslation = getDomainTranslate(domain)

        banks.forEachIndexed { index, bank ->
            bank.openApiService.forEach { service ->
                val outputFile = outputMarkdown(markdownApiOutputDir, index, bank, bank.openApiService.first())
                val output = outputFile.readText()

                val serviceName = translateName(domainTranslation, service.name)

                serviceNameMap[serviceName] = true

                serviceMap[serviceName] = output

                instructions += Instruction(instruction = ONE_API_INSTRUCTION, input = serviceName, output = output)
            }
        }

        createServiceNameMap(serviceNameMap)

        // repeat 1000 time, to randomize take 3~5 items from serviceMap
//        repeat(1000) {
//            val serviceNames = serviceMap.keys.toList().shuffled().take(Random.nextInt(3, 5))
//            val instruction = GROUP_API_INSTRUCTION
//            val input = serviceNames.joinToString(separator = "、")
//            val output = serviceNames.joinToString(separator = "\n") { serviceMap[it]!! }
//            instructions += Instruction(instruction = instruction, input = input, output = output)
//        }

        jsonlApiFile.writeText("")
        instructions.forEach {
            jsonlApiFile.appendText(Json { isLenient = true }.encodeToString(it))
            jsonlApiFile.appendText("\n")
        }
    }

    private fun translateName(
        domainTranslation: MutableMap<String, String>,
        name: String,
    ): String {
        var serviceName = name.simplifyApi()

        if (domainTranslation.containsKey(serviceName)) {
            serviceName = domainTranslation[serviceName]!!
        }
        return serviceName
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

class Modeling : CliktCommand() {
    private val prompt by argument().file().default(File("domain-prompt.txt"))
    private val inputDir by argument().file().help("Input directory").default(File("input"))
    private val outputDir by argument().file().default(File("output"))
    private val maxLength by argument().int().default(4092)

    override fun run() {
        logger.debug("loading dotenv")
        val dotenv = Dotenv.load()
        val proxy = dotenv.get("OPEN_AI_PROXY")
        val key = dotenv.get("OPEN_AI_KEY")

        logger.debug("key: $key, proxy: $proxy")

        val outputDir = File("output")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val promptText = prompt.readText()
        val instructions: MutableList<Instruction> = mutableListOf()

        // prompt with new
        val domainDir = File(outputDir.absolutePath, "domain")
        domainDir.mkdirs()

        val prompter = OpenAiPrompter(key, proxy)

        inputDir.walk().forEachIndexed { index, file ->
            if (file.isFile) {
                var processor: ApiProcessor? = null
                try {
                    processor = ApiProcessorDetector.detectApiProcessor(file, withPostman = true)
                } catch (e: Exception) {
                    logger.info("Failed to parse ${file.absolutePath}", e)
                }

                if (processor == null) {
                    return@forEachIndexed
                }

                try {
                    val parentDir = file.parentFile
                    val domain = parentDir.name

                    val collections = processor.convertApi()
                    val render = MarkdownTableRender()


                    collections.forEachIndexed { subIndex, it ->
                        val collection = it
                        collection.items.forEach { item ->
                            item.description = ""
                        }

                        // file.name without extension
                        val domainFile = "$domain-${file.nameWithoutExtension.trim()}-$subIndex.puml"
                        val outputFile = File(domainDir, domainFile)

                        if (outputFile.exists()) {
                            return@forEachIndexed
                        }

                        val single = render.render(listOf(collection))
                        if (single.length < 128 || single.length >= maxLength) {
                            logger.debug("Skip ${file.absolutePath} because it's too short")
                            return@forEachIndexed
                        }

                        val newPrompt = promptText.replace("{code}", single)

                        logger.info("newPrompt: ${newPrompt}")

                        var output = ""
                        try {
                            logger.debug("start prompt to: ${outputFile.absolutePath}")
                            output = prompter.prompt(newPrompt)
                            // if output starsWith ```uml and endsWith ```, remove it
                            if (output.startsWith("```uml") && output.endsWith("```")) {
                                output = output.substring(6, output.length - 3)
                            }

                            outputFile.writeText(output)
                        } catch (e: Exception) {
                            Thread.sleep(1000)
                            logger.debug("Error sleeping", e)
                        }

                        if (output.isEmpty()) {
                            return@forEachIndexed
                        }

                        instructions += Instruction(
                            instruction = newPrompt,
                            input = newPrompt,
                            output = output,
                        )
                    }
                } catch (e: Exception) {
                    logger.error("Failed to parse ${file.absolutePath}", e)
                }
            }
        }

        // write to jsonl
        val jsonl = File(this.outputDir, "domains-instructions.jsonl")
        jsonl.writeText(instructions.joinToString("\n") { Json.encodeToString(it) })
    }
}

private fun String.simplifyApi(): String = this
    .replace(" Services", "")
    .replace(" Service", "")
    .replace(" API", "")
    .replace("API", "")
    .replace("服务", "")

fun getDomainTranslate(domainFile: File): MutableMap<String, String> {
    val domainTranslation = mutableMapOf<String, String>()
    if (domainFile.exists()) {
        val englishToChinese = DataFrame.readCSV(domainFile.absolutePath)
        englishToChinese.rows().forEach { row ->
            val values = row.values() as List<String>
            val english = values[0]
            domainTranslation[english] = values[1] + "($english)" + "服务"
        }
    }

    return domainTranslation
}

private fun outputMarkdown(
    markdownApiOutputDir: File,
    index: Int,
    bank: Bank,
    service: OpenApiService,
) = File(markdownApiOutputDir, "$index-${bank.name}-${service.name}.md")

@Serializable
class Bank(
    val name: String,
    val fullName: String,
    val description: String,
    val openApiService: List<OpenApiService>,
    val otherService: List<OtherService>? = null,
    val bankType: String,
)

@Serializable
class OpenApiService(
    val name: String,
    val description: String,
)

@Serializable
class OtherService(
    val name: String,
    val description: String,
)
