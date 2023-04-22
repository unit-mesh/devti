package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanHeader {
    var key: String? = null
    var value: String? = null
    var type: String? = null
}
