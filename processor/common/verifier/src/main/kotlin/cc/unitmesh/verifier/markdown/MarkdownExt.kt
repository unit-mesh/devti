package cc.unitmesh.verifier.markdown

import org.commonmark.ext.gfm.tables.TablesExtension
import org.commonmark.parser.Parser

fun createParser(): Parser {
    return Parser.builder()
        .extensions(listOf(TablesExtension.create()))
        .build()
}