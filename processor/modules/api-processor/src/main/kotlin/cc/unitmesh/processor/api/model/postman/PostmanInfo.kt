package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanInfo {
    var _postman_id: String? = null
    var name: String? = null
    var description: String? = null
    var schema: String? = null
    var _exporter_id: String? = null
    var _collection_link: String? = null
}
