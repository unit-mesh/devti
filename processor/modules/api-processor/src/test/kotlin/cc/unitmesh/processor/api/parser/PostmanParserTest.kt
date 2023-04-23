package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.model.postman.PostmanReader
import org.junit.jupiter.api.Test
import java.io.File

class PostmanParserTest {
    val file = javaClass.getResource("/openapi/CircleCI.postman_collection.json")!!
    private val text = file.readText()

    @Test
    fun should_print_out() {
        val postmanReader = PostmanReader()
        val collection = postmanReader.readCollectionFile(File(file.toURI()).absolutePath)
        val postmanParser = PostmanParser()
        postmanParser.parse(collection)
    }

}