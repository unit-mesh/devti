package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiDetail
import cc.unitmesh.processor.api.base.ApiTagOutput

class MarkdownTableRender : ApiDetailRender {
    override fun render(apiDetails: List<ApiDetail>): String {
        val apiDetailsByTag = renderByTag(apiDetails)
        return apiDetailsByTag.joinToString("\n\n") { it.toString() }
    }

    override fun renderItem(tags: List<String>, apiDetails: List<ApiDetail>): ApiTagOutput {
        val result: MutableList<String> = mutableListOf()
        result += listOf("| API | Method | Description | Request | Response | Error Response |")
        result += listOf("| --- | --- | --- | --- | --- | --- |")

        apiDetails.forEach { detail ->
            val api = detail.path
            val method = detail.method
            val description = detail.description
            val request = detail.request!!.toString()
            val response = detail.response.toString()
            val errorResponse = "400: {\"error\": String}"
            result += listOf("| $api | $method | $description | $request | $response | $errorResponse |")
        }

        return ApiTagOutput(result.joinToString("\n"))
    }

}