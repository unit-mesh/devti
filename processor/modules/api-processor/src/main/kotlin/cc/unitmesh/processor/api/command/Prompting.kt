package cc.unitmesh.processor.api.command

import cc.unitmesh.core.Instruction
import cc.unitmesh.core.prompter.OpenAiPrompter
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.io.read
import java.io.File

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