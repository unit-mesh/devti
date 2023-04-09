package cc.unitmesh.importer.model

import kotlinx.serialization.Serializable

@Serializable
data class RawDump(
    @kotlinx.serialization.SerialName("repo_name")
    val repo_name: String,
    val path: String,
    val copies: String,
    val size: String,
    val content: String,
    val license: String
)

