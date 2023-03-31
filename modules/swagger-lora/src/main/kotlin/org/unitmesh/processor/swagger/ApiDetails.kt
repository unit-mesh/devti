package org.unitmesh.processor.swagger

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
        fun formatApiDetailsByTag(apiDetails: List<ApiDetails>): String {
            val result = StringBuilder()
            apiDetails.groupBy { it.tags }.forEach { (tags, apiDetails) ->
                result.appendLine(tags.joinToString(", "))
                apiDetails.forEach {
                    result.appendLine("${it.method} ${it.path} ${it.summary}")
                }
            }

            return result.toString()
        }
    }
}