package org.unitmesh.processor.swagger.converter

import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI
import org.unitmesh.processor.swagger.ApiDetails
import org.unitmesh.processor.swagger.Parameter
import java.io.File

class Swagger3Processor(private val api: OpenAPI) : SwaggerProcessor {
    override fun convertApi(): List<ApiDetails> {
        val result = mutableListOf<ApiDetails>()
        if (api.paths == null) return result

        api.paths.forEach { (path, pathItem) ->
            pathItem.readOperationsMap().forEach { (method, operation) ->
                result.add(
                    ApiDetails(
                        path = path,
                        method = method.toString(),
                        summary = operation.summary ?: "",
                        operationId = operation.operationId ?: "",
                        tags = operation.tags ?: listOf(),
                        inputs = operation.parameters?.map { parameter ->
                            Parameter(
                                name = parameter.name,
                                type = parameter.schema?.type ?: ""
                            )
                        } ?: listOf(),
                        outputs = operation.responses?.values?.flatMap { response ->
                            response.content?.values?.flatMap { content ->
                                content.schema?.properties?.map { (name, schema) ->
                                    Parameter(
                                        name = name,
                                        type = schema.type ?: ""
                                    )
                                } ?: listOf()
                            } ?: listOf()
                        } ?: listOf()
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
