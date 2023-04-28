package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiDetails
import cc.unitmesh.processor.api.base.ApiTagOutput

class SimpleApiRender : ApiDetailRender {
    override fun render(apiDetails: List<ApiDetails>): String {
        val apiDetailsByTag = format(apiDetails)
        return apiDetailsByTag.joinToString("\n\n") { it.toString() }
    }

    private fun format(apiDetails: List<ApiDetails>): List<ApiTagOutput> {
        val result: MutableList<ApiTagOutput> = mutableListOf()
        apiDetails.groupBy { it.tags }.forEach { (tags, apiDetails) ->
            val tag = tags.joinToString(", ")
            val apiDetailsString = apiDetails.joinToString("\n") {
                "${it.method}${operationInformation(it)} ${it.path} ${it.summary}"
            }
            result += listOf(ApiTagOutput("$tag\n$apiDetailsString"))
        }

        return result
    }

    private fun operationInformation(it: ApiDetails): String {
        if (it.operationId.isEmpty()) return ""

        return " ${it.operationId}${ioParameters(it)}"
    }

    private fun ioParameters(details: ApiDetails): String {
        val inputs = details.inputs.joinToString(", ") { it.toString() }
        val outputs = details.outputs.joinToString(", ") { it.toString() }
        if (inputs.isEmpty() && outputs.isEmpty()) return "()"
        if (inputs.isEmpty()) return "(): $outputs"
        if (outputs.isEmpty()) return "($inputs)"

        return "(${inputs}) : $outputs"
    }
}