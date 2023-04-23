package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.model.postman.PostmanReader
import org.junit.jupiter.api.Test
import java.io.File

class PostmanParserTest {
    @Test
    fun should_print_out() {
        val file = javaClass.getResource("/openapi/CircleCI.postman_collection.json")!!
        val postmanReader = PostmanReader()
        val collection = postmanReader.readCollectionFile(File(file.toURI()).absolutePath)
        val postmanParser = PostmanParser()
        postmanParser.parse(collection)
    }

    @Test
    fun should_print_out_2() {
        val boxJson = javaClass.getResource("/openapi/Box.json")!!
        val postmanReader = PostmanReader()
        val collection = postmanReader.readCollectionFile(File(boxJson.toURI()).absolutePath)
        val postmanParser = PostmanParser()
        postmanParser.parse(collection)
    }
}