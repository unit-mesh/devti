package cc.unitmesh.importer

import cc.unitmesh.importer.model.CodeSnippet
import cc.unitmesh.importer.model.PackageUtil
import cc.unitmesh.importer.model.RawDump
import cc.unitmesh.importer.processor.KotlinCodeProcessor
import cc.unitmesh.importer.processor.KotlinParserWrapper
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = Importer()
    .subcommands(Analysis(), Type(), Prompt())
    .main(args)

val logger: Logger = LoggerFactory.getLogger(Importer::class.java)

class Importer : CliktCommand() {
    override fun run() = Unit
}

fun readDumpLists(): List<RawDump> {
    val jsonFiles = File("datasets" + File.separator + "rawdump").walkTopDown().filter { file ->
        file.name.endsWith(".json")
    }.toList()

    return jsonFiles.flatMap(File::readLines).map(Json.Default::decodeFromString)
}

val filteredFile = "datasets" + File.separator + "filtered.json"
val splitFile = File("datasets" + File.separator + "split.json")

class Analysis : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Analysis Started")

        logger.info("Analysis Prepare filter data")

        val outputFile = File(filteredFile)
        if (!outputFile.exists()) {

            val codes: List<RawDump> = readDumpLists()

            val outputs = codes.filter { code ->
                val snippet: KotlinParserWrapper
                try {
                    snippet = KotlinParserWrapper.createUnitContext(code.toCode())
                } catch (e: KtLintParseException) {
                    return@filter false
                }

                val processor = KotlinCodeProcessor(snippet.rootNode, code.content)
                processor.getMethodByAnnotationName("Query").isNotEmpty()
            }

            outputFile.writeText(Json.Default.encodeToString(outputs))
        } else {
            logger.info("Skip analysis, because the output file already exists")
        }

        val results: MutableList<CodeSnippet> = Snippets.fromFile(outputFile)

        splitFile.writeText(Json.Default.encodeToString(results))

        logger.info("Analysis finished")
    }
}


private val typeFile = "datasets" + File.separator + "types.json"

class Type : CliktCommand(help = "Generate Type Items") {
    override fun run() {
        val snippets: List<CodeSnippet> = Json.decodeFromString(splitFile.readText())

        val types = snippets.flatMap { snippet ->
            snippet.requiredType
        }.distinct()

        // write types for debug
        File("datasets" + File.separator + "required-types.json").writeText(Json.Default.encodeToString(types))

        val rawdumpMap: Map<String, RawDump> =
            readDumpLists().associateBy { PackageUtil.pathToIdentifier(it.path) }

        val typeItems: List<RawDump> = types.map { type ->
            val rawDump = rawdumpMap[type] ?: return@map null
            rawDump.copy(path = type)
        }.filterNotNull()

        File(typeFile).writeText(Json.Default.encodeToString(typeItems))
    }
}


class Prompt : CliktCommand(help = "Generate Prompt") {
    override fun run() {
        val snippets: List<CodeSnippet> = Json.decodeFromString(splitFile.readText())
        val typesDump: List<RawDump> = Json.decodeFromString(File(typeFile).readText())

        val openAiPrompts = Snippets.toOpenAIPrompts(snippets)
        logger.info("Prompt sizes: ${openAiPrompts.size}")
        File("datasets" + File.separator + "prompts.json").writeText(Json.Default.encodeToString(openAiPrompts))

        val prompts = Snippets.toLLMPrompts(typesDump, snippets)

        logger.info("Prompt sizes: ${prompts.size}")
        File("datasets" + File.separator + "llm-prompts.json").writeText(Json.Default.encodeToString(prompts))
    }
}
