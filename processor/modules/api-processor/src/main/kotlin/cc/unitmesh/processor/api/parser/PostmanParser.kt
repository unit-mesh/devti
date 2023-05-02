package cc.unitmesh.processor.api.parser

import cc.unitmesh.processor.api.base.ApiCollection
import cc.unitmesh.processor.api.base.ApiItem
import cc.unitmesh.processor.api.base.BodyMode
import cc.unitmesh.processor.api.base.Parameter
import cc.unitmesh.processor.api.base.Request
import cc.unitmesh.processor.api.base.Response
import cc.unitmesh.processor.api.model.postman.*
import org.jetbrains.kotlin.cli.common.repl.replEscapeLineBreaks
import java.net.URI

sealed class ChildType {
    class Folder(val collection: ApiCollection) : ChildType()
    class Item(val items: List<ApiItem>) : ChildType()

}

class PostmanParser {
    private val `var`: PostmanVariables = PostmanVariables(PostmanEnvironment())
    fun parse(collection: PostmanCollection): List<ApiCollection>? {
        return collection.item?.map {
            parseFolder(it, it.name)
        }?.flatten()
    }

    private fun parseFolder(item: PostmanFolder, folderName: String?): List<ApiCollection> {
        val details: MutableList<ApiCollection> = mutableListOf()
        if (item.item != null) {
            val childTypes = item.item.map {
                parseItem(it, folderName, item.name)
            }.flatten()

            childTypes.filterIsInstance<ChildType.Folder>().forEach {
                details.add(it.collection)
            }

            val items = childTypes.filterIsInstance<ChildType.Item>().map { it.items }.flatten()
            if (items.isNotEmpty()) {
                val descriptionName = if (folderName == item.name) {
                    ""
                } else {
                    item.name ?: ""
                }

                details.add(ApiCollection(folderName ?: "", descriptionName, items))
            }
        } else if (item.request != null) {
            val apiItems = processApiItem(item as PostmanItem, folderName, item.name)?.let {
                listOf(it)
            } ?: listOf()

            val descriptionName = if (folderName == item.name) {
                ""
            } else {
                item.name ?: ""
            }

            details.add(ApiCollection(folderName ?: "", descriptionName, apiItems))
        }

        return details
    }

    private fun parseItem(item: PostmanItem, folderName: String?, itemName: String?): List<ApiCollection> {
        val details: MutableList<ApiCollection> = mutableListOf()
        if (item.item != null) {
            val childTypes = item.item!!.map {
                parseChildItem(it, folderName, item.name)
            }.flatten()

            childTypes.filterIsInstance<ChildType.Folder>().forEach {
                details.add(it.collection)
            }

            val items = childTypes.filterIsInstance<ChildType.Item>().map { it.items }.flatten()
            if (items.isNotEmpty()) {
                val descriptionName = if (folderName == item.name) {
                    ""
                } else {
                    item.name ?: ""
                }

                details.add(ApiCollection(folderName ?: "", descriptionName, items))
            }
        } else if (item.request != null) {
            val apiItems = processApiItem(item as PostmanItem, folderName, item.name)?.let {
                listOf(it)
            } ?: listOf()

            val descriptionName = if (folderName == item.name) {
                ""
            } else {
                item.name ?: ""
            }

            details.add(ApiCollection(folderName ?: "", descriptionName, apiItems))
        }

        return details
    }

    private fun parseChildItem(subItem: PostmanItem, folderName: String?, itemName: String?): List<ChildType> {
        return when {
            subItem.item != null -> {
                return subItem.item!!.map {
                    parseChildItem(it, folderName, itemName)
                }.flatten()
            }

            subItem.request != null -> {
                val apiItems = processApiItem(subItem, folderName, itemName)?.let {
                    listOf(it)
                } ?: listOf()

                listOf(ChildType.Item(apiItems))
            }

            else -> {
                listOf()
            }
        }
    }

    private fun processApiItem(
        subItem: PostmanItem,
        folderName: String?,
        itemName: String?
    ): ApiItem? {
        val request = subItem.request
        val url: PostmanUrl? = request?.url
        val method = request?.method
        val body = request?.body
        val description = request?.description
        val name = subItem.name

        var uri = request?.getUrl(`var`)
        // remove UNDEFINED and http://UNDEFINED
        uri = uri?.replace("http://UNDEFINED", "")
            ?.replace("https://UNDEFINED", "")

        // try parse URI and remove host
        try {
            val uriObj = URI(uri)
            uri = uriObj.path
        } catch (e: Exception) {
            // ignore
        }


        val responses = subItem.response?.map {
            Response(
                code = it.code ?: 0,
                parameters = listOf(),
                bodyMode = BodyMode.RAW_TEXT,
                bodyString = it.body ?: "",
            )
        }?.toList() ?: listOf()

        val req = Request(
            parameters = urlParameters(url),
            body = body?.formdata?.map { Parameter(it.key ?: "", it.value ?: "") } ?: listOf(),
        )

        if (uri?.isEmpty() == true) {
            return null
        }

        return ApiItem(
            method = method ?: "",
            path = uri ?: "",
            description = description.replaceLineBreak() ?: "",
            operationId = name ?: "",
            tags = listOf(folderName ?: "", itemName ?: ""),
            request = req,
            response = responses,
        )
    }

    private fun urlParameters(url: PostmanUrl?): List<Parameter> {
        val variable = url?.variable?.map {
            Parameter(it.key ?: "", formatValue(it.value))
        }

        val queries = url?.query?.map {
            Parameter(it.key ?: "", formatValue(it.value))
        }

        return (variable ?: listOf()) + (queries ?: listOf())
    }

    private fun formatValue(it: String?): String {
        val regex = Regex("^\\d+$")
        val boolRegex = Regex("^(true|false)$")

        return when {
            it?.matches(regex) == true -> {
                it
            }

            it?.matches(boolRegex) == true -> {
                it
            }

            (it?.length ?: 0) > 0 -> {
                "\"$it\""
            }

            else -> ""
        }
    }
}

private fun String?.replaceLineBreak(): String? {
    return this?.replEscapeLineBreaks()?.replace("\n", "")?.replace("\r", "")
}

