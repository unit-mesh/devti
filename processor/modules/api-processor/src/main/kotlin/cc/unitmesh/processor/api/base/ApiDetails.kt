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
) {
    companion object {
        fun formatApiDetailsByTag(apiDetails: List<ApiDetails>): List<ApiTagOutput> {
            val result: MutableList<ApiTagOutput> = mutableListOf()
            apiDetails.groupBy { it.tags }.forEach { (tags, apiDetails) ->
                val tag = tags.joinToString(", ")
                val apiDetailsString = apiDetails.joinToString("\n") {
                    "${it.method}${operationInformation(it)} ${it.path} ${it.summary}"
                }
                result += listOf(ApiTagOutput("$tag\n$apiDetailsString"))
            }

            return result
        }

        private fun operationInformation(it: ApiDetails): String {
            if (it.operationId.isEmpty()) return ""

            return " ${it.operationId}${ioParameters(it)}"
        }

        private fun ioParameters(details: ApiDetails): String {
            val inputs = details.inputs.joinToString(", ") { it.toString() }
            val outputs = details.outputs.joinToString(", ") { it.toString() }
            if (inputs.isEmpty() && outputs.isEmpty()) return "()"
            if (inputs.isEmpty()) return "(): $outputs"
            if (outputs.isEmpty()) return "($inputs)"

            return "(${inputs}) : $outputs"
        }
    }
}