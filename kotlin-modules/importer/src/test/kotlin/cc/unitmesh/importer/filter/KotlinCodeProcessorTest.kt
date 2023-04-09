package cc.unitmesh.importer.filter

import cc.unitmesh.importer.model.RawDump
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class KotlinCodeProcessorTest {
    val sourceCode =
        """{"repo_name":"KcgPrj/HouseHoldAccountBook","path":"src/main/kotlin/jp/ac/kcg/repository/ItemRepository.kt","copies":"1","size":"584","content":"package jp.ac.kcg.repository\n\nimport jp.ac.kcg.domain.Item\nimport jp.ac.kcg.domain.User\nimport org.springframework.data.jpa.repository.JpaRepository\nimport org.springframework.data.jpa.repository.Query\nimport org.springframework.data.repository.query.Param\nimport java.time.LocalDate\n\ninterface ItemRepository: JpaRepository\u003cItem, Long\u003e {\n\n    @Query(\"select i from Item i where i.user = :user and :before \u003c= i.receiptDate and i.receiptDate \u003c= :after\")\n    fun searchItems(@Param(\"user\") user: User, @Param(\"before\") before: LocalDate, @Param(\"after\") after: LocalDate): List\u003cItem\u003e\n}\n","license":"mit"}"""
    lateinit var unitContext: CodeSnippetContext
    lateinit var dump: RawDump

    @BeforeEach
    fun setUp() {
        dump = RawDump.fromString(sourceCode)
        unitContext = CodeSnippetContext.createUnitContext(dump.toCode())

    }

    @Test
    fun should_filter_from_jpa() {
        val filter = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        filter.allMethodHasAnnotation("Query") shouldBe true
    }

    @Test
    fun should_get_method_name() {
        val filter = KotlinCodeProcessor(unitContext.rootNode, dump.content)
        filter.allMethodHasAnnotation("Query") shouldBe true

        val nodes = filter.getMethodByAnnotationName("Query")
        nodes.size shouldBe 1
    }
}