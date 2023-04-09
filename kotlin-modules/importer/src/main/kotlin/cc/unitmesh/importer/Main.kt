package cc.unitmesh.importer

import cc.unitmesh.importer.model.RawDump
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        val jsonFiles = File("datasets" + File.separator + "rawdump").walkTopDown().filter { file ->
            file.name.endsWith(".json")
        }.toList()

        val codes: List<RawDump> = jsonFiles.flatMap(File::readLines).map(Json.Default::decodeFromString)
        println(codes.size)
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    }
}