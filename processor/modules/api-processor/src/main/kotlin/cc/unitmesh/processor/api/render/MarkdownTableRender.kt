package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiItem
import cc.unitmesh.processor.api.base.ApiTagOutput

class MarkdownTableRender : ApiDetailRender {
    override fun render(apiItems: List<ApiItem>): String {
        val apiDetailsByTag = renderByTag(apiItems)
        return apiDetailsByTag.joinToString("\n\n") { it.toString() }
    }

    override fun renderItem(tags: List<String>, apiItems: List<ApiItem>): ApiTagOutput {
        val result: MutableList<String> = mutableListOf()
        result += listOf("| API | Method | Description | Request | Response | Error Response |")
        result += listOf("| --- | --- | --- | --- | --- | --- |")

        apiItems.forEach { detail ->
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