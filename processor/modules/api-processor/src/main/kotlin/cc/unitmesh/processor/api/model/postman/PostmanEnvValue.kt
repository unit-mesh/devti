package cc.unitmesh.processor.api.model.postman

class PostmanEnvValue {
    var key: String? = null
    var value: String? = null
    var type: String? = null
    var name: String? = null
    override fun toString(): String {
        return "[$key:$value]"
    }
}
