package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanItem {
    var name: String? = null
    var event: List<PostmanEvent>? = null
    var request: PostmanRequest? = null
}
