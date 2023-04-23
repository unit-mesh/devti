package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanVariable {
    var key: String? = null
    var value: String? = null
    var type: String? = null
    var description: String? = null
    fun format(): String {
        return "$key=$value"
    }
}
