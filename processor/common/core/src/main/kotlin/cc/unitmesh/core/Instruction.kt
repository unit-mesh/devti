package cc.unitmesh.core

import kotlinx.serialization.Serializable

@Serializable
class Instruction(
    val instruction: String,
    val input: String,
    val output: String,
)
