package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanEvent {
    var listen: String? = null
    var script: PostmanScript? = null
}
