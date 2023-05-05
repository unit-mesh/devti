package cc.unitmesh.verify

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = PlantUmlVerifier()
    .main(args)

val logger: Logger = LoggerFactory.getLogger(PlantUmlVerifier::class.java)

class PlantUmlVerifier : CliktCommand() {
    private val sourceDir by argument().file().default(File("output", "domain"))

    override fun run() {
        logger.info("Unit Connector Started")
        // walkdir in source dir
        sourceDir.walkTopDown().forEach {
            if (it.isFile && it.extension == "puml") {
                val isCorrect = PlantUmlParser(it).isCorrect()
                if (!isCorrect) {
                    logger.error("failed to verify ${it.absolutePath}")
                }
            }
        }
    }
}
