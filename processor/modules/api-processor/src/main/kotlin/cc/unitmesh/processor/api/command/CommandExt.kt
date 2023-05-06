package cc.unitmesh.processor.api.command

import cc.unitmesh.processor.api.UnitApi
import kotlinx.serialization.Serializable
import org.jetbrains.kotlinx.dataframe.DataFrame
import org.jetbrains.kotlinx.dataframe.api.rows
import org.jetbrains.kotlinx.dataframe.io.readCSV
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import kotlin.random.Random

class Command
val logger: Logger = LoggerFactory.getLogger(Command::class.java)

val GROUP_API_INSTRUCTION = "帮我设计一组 API："
val ONE_API_INSTRUCTION = "帮我设计一个银行的 API:"
fun randomInstruction(serviceName: String): String {
    val instructions = listOf(
        "展示${serviceName}的 API 应该如何设计？",
        "如何实现${serviceName}功能的 API？",
        "设计一个可以查询${serviceName}的 API。",
        "如何设计一个支持${serviceName}的 API？",
        "你会如何设计一个可以处理${serviceName}的 API？",
        "设计一个可以查询${serviceName}的 API。",
        "如何设计一个可以处理${serviceName}的 API？",
        "如何设计一个可以处理${serviceName}的 API？",
        "如何实现${serviceName}的 API？",
        "设计一个可以${serviceName}的 API。",
    )

    val random = Random.nextInt(0, instructions.size)
    return instructions[random]
}

val MIN_OUTPUT_LENGTH = 128
fun String.simplifyApi(): String = this
    .replace(" Services", "")
    .replace(" Service", "")
    .replace(" API", "")
    .replace("API", "")
    .replace("服务", "")

fun getDomainTranslate(domainFile: File): MutableMap<String, String> {
    val domainTranslation = mutableMapOf<String, String>()
    if (domainFile.exists()) {
        val englishToChinese = DataFrame.readCSV(domainFile.absolutePath)
        englishToChinese.rows().forEach { row ->
            val values = row.values() as List<String>
            val english = values[0]
            domainTranslation[english] = values[1] + "($english)" + "服务"
        }
    }

    return domainTranslation
}

fun outputMarkdown(
    markdownApiOutputDir: File,
    index: Int,
    bank: Bank,
    service: OpenApiService,
) = File(markdownApiOutputDir, "$index-${bank.name}-${service.name}.md")

@Serializable
class Bank(
    val name: String,
    val fullName: String,
    val description: String,
    val openApiService: List<OpenApiService>,
    val otherService: List<OtherService>? = null,
    val bankType: String,
)

@Serializable
class OpenApiService(
    val name: String,
    val description: String,
)

@Serializable
class OtherService(
    val name: String,
    val description: String,
)