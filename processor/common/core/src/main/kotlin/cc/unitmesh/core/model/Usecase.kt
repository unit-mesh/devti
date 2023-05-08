package cc.unitmesh.core.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json

// | 用例名称 | 前置条件 | 后置条件 | 主成功场景 | 扩展场景 |
@Serializable
data class Usecase(
    val name: String,
    val preCondition: String,
    val postCondition: String,
    val mainSuccessScenario: String,
    val extensionScenario: String,
)

object Usecases {
    fun fromString(json: String): List<Usecase> {
        return Json.decodeFromString(json)
    }

    fun toMarkdown(usecases: List<Usecase>): String {
        return buildString {
            append("| 用例名称 | 前置条件 | 后置条件 | 主成功场景 | 扩展场景 |\n")
            append("| --- | --- | --- | --- | --- |\n")
            usecases.forEach {
                append("| ${it.name} | ${it.preCondition} | ${it.postCondition} | ${it.mainSuccessScenario} | ${it.extensionScenario} |\n")
            }
        }
    }
}
