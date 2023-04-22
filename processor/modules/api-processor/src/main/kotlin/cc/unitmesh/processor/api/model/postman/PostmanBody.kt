package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanBody {
    var mode: String? = null
    var raw: String? = null
    var urlencoded: List<PostmanUrlEncoded>? = null
}
