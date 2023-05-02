package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
open class PostmanItem {
    open var name: String? = null
    open val item: List<PostmanItem>? = null
    open var event: List<PostmanEvent>? = null
    open var request: PostmanRequest? = null
    open var response: List<PostmanResponse>? = null
    var protocolProfileBehavior: PostmanProtocolProfileBehavior? = null
}

@Serializable
class PostmanProtocolProfileBehavior {
    var disableBodyPruning: Boolean? = null
}

