package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanFolder {
    var item: List<PostmanItem>? = null
    var name: String? = null
    var description: String? = null
    var request: PostmanRequest? = null
    var response: List<PostmanResponse>? = null
}
