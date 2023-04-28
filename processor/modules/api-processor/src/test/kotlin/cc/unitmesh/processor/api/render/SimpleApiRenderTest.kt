package cc.unitmesh.processor.api.render

import cc.unitmesh.processor.api.ApiProcessorDetector
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class SimpleApiRenderTest {
    @Test
    fun should_render_api_to_string() {
        val file2 = File("src/test/resources/testsets/swagger-3.yaml")
        val processor2 = ApiProcessorDetector.detectApiProcessor(file2)!!
        val apiDetails = processor2.convertApi()

        val render = SimpleApiRender()
        val result = render.render(apiDetails)

        val expected = """Cashback campaign
GET getCashbackCampaign(X-App-Token: string, campaignId: string) /cashback-campaigns/{campaignId} 

Cashback
GET getCashbackList(X-App-Token: string, campaignId: string, pageNumber: integer, pageSize: integer, fromDateTime: string, toDateTime: string, status: string) : cashbacks: array, totalElementCount: integer /cashback-campaigns/{campaignId}/cashbacks 
POST createCashback(X-App-Token: string, campaignId: string) /cashback-campaigns/{campaignId}/cashbacks 
GET getCashback(X-App-Token: string, campaignId: string, cashbackId: string) /cashback-campaigns/{campaignId}/cashbacks/{cashbackId} 

Cashback notification
POST subscribeCashbackNotifications(X-App-Token: string) : subscriptionId: string /cashback-subscriptions 
DELETE deleteCashbackNotifications(X-App-Token: string) /cashback-subscriptions """
        assertEquals(expected, result)
    }
}