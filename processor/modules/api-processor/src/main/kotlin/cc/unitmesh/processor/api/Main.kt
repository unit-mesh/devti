package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.core.prompter.OpenAiPrompter
import cc.unitmesh.processor.api.command.logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import io.github.cdimascio.dotenv.Dotenv
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) = Usecase()
//    .subcommands(Prompting())
    .main(args)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

class Usecase : CliktCommand() {
    private val prompt by argument().file().default(File("usecase-prompt.txt"))
    private val inputDir by argument().file().default(File("output"))
    private val outputDir by argument().file().default(File("output"))

    override fun run() {
        logger.info("Usecases Started")

        logger.debug("loading dotenv")
        val dotenv = Dotenv.load()
        val proxy = dotenv.get("OPEN_AI_PROXY")
        val key = dotenv.get("OPEN_AI_KEY")
        val prompter = OpenAiPrompter(key, proxy)

        // prompt with new
        val pumlDir = File(inputDir.absolutePath, "domain")
        pumlDir.mkdirs()


        // prompt with new
        val usecaseDir = File(inputDir.absolutePath, "usecases")
        usecaseDir.mkdirs()

        val promptText = prompt.readText()

        val instructions: MutableList<Instruction> = mutableListOf()

        // load all uml files under output/domain/*.puml
        pumlDir.walk().forEachIndexed { index, file ->
            if (file.isFile && file.name.endsWith(".puml")) {
                logger.info("Processing ${file.absolutePath}")
                val content = file.readText()

                val newPrompt = promptText.replace("{code}", content)

                val outputFile = File(usecaseDir, file.name.replace("puml", "md"))
                if (outputFile.exists()) {
                    instructions += Instruction(
                        instruction = "分析下面遗留代码的业务需求，并使用用户视角来编写需求用例。",
                        input = content,
                        output = outputFile.readText(),
                    )
                    logger.info("Skipping ${file.absolutePath}")
                    return@forEachIndexed
                }

                var output = ""
                try {
                    output = prompter.prompt(newPrompt)
                } catch (e: Exception) {
                    logger.info("Failed to prompt ${file.absolutePath}", e)
                }

                val instruction = Instruction(
                    instruction = "分析下面遗留代码的业务需求，并使用用户视角来编写需求用例。",
                    input = content,
                    output = output,
                )
                instructions += instruction

                logger.debug("output to text: ${outputFile.absolutePath}")
                outputFile.writeText(output)
            }
        }

        val jsonl = File(this.outputDir, "usecase.jsonl")
        jsonl.writeText(instructions.joinToString("\n") { Json.encodeToString(it) })
    }
}