package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.processor.api.command.createOpenAiPrompter
import cc.unitmesh.processor.api.command.logger
import cc.unitmesh.verifier.markdown.MarkdownVerifier
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
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
        val prompter = createOpenAiPrompter()

        val pumlDir = File(inputDir.absolutePath, "domain")
        pumlDir.mkdirs()

        val usecaseDir = File(inputDir.absolutePath, "usecases")
        usecaseDir.mkdirs()

        val promptText = prompt.readText()

        val instructions: MutableList<Instruction> = mutableListOf()

        // load all uml files under output/domain/*.puml
        pumlDir.walk().forEachIndexed { index, file ->
            if (file.isFile && file.name.endsWith(".puml")) {
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

                var output: String = try {
                    prompter.prompt(newPrompt)
                } catch (e: Exception) {
                    logger.info("Failed to prompt ${file.absolutePath}", e)
                    return@forEachIndexed
                }
                output = clearOutput(output)

                instructions += Instruction(
                    instruction = "分析下面遗留代码的业务需求，并使用用户视角来编写需求用例。",
                    input = content,
                    output = output,
                )

                logger.debug("output to text: ${outputFile.absolutePath}")
                outputFile.writeText(output)
            }
        }

        val markdownVerifier = MarkdownVerifier()
        val headers = listOf("用例名称", "前置条件", "后置条件", "主成功场景", "扩展场景")

        // walk dir under usecaseDir filer all *.md
        usecaseDir.walk().forEachIndexed { index, file ->
            if (file.isFile && file.name.endsWith(".md")) {
                val content = file.readText()

                val isCorrect = markdownVerifier.tableVerifier(content, headers = headers)
                if (!isCorrect) {
                    logger.warn("Failed to verify ${file.absolutePath}")
                    logger.info("content: $content")
                    file.delete()
                }
            }
        }

        val jsonl = File(this.outputDir, "usecase.jsonl")
        jsonl.writeText(instructions.joinToString("\n") { Json.encodeToString(it) })
    }

    private fun clearOutput(output: String): String {
        // if content starts with ### or ends with ###, remove it
        var newOutput = output

        if (newOutput.startsWith("###")) {
            newOutput = newOutput.substring(3)
        }

        if (newOutput.endsWith("###")) {
            newOutput = newOutput.substring(0, newOutput.length - 3)
        }

        // remove continues empty line
        newOutput = newOutput.replace("\n\n\n", "\n\n")

        return newOutput
    }
}
