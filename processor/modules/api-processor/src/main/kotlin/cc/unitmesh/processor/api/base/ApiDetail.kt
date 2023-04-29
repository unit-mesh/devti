package cc.unitmesh.processor.api.base

import kotlinx.serialization.Serializable


@Serializable
data class ApiTagOutput(val string: String) {
    override fun toString() = string
}

@Serializable
data class Parameter(
    val name: String,
    val type: String,
) {
    override fun toString() = "$name: $type"
}

enum class BodyMode {
    RAW_TEXT,
    TYPED,
}

@Serializable
data class Request(
    val parameters: List<Parameter> = listOf(),
    val body: List<Parameter> = listOf(),
    val bodyMode: BodyMode = BodyMode.TYPED,
    val bodyString: String = ""
) {
    override fun toString(): String {
        val params = parameters.joinToString(", ") { it.toString() }
        val body = body.joinToString(", ") { it.toString() }
        if (params.isEmpty() && body.isEmpty()) return ""
        if (params.isEmpty()) return body
        if (body.isEmpty()) return params

        return "$params, ($body)"
    }
}

@Serializable
data class Response(
    val code: Int,
    val parameters: List<Parameter> = listOf(),
    var bodyMode: BodyMode = BodyMode.TYPED,
    var bodyString: String = ""
) {
    override fun toString() = "$code: {${parameters.joinToString(", ") { it.toString() }}}"
}

@Serializable
data class ApiDetail(
    val path: String,
    val method: String,
    val description: String,
    val operationId: String,
    val tags: List<String>,
    val request: Request? = null,
    val response: List<Response> = listOf(),
) {
    override fun toString(): String {
        val request = request.toString()
        val response = response.joinToString(", ") { it.toString() }
        return "$method $path $description $request $response"
    }
}