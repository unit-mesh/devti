package cc.unitmesh.processor.api.model.postman

import kotlinx.serialization.Serializable

@Serializable
class PostmanResponse(
    val name: String,
    val originalRequest: PostmanRequest,
    val status: String,
    val code: Int,
    val header: List<PostmanHeader>,
    val cookie: List<String>,
    val body: String,
    val _postman_previewlanguage: String,
//    val data: List<Data>,
//    val links: Links,
//    val meta: Meta
) {

}
