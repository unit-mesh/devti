package cc.unitmesh.core.model

import kotlinx.serialization.Serializable

@Serializable
data class PreProcessorConfig(
    val scm: List<Scm>
)

@Serializable
data class Scm(
    val repository: String,
    val branch: String,
    val language: String
)
