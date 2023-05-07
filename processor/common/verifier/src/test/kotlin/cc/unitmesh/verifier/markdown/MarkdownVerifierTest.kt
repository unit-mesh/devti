package cc.unitmesh.verifier.markdown

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MarkdownVerifierTest {
    @Test
    fun should_return_true_when_given_headers() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
        """.trimIndent()

        val verifier = MarkdownVerifier()
        val result = verifier.tableVerifier(markdown, listOf("a", "b", "c"))
        assertTrue(result)
    }

    @Test
    fun should_return_true_when_given_table_inside_markdown_code_block() {
        val markdown = """
            ```markdown
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
            ```
        """.trimIndent()

        val verifier = MarkdownVerifier()
        val result = verifier.tableVerifier(markdown, listOf("a", "b", "c"))
        assertTrue(result)
    }

    @Test
    fun should_return_false_when_dont_have_table() {
        val markdown = """
            # a
            ## b
            ### c
        """.trimIndent()

        val verifier = MarkdownVerifier()
        val result = verifier.tableVerifier(markdown, listOf("a", "b", "c"))
        assertFalse(result)
    }
}