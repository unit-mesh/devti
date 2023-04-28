package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.ApiProcessorDetector
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class MarkdownTableRenderTest {
    @Test
    fun should_handle_to_table() {
        val file2 = File("src/test/resources/testsets/swagger-3.yaml")
        val processor2 = ApiProcessorDetector.detectApiProcessor(file2)!!
        val apiDetails = processor2.convertApi()

        val render = MarkdownTableRender()
        val result = render.render(apiDetails)

        val expected =
"""
| API | Method | Description | Request | Response | Error Response |
| --- | --- | --- | --- | --- | --- |
| /cashback-campaigns/{campaignId} | GET |  | X-App-Token: string, campaignId: string | [200: {campaignId: string, campaignName: string, startDate: string, endDate: string, status: string, remainingAmountInCents: integer}, 400: {errors: array}, 401: {errors: array}, 403: {errors: array}, 404: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks | GET |  | X-App-Token: string, campaignId: string, pageNumber: integer, pageSize: integer, fromDateTime: string, toDateTime: string, status: string | [200: {cashbacks: array, totalElementCount: integer}, 400: {errors: array}, 401: {errors: array}, 403: {errors: array}, 404: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks | POST |  | X-App-Token: string, campaignId: string | [201: {cashbackId: string, url: string, amountInCents: integer, createdDateTime: string, expiryDateTime: string, redeemedDateTime: string, status: string, referenceId: string, locationId: string, locationAddress: string}, 400: {errors: array}, 401: {errors: array}, 403: {errors: array}, 404: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks/{cashbackId} | GET |  | X-App-Token: string, campaignId: string, cashbackId: string | [200: {cashbackId: string, url: string, amountInCents: integer, createdDateTime: string, expiryDateTime: string, redeemedDateTime: string, status: string, referenceId: string, locationId: string, locationAddress: string}, 400: {errors: array}, 401: {errors: array}, 403: {errors: array}, 404: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
| /cashback-subscriptions | POST |  | X-App-Token: string | [201: {subscriptionId: string}, 400: {errors: array}, 401: {errors: array}, 403: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
| /cashback-subscriptions | DELETE |  | X-App-Token: string | [204: {}, 401: {errors: array}, 403: {errors: array}, 500: {errors: array}] | 400: {"error": String} |
""".trimIndent()
        assertEquals(expected, result)
    }

}