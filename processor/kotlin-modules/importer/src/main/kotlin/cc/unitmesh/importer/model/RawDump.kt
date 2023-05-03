package cc.unitmesh.importer.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.Code
import kotlin.io.path.Path

@Serializable
data class RawDump(
    val repo_name: String,
    var path: String,
    val copies: String,
    var size: Int,
    var content: String,
    val license: String,
) {
    fun identifierName(): String {
        // match the package name by regex `package com.cognifide.gradle.aem.instance.tasks`
        val packageName = Regex("package\\s+(\\S+)").find(content)?.groupValues?.get(1)
        // match class name by FilePath
        val className = path.substringAfterLast("/").substringBeforeLast(".")
        return "$packageName.$className"
    }

    fun toCode(): Code {
        return Code(
            content = content.trimIndent(),
            filePath = Path(path),
            fileName = path.substringAfterLast("/"),
            script = path.endsWith(".kts", ignoreCase = true),
            isStdIn = false,
        )
    }

    companion object {
        fun fromString(text: String): RawDump {
            return Json.decodeFromString(text)
        }
    }
}
