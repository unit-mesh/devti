package org.unitmesh.processor.swagger

import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger
import org.unitmesh.processor.swagger.converter.Swagger2Processor
import org.unitmesh.processor.swagger.converter.Swagger3Processor
import org.unitmesh.processor.swagger.converter.SwaggerProcessor
import java.io.File
import kotlin.system.exitProcess

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json


fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")
        val rootDir = ".." + File.separator + ".." + File.separator
        val apisDir = File(rootDir + "datasets" + File.separator + "swagger")
        if (!apisDir.exists()) {
            logger.error("APIs directory not found: ${apisDir.absolutePath}")
            exitProcess(1)
        }

        val output: List<ApiTagOutput> = apisDir.walkTopDown().filter {
            it.isFile && (it.extension == "yaml" || it.extension == "yml" || it.extension == "json")
        }.map {
            val openAPI = getProcessor(it)
            if (openAPI == null) {
                logger.error("Failed to parse ${it.absolutePath}")
                null
            } else {
                ApiDetails.formatApiDetailsByTag(openAPI.mergeByTags())
            }
        }.filterNotNull().flatten().toList()

        val outputFile = File(rootDir + "datasets" + File.separator + "swagger-merged.json")
        outputFile.writeText(Json.encodeToString(output))
    }

    private fun getProcessor(it: File): SwaggerProcessor? {
        try {
            val openAPI = Swagger2Processor.fromFile(it)!!
            return Swagger2Processor(openAPI)
        } catch (e: Exception) {
            try {
                val openAPI = Swagger3Processor.fromFile(it)!!
                return Swagger3Processor(openAPI)
            } catch (e: Exception) {
                logger.error("Failed to parse ${it.absolutePath}", e)
            }
        }

        return null
    }


    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(Runner::class.java)
    }
}
