package cc.unitmesh.processor.api.command

import cc.unitmesh.core.Instruction
import cc.unitmesh.core.model.ApiCollection
import cc.unitmesh.processor.api.ApiProcessorDetector
import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.render.MarkdownTableRender
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.arguments.help
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

class Generating : CliktCommand() {
    private val inputDir by argument().file().help("Input directory").default(File("input"))
    private val domain by argument().file().default(File("domains.csv"))
    private val outputDir by argument().file().default(File("output"))
    private val maxLength by argument().int().default(4092)

    override fun run() {
        val outputDir = File("output", "markdown")
        if (!outputDir.exists()) {
            outputDir.mkdirs()
        }

        val instructions: MutableList<Instruction> = mutableListOf()

        val translation = getDomainTranslate(domain)

        val apiNames = mutableSetOf<String>()

        inputDir.walk().forEach { file ->
            if (file.isFile) {
                var processor: ApiProcessor? = null
                try {
                    processor = ApiProcessorDetector.detectApiProcessor(file, withPostman = true)
                } catch (e: Exception) {
                    logger.info("Failed to parse ${file.absolutePath}", e)
                }

                if (processor == null) {
                    return@forEach
                }
                val parentName = file.parentFile.name

                try {
                    val collections = processor.convertApi()
                    val render = MarkdownTableRender()

                    // create for each api
                    collections.forEach {
                        val single = render.render(listOf(it))
                        if (single.length < 128 || single.length >= maxLength) {
                            logger.info("Skip ${file.absolutePath} because it's too short")
                            return@forEach
                        }

                        val serviceName = domainName(translation, it)
                        apiNames += serviceName

                        instructions += Instruction(
                            instruction = ONE_API_INSTRUCTION,
                            input = serviceName,
                            output = single,
                        )

                        instructions += Instruction(
                            instruction = randomInstruction(serviceName),
                            input = "",
                            output = single,
                        )
                    }

                    // todo: spike better way to group APIs
                    val output = createForGroup(render, collections, file, instructions, translation)
                    if (output != null) {
                        val outputFile = File(outputDir, "$parentName-${file.nameWithoutExtension}.md")
                        outputFile.writeText(output)
                    }
                } catch (e: Exception) {
                    logger.error("Failed to parse ${file.absolutePath}", e)
                }
            }
        }

        // write apiNames to CSV
        val csv = File(this.outputDir, "apiNames.csv")
        csv.writeText(apiNames.joinToString("\n"))

        // write to jsonl
        val jsonl = File(this.outputDir, "instructions.jsonl")
        jsonl.writeText(instructions.joinToString("\n") { Json.encodeToString(it) })
    }

    private fun createForGroup(
        render: MarkdownTableRender,
        collections: List<ApiCollection>,
        file: File,
        instructions: MutableList<Instruction>,
        domainTranslation: MutableMap<String, String>,
    ): String? {
        val output = render.render(collections)
        val maybeAGoodApi = 128

        if (output.length > maybeAGoodApi) {
            instructions += Instruction(
                instruction = GROUP_API_INSTRUCTION,
                input = collections.joinToString(", ") { domainName(domainTranslation, it) },
                output = output,
            )

            return output
        } else {
            logger.info("Skip ${file.absolutePath} because it's too short")
        }

        return null
    }

    private fun domainName(
        domainTranslation: MutableMap<String, String>,
        it: ApiCollection,
    ) = domainTranslation[it.name] ?: it.name
}
