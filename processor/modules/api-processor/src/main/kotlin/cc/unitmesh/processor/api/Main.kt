package cc.unitmesh.processor.api

import cc.unitmesh.core.Instruction
import cc.unitmesh.processor.api.command.Workspace
import cc.unitmesh.processor.api.command.logger
import cc.unitmesh.verifier.markdown.UsecaseParser
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.arguments.argument
import com.github.ajalt.clikt.parameters.arguments.default
import com.github.ajalt.clikt.parameters.types.file
import kotlinx.serialization.json.Json
import java.io.File

fun main(args: Array<String>) = Bundling()
//    .subcommands(Prompting())
    .main(args)

class UnitApi : CliktCommand() {
    override fun run() {
        logger.info("Unit Connector Started")
    }
}

class Bundling : CliktCommand() {
    private val outputDir by argument().file().default(File("output"))

    override fun run() {
        logger.info("Bundling Started")

        val instructions: MutableList<Instruction> = mutableListOf()
        val usecaseFileNamesMap = mutableMapOf<String, String>()

        val usecaseDir = Workspace.usecases(outputDir.absolutePath)
        val usecasesInstruction: List<Instruction> = usecaseDir.listFiles()?.filter { it.isFile }?.map {
            val usecase = it.readText()
            val usecasesNames = usecasesName(usecase)
            usecaseFileNamesMap[it.name] = usecasesNames.joinToString(separator = ",")
            Instruction(
                instruction = "根据下面内容编写需求用例，返回格式：| 用例名称 | 前置条件 | 后置条件 | 主成功场景 | 扩展场景 |",
                input = usecasesNames.joinToString(separator = ","),
                output = usecase,
            )
        } ?: emptyList()

        instructions += usecasesInstruction

        val pumlDir = Workspace.puml(outputDir.absolutePath)
        val modelInstructions = usecaseDir.walk().mapNotNull { file -> createDomainModel(file, pumlDir) }
        instructions += modelInstructions

        // merge from instructions.jsonl
//        val instructionsFile = File(outputDir, "instructions.jsonl")
//        if (instructionsFile.exists()) {
//            val json = Json { ignoreUnknownKeys = true }
//            val instructionsFromFile =
//                instructionsFile.readLines().map { json.decodeFromString(Instruction.serializer(), it) }
//            instructions += instructionsFromFile
//        }

        val domainModelJsonDir = File(outputDir, "domain-json")

        // read domains-instructions.jsonl and replace instructions
        val apisInstructions = usecaseFileNamesMap.map {
            // get puml file name by it.key replace .md to .puml
            val oldInstructionFile = it.key.replace("md", "json")
            val oldInstruction =
                Json.decodeFromString(Instruction.serializer(), File(domainModelJsonDir, oldInstructionFile).readText())
            val instruction = Instruction(
                instruction = "根据下面信息，设计 API，返回格式：| API | Method | Description | Request | Response | Error Response |",
                input = it.value,
                output = oldInstruction.input,
            )

            instruction
        }
        instructions += apisInstructions

        // write instructions to bundling.jsonl
        val bundlingFile = File(outputDir, "bundling.jsonl")
        bundlingFile.writeText(
            instructions.joinToString(separator = "\n") {
                Json.encodeToString(Instruction.serializer(), it)
            },
        )
    }
}

private val USECASE_NAME = "用例名称"

private fun usecasesName(markdown: String) = UsecaseParser().filterTableColumn(markdown, USECASE_NAME)

private fun createDomainModel(file: File, pumlDir: File): Instruction? {
    if (file.isFile && file.name.endsWith(".md")) {
        // get origin puml file from pumlDir
        val pumlFile = File(pumlDir, file.name.replace("md", "puml"))
        if (!pumlFile.exists()) {
            logger.info("Skipping ${file.absolutePath}")
            return null
        }

        val content = file.readText()
        val model = pumlFile.readText()
        return Instruction(
            instruction = "根据下面信息设计领域模型，并使用 PlantUML 返回",
            input = usecasesName(content).joinToString(separator = ","),
            output = model,
        )
    }

    return null
}
