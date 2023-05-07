package cc.unitmesh.core

import kotlinx.serialization.Serializable

@Serializable
data class Instruction(
    val instruction: String,
    val input: String,
    val output: String,
)
