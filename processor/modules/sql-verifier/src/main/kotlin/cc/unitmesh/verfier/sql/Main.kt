package cc.unitmesh.verfier.sql

import cc.unitmesh.verifier.sql.SqlParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = SqlVerifier()
    .main(args)

val logger: Logger = LoggerFactory.getLogger(SqlVerifier::class.java)

class SqlVerifier : CliktCommand() {
    private val sourceDir by argument().file().default(File("output", "domain"))

    override fun run() {
        logger.info("Unit Connector Started")

        // walkdir in source dir
        sourceDir.walkTopDown().forEach {
            if (it.isFile && it.extension == "sql") {
                val sqlText = it.readText()
                val isCorrect = SqlParser.isCorrect(sqlText)
                if (!isCorrect) {
                    logger.info("failed to verify ${it.absolutePath}, will remove it")
                }
            }
        }
    }
}
