package cc.unitmesh.verifier.markdown

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TableHeaderVisitorTest {
    @Test
    fun should_return_correct_headers_when_given_a_markdown() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
        """.trimIndent()

        val node = MarkdownVerifier.createParser().parse(markdown)
        val visitor = TableHeaderVisitor()
        node.accept(visitor)

        assertEquals(3, visitor.header.size)
        assertEquals("a", visitor.header[0])
        assertEquals("b", visitor.header[1])
        assertEquals("c", visitor.header[2])
    }
}