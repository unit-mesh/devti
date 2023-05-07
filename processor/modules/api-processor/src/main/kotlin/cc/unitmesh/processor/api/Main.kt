package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.processor.api.command.Workspace
import cc.unitmesh.processor.api.command.logger
import cc.unitmesh.verifier.markdown.UsecaseParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import java.io.File

fun main(args: Array<String>) = Bundling()
//    .subcommands(Prompting())
    .main(args)


class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

class Bundling : CliktCommand() {
    private val outputDir by argument().file().default(File("output"))

    override fun run() {
        logger.info("Bundling Started")

        val instructions: MutableList<Instruction> = mutableListOf()

        val usecaseDir = Workspace.usecases(outputDir.absolutePath)
        val usecases = usecaseDir.listFiles()?.filter { it.isFile }?.map { it.readText() } ?: emptyList()
        instructions += usecases.map(::createUsecasePrompt)

        val pumlDir = Workspace.puml(outputDir.absolutePath)
        instructions += usecaseDir.walk().map { file ->
            val instruction = createDomainModel(file, pumlDir)
            println(instruction)
            instruction
        }.filterNotNull()
    }
}

private val USECASE_NAME = "用例名称"

fun createUsecasePrompt(markdown: String): Instruction {
    val strings = usecasesName(markdown)
    return Instruction(
        instruction = "使用 markdown 编写用例: ${strings.joinToString(separator = ",")}",
        input = "",
        output = markdown
    )
}

private fun usecasesName(markdown: String) = UsecaseParser().filterTableColumn(markdown, USECASE_NAME)

private fun createDomainModel(file: File, pumlDir: File): Instruction? {
    if (file.isFile && file.name.endsWith(".md")) {
        // get origin puml file from pumlDir
        val pumlFile = File(pumlDir, file.name.replace("md", "puml"))
        if (!pumlFile.exists()) {
            logger.info("Skipping ${file.absolutePath}")
            return null
        }

        val content = file.readText()
        val model = pumlFile.readText()
        return Instruction(
            instruction = "分析下面的业务需求，设计领域模型：" + usecasesName(content).joinToString(separator = ","),
            input = "",
            output = model,
        )
    }

    return null
}
