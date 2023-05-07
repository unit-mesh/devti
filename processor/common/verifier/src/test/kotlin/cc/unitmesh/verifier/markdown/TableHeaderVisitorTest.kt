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

        val node = createParser().parse(markdown)
        val visitor = TableHeaderVisitor()
        node.accept(visitor)

        assertEquals(3, visitor.headers.size)
        assertEquals("a", visitor.headers[0])
        assertEquals("b", visitor.headers[1])
        assertEquals("c", visitor.headers[2])
    }
}