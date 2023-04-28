package cc.unitmesh.processor.api.swagger

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.base.ApiDetails
import cc.unitmesh.processor.api.base.Parameter
import io.swagger.oas.models.OpenAPI


class Swagger2Processor(private val api: OpenAPI) : ApiProcessor {
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
                                name = parameter.name ?: "",
                                type = parameter.schema?.type ?: ""
                            )
                        } ?: listOf(),
                        outputs = operation.responses?.values?.flatMap { response ->
                            response.content?.values?.flatMap { content ->
                                content.schema?.properties?.map { (name, schema) ->
                                    Parameter(
                                        name = name ?: "",
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
//        fun fromFile(it: File): OpenAPI? {
//            val result: SwaggerParseResult =
//                OpenAPIParser().readContents(it.readText(), null, null)
//            val openAPI = result.openAPI
//            if (result.messages != null) result.messages.forEach(System.err::println); // validation errors and warnings
//            return openAPI
//        }
    }
}
