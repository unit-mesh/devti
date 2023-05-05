package cc.unitmesh.verify

import net.sourceforge.plantuml.FileFormat
import net.sourceforge.plantuml.FileFormatOption
import net.sourceforge.plantuml.SourceStringReader
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.nio.charset.Charset

class PlantUmlParser(private val file: File) {
    fun isCorrect(): Boolean {
        val source = file.readText()

        val reader = SourceStringReader(source)

        try {
            val os = ByteArrayOutputStream()
            reader.generateImage(os, FileFormatOption(FileFormat.SVG))
            val svg = String(os.toByteArray(), Charset.forName("UTF-8"))

            if (svg.contains("Syntax Error?")) {
                logger.error("failed to parse ${file.absolutePath}")
                return false
            }

            // parentFile + "svg" + name
            val svgFile = File(file.parentFile, "svg/${file.nameWithoutExtension}.svg")
            svgFile.writeText(svg)
        } catch (e: Exception) {
            logger.error("failed to parse ${file.absolutePath}", e)
            return false
        }

        return true
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(PlantUmlParser::class.java)
    }
}
