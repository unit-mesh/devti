package cc.unitmesh.core.java

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration

class TestProcessor(code: String) : JavaProcessor(code) {
    fun splitTests(): List<String> {
        val tests = mutableListOf<String>()
        unit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
            cls.methods.filter {
                it.annotations.any { annotation ->
                    annotation.nameAsString == "Test"
                }
            }.map { method ->
                val test = unit.clone()
                test.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
                    cls.methods.filter { it != method }.forEach { it.remove() }
                }
                tests.add(test.toString())
            }
        }

        return tests
    }

    fun output(): String {
        return unit.toString()
    }
}
