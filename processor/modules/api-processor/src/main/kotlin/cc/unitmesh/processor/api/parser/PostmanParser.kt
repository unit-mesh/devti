package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.model.postman.PostmanCollection
import cc.unitmesh.processor.api.model.postman.PostmanEnvironment
import cc.unitmesh.processor.api.model.postman.PostmanFolder
import cc.unitmesh.processor.api.model.postman.PostmanItem
import cc.unitmesh.processor.api.model.postman.PostmanUrl
import cc.unitmesh.processor.api.model.postman.PostmanVariables

class PostmanParser {
    private val `var`: PostmanVariables = PostmanVariables(PostmanEnvironment())
    fun parse(collection: PostmanCollection) {
        for (item in collection.item!!) {
            parseFolder(item, item.name)
        }
    }

    private fun parseFolder(item: PostmanFolder, folderName: String?) {
        if (item.item != null) {
            for (subItem in item.item!!) {
                parseItem(subItem, folderName, item.name)
            }
        }
    }

    private fun parseItem(subItem: PostmanItem, folderName: String?, itemName: String?) {
        if (subItem.item != null) {
            for (subSubItem in subItem.item) {
                parseItem(subSubItem, folderName, subSubItem.name)
            }
        }

        if (subItem.request != null) {
            formatOutput(subItem, folderName, itemName)
        }
    }

    private fun formatOutput(
        subItem: PostmanItem,
        folderName: String?,
        itemName: String?
    ) {
        val request = subItem.request!!
//        val url: PostmanUrl = request.url
        val method = request.method!!
        val body = request.body
        val description = request.description
        val name = subItem.name
        val folder = folderName
        val item = itemName

        println("folder: $folder - name: $name")

        // simplify the description
        if (description != null) {
            println("description: ${description.replace("\n", " ")}")
        }

        var uri = request.getUrl(`var`)
        // if uri startsWith `UNDEFINED` then remove UNDEFINED
        if (uri?.startsWith("UNDEFINED") == true) {
            uri = uri.substring(9)
        }

        println("$method $uri")
        val headers = request.getHeaders(`var`)
        if (headers.isNotEmpty()) {
            for (header in headers) {
                print("${header.key}: ${header.value}\n")
            }
        }

        val responses = subItem.response
        // print response in one-line
        if (responses != null) {
            for (response in responses) {
                print("${response.code} ${response.name}\n")
            }
        }

        // {auth type}: {accessToken, tokenType, addTokenTo}
        if (request.auth != null) {
            val auth = request.auth!!
            println(auth.format())
        }
        // description
        // method uri
        // request headers
        // response body
    }
}

