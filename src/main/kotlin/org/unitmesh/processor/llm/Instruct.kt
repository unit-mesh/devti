package org.unitmesh.processor.llm

import kotlinx.serialization.Serializable

@Serializable
data class Instruct(
    val instruction: String,
    val input: String,
    val output: String,
)