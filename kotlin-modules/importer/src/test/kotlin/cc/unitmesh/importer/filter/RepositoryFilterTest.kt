package cc.unitmesh.importer.filter

import cc.unitmesh.importer.model.RawDump
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class RepositoryFilterTest {
    @Test
    fun should_filter_from_jpa() {
        val sourceCode = """{"repo_name":"KcgPrj/HouseHoldAccountBook","path":"src/main/kotlin/jp/ac/kcg/repository/ItemRepository.kt","copies":"1","size":"584","content":"package jp.ac.kcg.repository\n\nimport jp.ac.kcg.domain.Item\nimport jp.ac.kcg.domain.User\nimport org.springframework.data.jpa.repository.JpaRepository\nimport org.springframework.data.jpa.repository.Query\nimport org.springframework.data.repository.query.Param\nimport java.time.LocalDate\n\ninterface ItemRepository: JpaRepository\u003cItem, Long\u003e {\n\n    @Query(\"select i from Item i where i.user = :user and :before \u003c= i.receiptDate and i.receiptDate \u003c= :after\")\n    fun searchItems(@Param(\"user\") user: User, @Param(\"before\") before: LocalDate, @Param(\"after\") after: LocalDate): List\u003cItem\u003e\n}\n","license":"mit"}"""
        val dump = RawDump.fromString(sourceCode)
        val unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

        val filter = RepositoryFilter(unitContext.rootNode)
        filter.allMethodHasAnnotation("Query") shouldBe true
    }
}