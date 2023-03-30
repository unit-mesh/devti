package org.unitmesh.processor.java

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class JavaProcessorTest {
    @Test
    fun `search spring class`() {
        val code = """
            package org.unitmesh.processor.java;
            
            import org.springframework.web.bind.annotation.RequestMapping;
            import org.springframework.web.bind.annotation.RestController;
            
            @RestController
            public class JavaProcessor {
                @RequestMapping("/test")
                public String test() {
                    return "test";
                }
            }
        """.trimIndent()
        val processor = JavaProcessor(code)
        processor.isSpringController() shouldBe true
    }
}