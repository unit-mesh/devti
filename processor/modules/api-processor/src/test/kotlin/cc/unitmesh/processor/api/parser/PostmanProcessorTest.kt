package cc.unitmesh.processor.api.parser

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class PostmanProcessorTest {
    @Test
    fun should_convert_postman_collection_to_api_details() {
        val file = File("src/test/resources/testsets/postman.json")
        val processor = PostmanProcessor(file)
        val apiDetails = processor.convertApi()

        assertEquals(5, apiDetails.size)
    }
}