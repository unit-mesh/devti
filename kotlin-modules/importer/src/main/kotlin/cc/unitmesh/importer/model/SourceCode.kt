package cc.unitmesh.importer.model

import org.jetbrains.exposed.sql.Table


object SourceCode : Table() {
    val id = integer("id").autoIncrement()
    val identifierName = varchar("package_name", 255).default("")
    val repoName = varchar("repo_name", 255)
    val path = varchar("path", 255)
    val size = varchar("size", 255)
    val content = text("content")
    val license = varchar("license", 255)
    val copies = varchar("copies", 255)

    override val primaryKey = PrimaryKey(id)
}
