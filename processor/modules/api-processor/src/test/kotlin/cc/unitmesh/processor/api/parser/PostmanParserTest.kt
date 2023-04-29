package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.model.postman.PostmanReader
import cc.unitmesh.processor.api.render.MarkdownTableRender
import cc.unitmesh.processor.api.render.SimpleApiRender
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import java.io.File

class PostmanParserTest {
    @Test
    fun should_print_out() {
        val file = javaClass.getResource("/openapi/CircleCI.postman_collection.json")!!
        val postmanReader = PostmanReader()
        val collection = postmanReader.readCollectionFile(File(file.toURI()).absolutePath)
        val postmanParser = PostmanParser()
        val listList = postmanParser.parse(collection)!!

        val apiDetails = listList.flatten()
        apiDetails.size shouldBe 31

        val output = MarkdownTableRender().render(apiDetails)
        println(output)
    }

    @Test
    fun should_print_out_2() {
        val boxJson = javaClass.getResource("/openapi/Box.json")!!
        val postmanReader = PostmanReader()
        val collection = postmanReader.readCollectionFile(File(boxJson.toURI()).absolutePath)
        val postmanParser = PostmanParser()

        val listList = postmanParser.parse(collection)!!

        val apiDetails = listList.flatten()
        apiDetails.size shouldBe 5

        val output = MarkdownTableRender().render(apiDetails)
        println(output)
    }
}