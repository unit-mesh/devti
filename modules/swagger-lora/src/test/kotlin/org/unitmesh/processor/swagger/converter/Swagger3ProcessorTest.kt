package org.unitmesh.processor.swagger.converter

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.unitmesh.processor.swagger.ApiDetails
import java.io.File

class Swagger3ProcessorTest {
    @Test
    fun `should merge by tags`() {
        val openAPI = Swagger3Processor.fromFile(File(javaClass.classLoader.getResource("v3-sample.yaml").file))!!
        val processor = Swagger3Processor(openAPI)
        val result = processor.mergeByTags()
        assertEquals(1, result.size)
        val expected = """Users
GET /list Returns a list of users.
""".trimIndent()
        assertEquals(expected, ApiDetails.formatApiDetailsByTag(result)[0].toString())
    }
}