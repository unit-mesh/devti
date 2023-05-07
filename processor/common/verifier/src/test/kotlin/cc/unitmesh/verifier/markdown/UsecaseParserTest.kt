package cc.unitmesh.verifier.markdown

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UsecaseParserTest {
    @Test
    fun should_return_columns_value_when_given_a_usecases() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
            | 4 | 5 | 6 |
        """.trimIndent()

        val parser = UsecaseParser()
        val values = parser.filterTableColumn(markdown, "a")
        assertEquals(listOf("1", "4"), values)
    }

    // fitler by b
    @Test
    fun should_return_columns_value_when_given_a_usecases_and_column_is_b() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
            | 4 | 5 | 6 |
        """.trimIndent()

        val parser = UsecaseParser()
        val values = parser.filterTableColumn(markdown, "b")
        assertEquals(listOf("2", "5"), values)
    }

    @Test
    fun should_return_empty_list_when_given_a_usecases_and_column_not_exist() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
            | 4 | 5 | 6 |
        """.trimIndent()

        val parser = UsecaseParser()
        val values = parser.filterTableColumn(markdown, "d")
        assertEquals(emptyList<String>(), values)
    }

    @Test
    fun should_return_empty_list_when_given_a_usecases_and_column_is_empty() {
        val markdown = """
            | a | b | c |
            |---|---|---|
            | 1 | 2 | 3 |
            | 4 | 5 | 6 |
        """.trimIndent()

        val parser = UsecaseParser()
        val values = parser.filterTableColumn(markdown, "")
        assertEquals(emptyList<String>(), values)
    }
}
