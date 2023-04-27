package cc.unitmesh.processor.api

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
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
    private val source by argument().file()
    private val itemsNum by argument().int()
    private val prompt by argument().file()
    private val outputDir by argument().file()

    override fun run() {
        logger.info("Prompting Started")
    }
}