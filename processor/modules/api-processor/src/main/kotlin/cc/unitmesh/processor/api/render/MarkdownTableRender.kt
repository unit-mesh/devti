package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiDetail

class MarkdownTableRender : ApiDetailRender {
    override fun render(apiDetails: List<ApiDetail>): String {
        val apiDetailsByTag = format(apiDetails)
        return apiDetailsByTag.joinToString("\n") { it }
    }

    private fun format(apiDetails: List<ApiDetail>): List<String> {
        val result: MutableList<String> = mutableListOf()
        result += listOf("| API | Method | Description | Request | Response | Error Response |")
        result += listOf("| --- | --- | --- | --- | --- | --- |")

        apiDetails.forEach { apiDetails ->
            val api = apiDetails.path
            val method = apiDetails.method
            val description = apiDetails.description
            val request = apiDetails.request!!.toString()
            val response = apiDetails.response.toString()
            val errorResponse = "400: {\"error\": String}"
            result += listOf("| $api | $method | $description | $request | $response | $errorResponse |")
        }

        return result
    }

}