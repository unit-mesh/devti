package cc.unitmesh.verifier.uml

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.IOException

fun main(args: Array<String>) = PlantUmlVerifier()
    .main(args)

val logger: Logger = LoggerFactory.getLogger(PlantUmlVerifier::class.java)

class PlantUmlVerifier : CliktCommand() {
    private val sourceDir by argument().file().default(File("output", "domain"))

    override fun run() {
        logger.info("Unit Connector Started")
        File(sourceDir, "svg").mkdirs()
        // walkdir in source dir
        sourceDir.walkTopDown().forEach {
            if (it.isFile && it.extension == "puml") {
                logger.info("verifying ${it.absolutePath}")
                try {
                    val isCorrect = PlantUmlParser(it).isCorrect()
                    if (!isCorrect) {
                        logger.info("failed to verify ${it.absolutePath}, will remove it")
                    }
                } catch (e: IOException) {
                    logger.error("failed to verify ${it.absolutePath}", e)
                }
            }
        }
    }
}
