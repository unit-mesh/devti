package cc.unitmesh.verfier.sql

import net.sf.jsqlparser.parser.CCJSqlParserUtil


class SqlParser {
    companion object {
        fun isCorrect(sqlText: String): Boolean {
            return try {
                CCJSqlParserUtil.parse(sqlText)
                true
            } catch (e: Exception) {
                false
            }
        }
    }

}
