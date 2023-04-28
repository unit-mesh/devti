package cc.unitmesh.processor.api.base

import kotlinx.serialization.Serializable

@Serializable
class Instruction(
    val instruction: String,
    val input: String,
    val output: String
)
