package org.unitmesh.processor.java

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class JavaProcessorTest {
    private val SampleClass = """
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
            """

    @Test
    fun `search spring class`() {
        val code = SampleClass.trimIndent()
        val processor = JavaProcessor(code)
        processor.isSpringController() shouldBe true
    }

    @Test
    fun `to short class`() {
        val code = SampleClass.trimIndent()
        val processor = JavaProcessor(code)
        val shortClass = processor.toShortClass()
        val expected = """
org.unitmesh.processor.java.JavaProcessor()
- methods: test(): String
        """.trimIndent()
        shortClass.toString() shouldBe expected
    }
}