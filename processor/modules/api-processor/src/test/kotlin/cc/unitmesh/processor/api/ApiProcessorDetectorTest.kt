package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.parser.PostmanProcessor
import cc.unitmesh.processor.api.swagger.v2.Swagger2Processor
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class ApiProcessorDetectorTest {

    @Test
    fun detectApiProcessor() {
        val file = File("src/test/resources/testsets/postman.json")
        val processor = ApiProcessorDetector.detectApiProcessor(file)!!
        processor.javaClass shouldBe PostmanProcessor::class.java

        // swagger-2.json
        val file2 = File("src/test/resources/testsets/swagger-2.json")
        val processor2 = ApiProcessorDetector.detectApiProcessor(file2)!!
        processor2.javaClass shouldBe Swagger2Processor::class.java
        //swagger-2.yaml
        //swagger-3.json
        //swagger-3.yaml

    }
}