package cc.unitmesh.verify

import net.sourceforge.plantuml.SourceStringReader
import java.io.File

class PlantUmlParser(private val file: File) {
    fun isCorrect(): Boolean {
        val source = file.readText()

        try {
            SourceStringReader(source)
        } catch (e: Exception) {
            return false
        }

        return true
    }
}
