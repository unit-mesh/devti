package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanAuth {
    var type: String? = null
    var bearer: List<PostmanVariable>? = null
}
