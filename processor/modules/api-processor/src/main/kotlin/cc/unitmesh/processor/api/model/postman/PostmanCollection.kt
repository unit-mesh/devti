package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanCollection {
    var info: PostmanInfo? = null
    var item: List<PostmanFolder>? = null
    var folderLookup: MutableMap<String?, PostmanFolder> = HashMap()
    var variable: List<PostmanVariable>? = null

    fun init() {
        for (f in item!!) {
            folderLookup[f.name] = f
        }
    }
}
