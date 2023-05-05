package cc.unitmesh.processor.api

import cc.unitmesh.processor.api.base.ApiProcessor
import cc.unitmesh.processor.api.parser.PostmanProcessor
import cc.unitmesh.processor.api.swagger.Swagger3Processor
import java.io.File

object ApiProcessorDetector {
    fun detectApiProcessor(file: File, withPostman: Boolean = false, postmanOnly: Boolean = false): ApiProcessor? {
        val content = file.readText()

        if (withPostman || postmanOnly) {
            if (file.extension == "json") {
                if (content.contains("_postman_id") && content.contains("schema")) {
                    return PostmanProcessor(file)
                }
            }
        }

        if (postmanOnly) {
            return null
        }

        // if not json, yaml, yml file, skip
        if (!file.extension.matches(Regex("json|yaml|yml"))) {
            return null
        }

        return getSwaggerProcessor(file)?.let {
            return it
        }
    }
}

private fun getSwaggerProcessor(it: File): ApiProcessor? {
    try {
        val openAPI = Swagger3Processor.fromFile(it)!!
        return Swagger3Processor(openAPI)
    } catch (e: Exception) {
        logger.info("Failed to parse ${it.absolutePath}", e)
    }

    return null
}
