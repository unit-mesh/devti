package cc.unitmesh.importer

import kotlinx.serialization.Serializable

@Serializable
class SnippetPrompt(
    val id: Int,
    val identifierName: String,
    val requiredType: List<String>,
    val content: String,
    val prompt: String,
) {
}