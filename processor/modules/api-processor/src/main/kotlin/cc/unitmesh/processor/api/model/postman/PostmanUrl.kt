package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanUrl {
    var raw: String? = null
    var host: List<String>? = null
    var path: List<String>? = null
    var query: List<PostmanQuery>? = null
    var variable: List<PostmanVariable>? = null
}
