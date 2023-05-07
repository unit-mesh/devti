package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.processor.api.command.Usecase
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

        val dir = Workspace.usecases(outputDir.absolutePath)
        val usecases = dir.listFiles()?.filter { it.isFile }?.map { it.readText() } ?: emptyList()
        instructions += usecases.map(::createUsecasePrompt)


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