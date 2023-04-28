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
| /cashback-campaigns/{campaignId} | GET |  | X-App-Token: string, campaignId: string |  | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks | GET |  | X-App-Token: string, campaignId: string, pageNumber: integer, pageSize: integer, fromDateTime: string, toDateTime: string, status: string | cashbacks: array, totalElementCount: integer | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks | POST |  | X-App-Token: string, campaignId: string |  | 400: {"error": String} |
| /cashback-campaigns/{campaignId}/cashbacks/{cashbackId} | GET |  | X-App-Token: string, campaignId: string, cashbackId: string |  | 400: {"error": String} |
| /cashback-subscriptions | POST |  | X-App-Token: string | subscriptionId: string | 400: {"error": String} |
| /cashback-subscriptions | DELETE |  | X-App-Token: string |  | 400: {"error": String} |
""".trimIndent()
        assertEquals(expected, result)
    }

}