package cc.unitmesh.processor.api.swagger

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.base.ApiDetail
import cc.unitmesh.processor.api.base.Parameter
import cc.unitmesh.processor.api.base.Request
import cc.unitmesh.processor.api.base.Response
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI
import java.io.File

class Swagger3Processor(private val api: OpenAPI) : ApiProcessor {
    override fun convertApi(): List<ApiDetail> {
        val result = mutableListOf<ApiDetail>()
        if (api.paths == null) return result

        api.paths.forEach { (path, pathItem) ->
            pathItem.readOperationsMap().forEach { (method, operation) ->
                result.add(
                    ApiDetail(
                        path = path,
                        method = method.toString(),
                        summary = operation.summary ?: "",
                        operationId = operation.operationId ?: "",
                        tags = operation.tags ?: listOf(),
                        request = operation.parameters?.map {
                            Parameter(
                                name = it.name,
                                type = it.schema?.type ?: ""
                            )
                        }?.let { Request(it) },
                        response = operation.responses?.values?.flatMap { response ->
                            response.content?.values?.flatMap { content ->
                                content.schema?.properties?.map { (name, schema) ->
                                    Parameter(
                                        name = name,
                                        type = schema.type ?: ""
                                    )
                                } ?: listOf()
                            } ?: listOf()
                        }?.let { Response(it) }
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
