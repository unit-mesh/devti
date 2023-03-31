package org.unitmesh.processor.swagger

import kotlinx.serialization.Serializable


@Serializable
data class ApiTagOutput(val string: String) {
    override fun toString() = string
}

data class ApiDetails(
    val path: String,
    val method: String,
    val summary: String,
    val operationId: String,
    val tags: List<String>
) {
    companion object {
        // Login
        // POST /users/login Login with REST API
        // POST /users/register Register with REST API
        fun formatApiDetailsByTag(apiDetails: List<ApiDetails>): List<ApiTagOutput> {
            var result: List<ApiTagOutput> = listOf()
            apiDetails.groupBy { it.tags }.forEach { (tags, apiDetails) ->
                val tag = tags.joinToString(", ")
                val apiDetailsString = apiDetails.joinToString("\n") {
                    "${it.method} ${it.path} ${it.summary}"
                }
                result = listOf(ApiTagOutput("$tag\n$apiDetailsString"))
            }

            return result
        }
    }
}