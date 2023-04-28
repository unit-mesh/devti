package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.base.ApiDetailRender
import cc.unitmesh.processor.api.base.ApiDetails

class MarkdownTableRender : ApiDetailRender {
    override fun render(apiDetails: List<ApiDetails>): String {
        val apiDetailsByTag = format(apiDetails)
        return apiDetailsByTag.joinToString("\n") { it }
    }

    private fun format(apiDetails: List<ApiDetails>): List<String> {
        //| API | Method | Description | Request | Response | Error Response |
//| --- | --- | --- | --- | --- | --- |
//| /confirmation-of-funds | POST | Confirm whether sufficient funds are available for a requested payment | {"account_id": String, "amount": Number, "currency": String, "destination_account_id": String, "reference": String} | 200 {"sufficient_funds": Boolean} | 400: {"error": String} |
//| /accounts | GET | Get accounts associated with a user | {"user_id": String} | 200 [{"id": String, "type": String, "currency": String, "balance": Number},...] | 400: {"error": String} |
//| /payments | POST | Make a payment from one account to another | {"account_id": String, "destination_account_id": String, "amount": Number, "currency": String, "reference": String} | 200 {"id": String, "status": String} | 400: {"error": String} |
//| /transactions | GET | Get transactions for a specific account | {"account_id": String} | 200 [{"id": String, "date": String, "amount": Number, "currency": String, "description": String},...] | 400: {"error": String} |
        val result: MutableList<String> = mutableListOf()
        result += listOf("| API | Method | Description | Request | Response | Error Response |")
        result += listOf("| --- | --- | --- | --- | --- | --- |")

        apiDetails.forEach { apiDetails ->
            val api = apiDetails.path
            val method = apiDetails.method
            val description = apiDetails.summary
            val request = apiDetails.inputs.joinToString(", ") { it.toString() }
            val response = apiDetails.outputs.joinToString(", ") { it.toString() }
            val errorResponse = "400: {\"error\": String}"
            result += listOf("| $api | $method | $description | $request | $response | $errorResponse |")
        }

        return result
    }

}