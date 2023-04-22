package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanFolder {
    var name: String? = null
    var description: String? = null
    var item: List<PostmanItem>? = null
}
