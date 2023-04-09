package cc.unitmesh.importer

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger
import org.slf4j.LoggerFactory

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        // open datasets/rawdump/*.json, json by lines
        // for each line, parse json
        // for each json, create a new CodeSnippetContext
    }

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    }
}