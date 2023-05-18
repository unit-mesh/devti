package cc.unitmesh.verifier.markdown

import org.commonmark.ext.gfm.tables.TableCell
import org.commonmark.ext.gfm.tables.TableHead
import org.commonmark.ext.gfm.tables.TableRow
import org.commonmark.node.AbstractVisitor
import org.commonmark.node.CustomNode
import org.commonmark.node.Node
import org.commonmark.node.Text
import org.commonmark.parser.Parser

class UsecaseParser {
    private val parser: Parser = createParser()

    fun filterTableColumn(markdown: String, headerName: String): List<String> {
        val node = parser.parse(markdown)
        val visitor = TableHeaderVisitor()
        node.accept(visitor)
        val headers = visitor.headers
        val columnIndex = headers.indexOf(headerName)

        if (columnIndex == -1) {
            return emptyList()
        }

        val filter = TableColumnFilter(columnIndex)
        node.accept(filter)
        return filter.values
    }
}

internal class TableColumnFilter(private val columnIndex: Int) : AbstractVisitor() {
    val values = mutableListOf<String>()

    // | a | b | c |
    // |---|---|---| <--- head line
    private var isBeforeHeadLine = true

    override fun visit(customNode: CustomNode?) {
        super.visit(customNode)

        // | a | b | c |
        // |---|---|---|
        // | col | .. | .. |
        when (customNode) {
            is TableHead -> {
                isBeforeHeadLine = false
            }

            is TableRow -> {
                // skip header
                if (isBeforeHeadLine) {
                    return
                }

                var node: Node? = customNode.getFirstChild()
                var index = 0
                while (node != null) {
                    if (node is TableCell) {
                        if (index == columnIndex) {
                            values += node.literal()
                        }
                    }

                    val next: Node = node.next ?: break
                    node = next
                    index += 1
                }
            }
        }
    }
}

private fun TableCell.literal(): String {
    if (this.firstChild is Text) {
        return (this.firstChild as Text).literal
    }

    return ""
}
