package cc.unitmesh.processor.api

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")
    }

    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(Runner::class.java)
    }
}