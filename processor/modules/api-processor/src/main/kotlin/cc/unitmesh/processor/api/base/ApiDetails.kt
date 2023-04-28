package cc.unitmesh.processor.api.base

import kotlinx.serialization.Serializable


@Serializable
data class ApiTagOutput(val string: String) {
    override fun toString() = string
}

@Serializable
data class Parameter(
    val name: String,
    val type: String,
) {
    override fun toString() = "$name: $type"
}

@Serializable
data class ApiDetails(
    val path: String,
    val method: String,
    val summary: String,
    val operationId: String,
    val tags: List<String>,
    val inputs: List<Parameter> = listOf(),
    val outputs: List<Parameter> = listOf(),
)