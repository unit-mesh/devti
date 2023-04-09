package cc.unitmesh.importer

import cc.unitmesh.importer.filter.CodeSnippetContext
import ktlint.analysis.Code
import org.junit.jupiter.api.Test
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class CodeSnippetContextTest {
    @Test
    fun `test create snippet`() {
        val code = Code.fromSnippet(
            content = """
                package org.unitmesh.processor

                fun main(args: Array<String>) {
                    println("Hello World!")
                }
            """.trimIndent(),
            script = false,
        )
        val context = CodeSnippetContext.createUnitContext(code)
        val mainFunc = context.functionByName("main")

        mainFunc shouldNotBe null
    }

    @Test
    fun `test find imports`() {
        val code = Code.fromSnippet(
            content = """
                package org.unitmesh.processor

                import org.junit.jupiter.api.Test

                fun main(args: Array<String>) {
                    println("Hello World!")
                }
            """.trimIndent(),
            script = false,
        )
        val context = CodeSnippetContext.createUnitContext(code)
        val imports = context.allImports()

        imports.size shouldBe 1
    }

    @Test
    fun `class names`() {
        val code = Code.fromSnippet(
            content = """
                package org.unitmesh.processor

                import org.junit.jupiter.api.Test

                class TestClass {
                    fun main(args: Array<String>) {
                        println("Hello World!")
                    }
                }
            """.trimIndent(),
            script = false,
        )
        val context = CodeSnippetContext.createUnitContext(code)
        val classNames = context.classNames()

        classNames.size shouldBe 1
        classNames[0] shouldBe "TestClass"
    }
}
