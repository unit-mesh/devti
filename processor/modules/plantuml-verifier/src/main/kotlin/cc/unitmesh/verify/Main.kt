package cc.unitmesh.verify

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = PlantUmlVerifier()
    .main(args)

val logger: Logger = LoggerFactory.getLogger(PlantUmlVerifier::class.java)

class PlantUmlVerifier : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}
