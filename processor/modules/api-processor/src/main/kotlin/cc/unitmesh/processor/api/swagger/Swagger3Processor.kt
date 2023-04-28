package cc.unitmesh.processor.api.swagger

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.base.ApiDetail
import cc.unitmesh.processor.api.base.Parameter
import cc.unitmesh.processor.api.base.Request
import cc.unitmesh.processor.api.base.Response
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.Operation
import io.swagger.v3.oas.models.responses.ApiResponse
import java.io.File

class Swagger3Processor(private val api: OpenAPI) : ApiProcessor {
    override fun convertApi(): List<ApiDetail> {
        val result = mutableListOf<ApiDetail>()
        if (api.paths == null) return result

        api.paths.forEach { (path, pathItem) ->
            pathItem.readOperationsMap().forEach { (method, operation) ->
                val apiDetail = ApiDetail(
                    path = path,
                    method = method.toString(),
                    summary = operation.summary ?: "",
                    operationId = operation.operationId ?: "",
                    tags = operation.tags ?: listOf(),
                    request = convertRequest(operation),
                    response = convertResponses(operation)
                )

                result.add(apiDetail)
            }
        }

        return result
    }

    private fun convertResponses(operation: Operation): List<Response> {
        return operation.responses?.map {
            val code = it.key.toInt()
            val responseBody = handleResponse(it.value) ?: listOf()
            Response(code, responseBody)
        } ?: listOf()
    }

    private val apiResponseMutableMap = api.components?.responses
    private val apiSchemaMutableMap = api.components?.schemas

    private fun handleResponse(response: ApiResponse): List<Parameter>? {
        val refsResponse = response.content?.values?.flatMap { content ->
            content.schema?.`$ref`?.let { ref ->
                val refName = ref.substringAfterLast("/")
                val refResponse = getFromResponseOrSchema(refName)
                if (refResponse != null) {
                    handleResponse(refResponse)
                } else {
                    listOf()
                }
            } ?:

            content.schema?.properties?.map { (name, schema) ->
                Parameter(
                    name = name,
                    type = schema.type ?: ""
                )
            } ?: listOf()
        }

        return refsResponse
    }

    private fun getFromResponseOrSchema(refName: String): ApiResponse?  = apiResponseMutableMap?.get(refName)

    private fun convertRequest(operation: Operation): Request? {
        val parameters = operation.parameters?.map {
            Parameter(
                name = it.name,
                type = it.schema?.type ?: ""
            )
        }

        val request = operation.requestBody?.content?.values?.flatMap { content ->
            content.schema?.properties?.map { (name, schema) ->
                Parameter(
                    name = name,
                    type = schema.type ?: ""
                )
            } ?: listOf()
        } ?: listOf()

        return Request(parameters ?: listOf(), request)
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
