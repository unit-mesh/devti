package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.processor.api.command.Usecase
import cc.unitmesh.processor.api.command.logger
import com.github.ajalt.clikt.core.CliktCommand

fun main(args: Array<String>) = Bundling()
//    .subcommands(Prompting())
    .main(args)


class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

class Bundling : CliktCommand() {
    override fun run() {

        logger.info("Bundling Started")
    }
}

fun createUsecasePrompt(markdown: String): List<Instruction> {
    //
    return listOf()
}