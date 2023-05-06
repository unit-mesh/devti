package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.command.Modeling
import cc.unitmesh.processor.api.command.logger
import com.github.ajalt.clikt.core.CliktCommand

fun main(args: Array<String>) = Modeling()
//    .subcommands(Prompting())
    .main(args)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

