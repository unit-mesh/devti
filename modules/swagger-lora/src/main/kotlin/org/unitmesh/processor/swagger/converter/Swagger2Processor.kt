package org.unitmesh.processor.swagger.converter

import io.swagger.oas.models.OpenAPI

data class ApiDetails(val path: String, val method: String, val summary: String, val operationId: String, val tags: List<String>) {

}

class Swagger2Processor(private val api: OpenAPI) {
    fun mergeByTags(): List<ApiDetails> {
        val result = mutableListOf<ApiDetails>()
        api.paths.forEach { (path, pathItem) ->
            pathItem.readOperationsMap().forEach { (method, operation) ->
                result.add(
                    ApiDetails(
                        path = path,
                        method = method.toString(),
                        summary = operation.summary ?: "",
                        operationId = operation.operationId ?: "",
                        tags = operation.tags ?: listOf()
                    )
                )
            }
        }
        return result
    }

    companion object {
        //
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
