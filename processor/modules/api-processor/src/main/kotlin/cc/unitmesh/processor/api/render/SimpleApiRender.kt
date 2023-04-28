package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiDetail
import cc.unitmesh.processor.api.base.ApiTagOutput

class SimpleApiRender : ApiDetailRender {
    override fun render(apiDetails: List<ApiDetail>): String {
        val apiDetailsByTag = renderByTag(apiDetails)
        return apiDetailsByTag.joinToString("\n\n") { it.toString() }
    }

    override fun renderItem(tags: List<String>, apiDetails: List<ApiDetail>): ApiTagOutput {
        val tag = tags.joinToString(", ")
        val apiDetailsString = apiDetails.joinToString("\n") {
            "${it.method} ${it.path} ${operationInformation(it)} "
        }

        return ApiTagOutput("$tag\n$apiDetailsString")
    }

    private fun operationInformation(it: ApiDetail): String {
        if (it.operationId.isEmpty()) return ""

        return " ${it.operationId}${ioParameters(it)}"
    }

    private fun ioParameters(details: ApiDetail): String {
        val inputs = details.request.toString()
        val outputs = details.response.toString()
        if (inputs.isEmpty() && outputs.isEmpty()) return "()"
        if (inputs.isEmpty()) return "(): $outputs"
        if (outputs.isEmpty()) return "($inputs)"

        return "(${inputs}) : $outputs"
    }
}