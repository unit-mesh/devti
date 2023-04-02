package cc.unitmesh.core.llm

import kotlinx.serialization.Serializable

@Serializable
data class Instruct(
    val instruction: String,
    val input: String,
    val output: String,
)