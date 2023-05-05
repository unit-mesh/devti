package cc.unitmesh.verify

import net.sourceforge.plantuml.SourceStringReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

class PlantUmlParser(private val file: File) {
    fun isCorrect(): Boolean {
        val source = file.readText()

        val reader = SourceStringReader(source)

        try {
            reader.generateDiagramDescription()
        } catch (e: Exception) {
            logger.info("failed to parse ${file.absolutePath}", e)
            return false
        }

        return true
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(PlantUmlParser::class.java)
    }
}
