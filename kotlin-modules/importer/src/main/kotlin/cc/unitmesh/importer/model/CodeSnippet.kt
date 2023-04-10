package cc.unitmesh.importer.model

import kotlinx.serialization.Serializable

@Serializable
data class CodeSnippet(
    val identifierName: String,
    val size: Int,
    val path: String,
    val content: String,
    val imports: List<String>,
    // convert returnType and to String
    val requiredType: List<String> = listOf()
)

