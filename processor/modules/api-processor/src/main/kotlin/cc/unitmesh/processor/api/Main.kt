package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.command.Usecase
import cc.unitmesh.processor.api.command.logger
import com.github.ajalt.clikt.core.CliktCommand

fun main(args: Array<String>) = Usecase()
//    .subcommands(Prompting())
    .main(args)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

