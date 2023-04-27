package cc.unitmesh.processor.api

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = UnitConnector()
    .subcommands(Prompting())
    .main(args)

val logger: Logger = LoggerFactory.getLogger(UnitConnector::class.java)

class UnitConnector : CliktCommand() {
    override fun run() = Unit
}

class Prompting : CliktCommand(help = "Prompting Runner") {
    override fun run() {
        logger.info("Prompting Started")
    }
}