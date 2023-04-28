package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.parser.PostmanProcessor
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class ApiProcessorDetectorTest {
    @Test
    fun detectApiProcessor() {
        // read file in resources/tests/postman.json
        val file = File("src/test/resources/tests/postman.json")
        val processor = ApiProcessorDetector.detectApiProcessor(file)!!
        processor.javaClass shouldBe PostmanProcessor::class
    }
}