package org.unitmesh.processor.swagger.converter

import io.swagger.oas.models.OpenAPI
import io.swagger.parser.OpenAPIParser
import io.swagger.parser.models.SwaggerParseResult
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.io.File

class Swagger2ProcessorTest {
    fun fromFile(it: File): OpenAPI {
        val result: SwaggerParseResult =
            OpenAPIParser().readContents(it.readText(), null, null)
        val openAPI = result.openAPI
        if (result.messages != null) result.messages.forEach(System.err::println); // validation errors and warnings
        return openAPI!!
    }


    @Test
    fun `should merge by tags`() {
        val openAPI = fromFile(File(javaClass.classLoader.getResource("v2-hello-world.yml").file))
        val processor = Swagger2Processor(openAPI)

        val result = processor.mergeByTags()
        assertEquals(1, result.size)


        val expected = """Users
GET /users Returns a list of users.

""".trimIndent()
        assertEquals(expected, ApiDetails.formatApiDetailsByTag(result))
    }
}