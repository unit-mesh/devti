package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.base.ApiDetails
import cc.unitmesh.processor.api.base.ApiProcessor
import java.io.File

class PostmanProcessor(val file: File) : ApiProcessor {
    override fun convertApi(): List<ApiDetails> {
        return listOf()
    }
}
