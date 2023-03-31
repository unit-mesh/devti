package org.unitmesh.processor.swagger.converter

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.unitmesh.processor.swagger.ApiDetails
import java.io.File

class Swagger2ProcessorTest {
    @Test
    fun `should merge by tags`() {
        val openAPI = Swagger2Processor.fromFile(File(javaClass.classLoader.getResource("v2-hello-world.yml").file))!!
        val processor = Swagger2Processor(openAPI)

        val result = processor.mergeByTags()
        assertEquals(1, result.size)


        val expected = """Users
GET /users Returns a list of users.
""".trimIndent()
        assertEquals(expected, ApiDetails.formatApiDetailsByTag(result)[0].toString())
    }

    // for wekan
    @Test
    fun `should merge by tags for wekan`() {
        val openAPI = Swagger2Processor.fromFile(File(javaClass.classLoader.getResource("v2-wekan.yml").file))!!
        val processor = Swagger2Processor(openAPI)

        val result = processor.mergeByTags()
        assertEquals(66, result.size)

        ApiDetails.formatApiDetailsByTag(result).forEach {
            println(it)
        }
    }
}