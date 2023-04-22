package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanAuth {
    // auth type: "oauth2",
    var type: String? = null
    var bearer: List<PostmanVariable>? = null
    var oauth2: List<PostmanVariable>? = null
}
