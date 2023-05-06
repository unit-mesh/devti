package cc.unitmesh.processor.api.command

import cc.unitmesh.core.Instruction
import cc.unitmesh.core.prompter.OpenAiPrompter
import cc.unitmesh.processor.api.ApiProcessorDetector
import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.render.MarkdownTableRender
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

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

        // prompt with new
        val domainJsonDir = File(outputDir.absolutePath, "domain-json")
        domainJsonDir.mkdirs()

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
                        if (it.items.size < 2) {
                            return@forEachIndexed
                        }

                        val collection = it
                        collection.items.forEach { item ->
                            item.description = ""
                            // if item.body length > 512, we remove it
                            if ((item.request?.bodyString?.length ?: 0) > 512) {
                                item.request!!.bodyString = ""
                            }
                        }

                        val domainFile = "$domain-${file.nameWithoutExtension.trim()}-$subIndex.puml"
                        val outputFile = File(domainDir, domainFile)

                        val instructFile = File(domainJsonDir, outputFile.name.replace("puml", "json"))
                        // we use puml check because it's faster
                        if (outputFile.exists()) {
                            // read instruction from file
                            val instruction = Json.decodeFromString<Instruction>(instructFile.readText())
                            instructions += instruction
                            return@forEachIndexed
                        }

                        val single = render.render(listOf(collection))
                        if (single.length < MIN_OUTPUT_LENGTH) {
                            logger.debug("Skip ${file.absolutePath} because it's too short")
                            return@forEachIndexed
                        }

                        if (single.length >= maxLength) {
                            // reduce collection.items to 8
                            val eightCollection = collection.copy(items = collection.items.take(8))
                            val eightString = render.render(listOf(eightCollection))
                            if (eightString.length > maxLength) {
                                // reduce collection.items to 4
                                val fourCollection = collection.copy(items = collection.items.take(4))
                                val fourItemStr = render.render(listOf(fourCollection))
                                if (fourItemStr.length > maxLength) {
                                    logger.debug(
                                        "Try reduce and reduce items but more than 4092, Skip ${file.absolutePath} - ${it.name} because it's too short",
                                    )
                                    return@forEachIndexed
                                }

                                logger.debug(
                                    "Try reduce items but more than 4092, Skip ${file.absolutePath} - ${it.name} because it's too short",
                                )
                                return@forEachIndexed
                            }
                        }

                        val newPrompt = promptText.replace("{code}", single)

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

                        val instruction = Instruction(
                            instruction = promptText,
                            input = single,
                            output = output,
                        )
                        instructions += instruction
                        logger.debug("output to json: ${instructFile.absolutePath}")
                        instructFile.writeText(Json.encodeToString(instruction))
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
