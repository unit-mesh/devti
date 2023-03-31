package org.unitmesh.processor.swagger.converter

import io.swagger.oas.models.OpenAPI
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.models.SwaggerParseResult
import org.unitmesh.processor.swagger.ApiDetails
import java.io.File


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

    companion object {
        fun fromFile(it: File): OpenAPI? {
            val result: SwaggerParseResult =
                OpenAPIParser().readContents(it.readText(), null, null)
            val openAPI = result.openAPI
            if (result.messages != null) result.messages.forEach(System.err::println); // validation errors and warnings
            return openAPI
        }
    }
}
