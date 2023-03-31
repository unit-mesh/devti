package org.unitmesh.processor.swagger.converter

import io.swagger.oas.models.OpenAPI


class Swagger2Processor(private val api: OpenAPI) : SwaggerProcessor {
    override fun mergeByTags(): List<ApiDetails> {
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
}
