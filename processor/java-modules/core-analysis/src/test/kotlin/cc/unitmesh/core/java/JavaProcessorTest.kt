package cc.unitmesh.core.java

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
    fun `to short class`() {
        val code = SampleClass.trimIndent()
        val processor = JavaProcessor(code)
        val shortClass = processor.toShortClass()!!
        val expected = """
org.unitmesh.processor.java.JavaProcessor()
- methods: test(): String
        """.trimIndent()
        shortClass.toString() shouldBe expected
    }

    @Test
    fun `split methods`() {
        val code = """
            class TestProcessorTest {
                @Test
                void test1() {
                }
                
                @Test
                void test2() {
                }
            }
         """.trimIndent()

        val processor = JavaProcessor(code)
        val methods = processor.splitMethods()
        methods.size shouldBe 2
        methods["test1"] shouldBe """
            class TestProcessorTest {
            
                @Test
                void test1() {
                }
            }
            
        """.trimIndent()

        methods["test2"] shouldBe """
            class TestProcessorTest {
            
                @Test
                void test2() {
                }
            }
            
        """.trimIndent()
    }

}