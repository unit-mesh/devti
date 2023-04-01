package org.unitmesh.processor.java

import kotlinx.serialization.Serializable

@Serializable
data class ShortClass(
    val className: String,
    val packageName: String?,
    val fields: List<ShortField> = listOf(),
    val methods: List<ShortMethod> = listOf(),
    val constructors: List<ShortParameter> = listOf()
) {
    override fun toString(): String {
        val classInfo =
            """${packageName?.let { "$it." } ?: ""}$className(${constructors.joinToString(", ") { it.dataType }})"""
        val fieldInfo =
            if (fields.isNotEmpty()) "\n- fields: ${fields.joinToString(", ") { it.fieldName + ":" + it.dataType }}"
            else ""
        val methodInfo =
            if (methods.isNotEmpty()) "\n- methods: ${methods.joinToString(", ") { it.toString() }}"
            else ""

        return classInfo + fieldInfo + methodInfo
    }
}

@Serializable
data class ShortField(
    val fieldName: String,
    val dataType: String,
)

@Serializable
data class ShortMethod(
    val methodName: String,
    val returnType: String,
    val parameters: List<ShortParameter> = listOf(),
) {
    override fun toString(): String {
        return "$methodName(${parameters.joinToString(", ") { it.dataType }}): $returnType"
    }
}

@Serializable
data class ShortParameter(
    val parameterName: String,
    val dataType: String,
)
