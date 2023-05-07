package cc.unitmesh.verifier.markdown

import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.node.*
import org.commonmark.parser.Parser


class MarkdownVerifier() {
    val parser: Parser = MarkdownVerifier.createParser()

    init {
        // clean up
    }

    fun tableVerifier(markdown: String, header: List<String>): Boolean {
        var content = markdown
        // 1. if there is inside code block, remove block, like: ```markdown xxx ```
        if (markdown.contains("```")) {
            val code = parseMarkdownCodeBlock(markdown)
            if (code.isEmpty()) {
                return false
            }

            content = code.first()
        }

        // 2. if no table, return false
        if (!markdown.contains("|")) {
            return false
        }

        // 3. parse table header and verify table header
        val tableHeader = parseTableHeader(content)
        if (tableHeader.size != header.size) {
            return false
        }

        tableHeader.forEachIndexed { index, s ->
            if (s != header[index]) {
                return false
            }
        }

        return true
    }

    private fun parseTableHeader(content: String): List<String> {
        val node = parser.parse(content)
        val visitor = TableHeaderVisitor()
        node.accept(visitor)
        return visitor.header
    }

    private fun parseMarkdownCodeBlock(markdown: String): List<String> {
        val node = parser.parse(markdown)
        val visitor = CodeFilter(lang = "markdown")
        node.accept(visitor)
        return visitor.code
    }

    companion object {
        fun createParser(): Parser {
            return Parser.builder()
                .extensions(listOf(TablesExtension.create()))
                .build()
        }
    }
}

internal class TableHeaderVisitor : AbstractVisitor() {
    val header = mutableListOf<String>()

    // | a | b | c |
    // |---|---|---| <--- head line
    private var isBeforeHeadLine = true
    override fun visit(customNode: CustomNode?) {
        super.visit(customNode)

        when (customNode) {
            is TableHead -> {
                isBeforeHeadLine = false
            }

            is TableCell -> {
                if (isBeforeHeadLine) {
                    header += (customNode.firstChild as Text).literal
                }
            }
        }
    }

    override fun visit(customBlock: CustomBlock?) {
        super.visit(customBlock)
    }
}

internal class CodeFilter(val lang: String) : AbstractVisitor() {
    var code = listOf<String>()

    override fun visit(fencedCodeBlock: FencedCodeBlock?) {
        if (fencedCodeBlock?.literal != null) {
            if (fencedCodeBlock.info == lang) {
                this.code += fencedCodeBlock.literal
            }
        }
    }

    override fun visit(indentedCodeBlock: IndentedCodeBlock?) {
        super.visit(indentedCodeBlock)
    }
}