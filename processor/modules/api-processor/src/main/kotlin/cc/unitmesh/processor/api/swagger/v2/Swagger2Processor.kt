package cc.unitmesh.processor.api.swagger.v2

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.base.ApiDetails
import v2.io.swagger.models.Swagger
import v2.io.swagger.parser.SwaggerParser
import java.io.File


class Swagger2Processor(private val api: Swagger) : ApiProcessor {
    override fun convertApi(): List<ApiDetails> {
        val result = mutableListOf<ApiDetails>()
        if (api.paths == null) return result

        api.paths.forEach { (path, pathItem) ->
            pathItem.operationMap.forEach { (method, operation) ->
                result.add(
                    ApiDetails(
                        path = path,
                        method = method.toString(),
                        summary = operation.summary ?: "",
                        operationId = operation.operationId ?: "",
                        tags = operation.tags ?: listOf(),
                        inputs = operation.parameters?.map { parameter ->
                            cc.unitmesh.processor.api.base.Parameter(
                                name = parameter.name ?: "",
                                type = ""
                            )
                        } ?: listOf(),
                        outputs = listOf()
                    )
                )
            }
        }

        return result
    }

    companion object {
        fun fromFile(it: File): Swagger {
            return SwaggerParser().read(it.readText())
        }
    }
}
