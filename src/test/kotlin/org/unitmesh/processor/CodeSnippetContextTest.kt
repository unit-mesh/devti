package org.unitmesh.processor

import com.pinterest.ktlint.rule.Code
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

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
}