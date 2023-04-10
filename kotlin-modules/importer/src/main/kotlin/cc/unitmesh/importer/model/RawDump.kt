package cc.unitmesh.importer.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.Code
import org.jetbrains.kotlinx.dataframe.annotations.DataSchema
import kotlin.io.path.Path

@DataSchema
@Serializable
data class RawDump(
    @kotlinx.serialization.SerialName("repo_name")
    val repoName: String,
    val path: String,
    val copies: String,
    val size: String,
    val content: String,
    val license: String
) {

    fun toCode(): Code {
        return Code(
            content = content.trimIndent(),
            filePath = Path(path),
            fileName = path.substringAfterLast("/"),
            script = path.endsWith(".kts", ignoreCase = true),
            isStdIn = false
        )
    }

    companion object {
        fun fromString(text: String): RawDump {
            return Json.decodeFromString(text)
        }
    }
}