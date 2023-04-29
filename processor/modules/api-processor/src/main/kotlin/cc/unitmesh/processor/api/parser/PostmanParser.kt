package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.base.ApiDetail
import cc.unitmesh.processor.api.base.Parameter
import cc.unitmesh.processor.api.base.Request
import cc.unitmesh.processor.api.base.Response
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

    fun formatOutput(
        subItem: PostmanItem,
        folderName: String?,
        itemName: String?
    ): ApiDetail {
        val request = subItem.request!!
        val url: PostmanUrl? = request.url
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


        val responses: MutableList<Response> = mutableListOf()
        val originResponse = subItem.response
        if (originResponse != null) {
            for (resp in originResponse) {
                val code = resp.code ?: 0
                val body = resp.body
                val parameters: MutableList<Parameter> = mutableListOf()
                if (body != null) {

                }
                responses.add(Response(code, parameters))
            }
        }

        val req = Request(
            parameters = url?.query?.map {
                Parameter(it.key ?: "", it.value ?: "")
            } ?: listOf(),
            body = body?.formdata?.map { Parameter(it.key ?: "", it.value ?: "") } ?: listOf(),
        )

        return ApiDetail(
            method = method,
            path = uri ?: "",
            description = description?.replace("\n", " ") ?: "",
            operationId = name ?: "",
            tags = listOf(folder ?: "", item ?: ""),
            request = req,
        )
    }
}

