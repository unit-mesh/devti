package cc.unitmesh.importer

import cc.unitmesh.importer.filter.CodeSnippetContext
import cc.unitmesh.importer.filter.KotlinCodeProcessor
import cc.unitmesh.importer.model.CodeSnippet
import cc.unitmesh.importer.model.RawDump
import cc.unitmesh.importer.model.SourceCodeTable
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import ktlint.analysis.KtLintParseException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.batchInsert
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.kotlinx.dataframe.api.toDataFrame
import org.jetbrains.kotlinx.dataframe.io.writeArrowFeather
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = Importer()
    .subcommands(Arrow(), Sqlite(), Analysis())
    .main(args)


val logger: Logger = LoggerFactory.getLogger(Importer::class.java)

class Importer : CliktCommand() {
    override fun run() = Unit
}

// not working well
class Arrow : CliktCommand(help = "Initialize the database") {
    override fun run() {
        logger.info("Convert to Arrow")
        val codes: List<RawDump> = readDumpLists()

        val dfs = codes.toDataFrame()
        dfs.writeArrowFeather(File("datasets" + File.separator + "rawdump.arrow"))
    }
}

fun readDumpLists(): List<RawDump> {
    val jsonFiles = File("datasets" + File.separator + "rawdump").walkTopDown().filter { file ->
        file.name.endsWith(".json")
    }.toList()

    val codes: List<RawDump> = jsonFiles.flatMap(File::readLines).map(Json.Default::decodeFromString)

    return codes
}

class Sqlite : CliktCommand(help = "Initialize the database") {
    override fun run() {
        logger.info("Initialize the database")
        Database.connect("jdbc:sqlite:datasets/data.db", "org.sqlite.JDBC")

        val codes: List<RawDump> = readDumpLists()

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(SourceCodeTable)

            SourceCodeTable.batchInsert(codes) {
                this[SourceCodeTable.repoName] = it.repo_name
                this[SourceCodeTable.path] = it.path
                this[SourceCodeTable.identifierName] = it.identifierName()
                this[SourceCodeTable.copies] = it.copies
                this[SourceCodeTable.size] = it.size
                this[SourceCodeTable.content] = it.content
                this[SourceCodeTable.license] = it.license
            }
        }
    }
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
                val snippet: CodeSnippetContext
                try {
                    snippet = CodeSnippetContext.createUnitContext(code.toCode())
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

        val results: MutableList<CodeSnippet> = mutableListOf();
        val dumpList = Json.decodeFromString<List<RawDump>>(outputFile.readText())
        dumpList.forEach { rawDump ->
            // split methods
            val snippet: CodeSnippetContext
            try {
                snippet = CodeSnippetContext.createUnitContext(rawDump.toCode())
            } catch (e: KtLintParseException) {
                return@forEach
            }

            val processor = KotlinCodeProcessor(snippet.rootNode, rawDump.content)
            processor.allClassNodes().forEach { classNode ->
                val packageName = processor.packageName()
                val className = processor.className(classNode)

                val methods = processor.splitClassMethodsToManyClass(classNode)
                val imports = processor.allImports()

                methods.map { method ->
                    val content = method.text
                    val size = method.text.length

                    if (size > 2048) {
                        logger.info("size too large: ${rawDump.path}")
                        return@map null
                    }

                    val returnType = processor.fullReturnType(method, processor.allImports())

                    results.add(
                        CodeSnippet(
                            identifierName = """$packageName.$className""",
                            content = content,
                            path = rawDump.path + "#" + method.text.hashCode(),
                            size = size,
                            imports = imports,
                            requiredType = listOf(returnType),
                        )
                    )
                }
            }
        }

        splitFile.writeText(Json.Default.encodeToString(results))

        logger.info("Analysis finished")
    }
}

class GenerateType : CliktCommand(help = "Generate TypeItem") {
    override fun run() {
        val snippets: List<CodeSnippet> = Json.decodeFromString(splitFile.readText())
    }
}