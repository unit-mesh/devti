package org.unitmesh.processor.swagger.converter

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI
import org.unitmesh.processor.swagger.ApiDetails
import java.io.File

class Swagger3Processor(private val api: OpenAPI) : SwaggerProcessor {
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
        fun fromFile(file: File): OpenAPI? {
            try {
                return OpenAPIV3Parser().read(file.absolutePath)
            } catch (e: Exception) {
                e.printStackTrace()
            }

            return null
        }
    }
}
