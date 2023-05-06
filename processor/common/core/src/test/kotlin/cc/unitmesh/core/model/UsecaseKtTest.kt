package cc.unitmesh.core.model

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class UsecaseKtTest {
    @Test
    fun `should parse usecase from string`() {
        val json = """
            [{
                "name": "usecase name",
                "preCondition": "pre condition",
                "postCondition": "post condition",
                "mainSuccessScenario": "main success scenario",
                "extensionScenario": "extension scenario"
            }]
        """.trimIndent()

        val usecase = Usecase(
            name = "usecase name",
            preCondition = "pre condition",
            postCondition = "post condition",
            mainSuccessScenario = "main success scenario",
            extensionScenario = "extension scenario"
        )

        assertEquals(listOf(usecase), Usecases.fromString(json))
    }

    @Test
    fun `should convert usecases to markdown`() {
        val usecases = listOf(
            Usecase(
                name = "usecase name",
                preCondition = "pre condition",
                postCondition = "post condition",
                mainSuccessScenario = "main success scenario",
                extensionScenario = "extension scenario"
            )
        )

        val markdown = """
| 用例名称 | 前置条件 | 后置条件 | 主成功场景 | 扩展场景 |
| --- | --- | --- | --- | --- |
| usecase name | pre condition | post condition | main success scenario | extension scenario |

""".trimIndent()

        Usecases.toMarkdown(usecases).also {
            assertEquals(markdown, it)
        }
    }
}
