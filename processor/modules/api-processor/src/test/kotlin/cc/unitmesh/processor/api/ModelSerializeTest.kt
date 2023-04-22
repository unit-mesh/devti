package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.model.postman.PostmanCollection
import io.kotest.matchers.shouldBe
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test

class ModelSerializeTest {
    private val text = javaClass.getResource("/openapi/CircleCI.postman_collection.json")!!.readText()

    @Test
    fun test() {
        // deserialize text to PostmanCollection
        val collection = Json.decodeFromString(PostmanCollection.serializer(), text)
        collection.info!!.name shouldBe "CircleCI"
    }
}