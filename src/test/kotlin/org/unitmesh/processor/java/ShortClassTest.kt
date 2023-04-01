package org.unitmesh.processor.java

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ShortClassTest {
    @Test
    fun `test short class to string`() {
        val shortClass = ShortClass(
            className = "TestClass",
            packageName = "org.unitmesh.processor",
            fields = listOf(
                ShortField(
                    fieldName = "field1",
                    dataType = "String",
                ),
                ShortField(
                    fieldName = "field2",
                    dataType = "Int",
                ),
            ),
            methods = listOf(
                ShortMethod(
                    methodName = "method1",
                    returnType = "String",
                    parameters = listOf(
                        ShortParameter(
                            parameterName = "param1",
                            dataType = "String",
                        ),
                        ShortParameter(
                            parameterName = "param2",
                            dataType = "Int",
                        ),
                    ),
                ),
                ShortMethod(
                    methodName = "method2",
                    returnType = "Int",
                ),
            ),
            constructors = listOf(
                ShortParameter(
                    parameterName = "param1",
                    dataType = "String",
                ),
                ShortParameter(
                    parameterName = "param2",
                    dataType = "Int",
                ),
            ),
        )
        val expected = """
org.unitmesh.processor.TestClass(String, Int)
- fields: field1:String, field2:Int
- methods: method1(String, Int): String, method2(): Int
        """.trimIndent()
        assertEquals(expected, shortClass.toString())
    }
}