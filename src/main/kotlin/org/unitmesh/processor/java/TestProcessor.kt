package org.unitmesh.processor.java

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.unitmesh.processor.JvmProcessor

class TestProcessor(val code: String) : JvmProcessor {
    private var cu: CompilationUnit = try {
        StaticJavaParser.parse(code)
    } catch (e: Exception) {
        throw e
    }

    fun removeLicenseInfoBeforeImport(): TestProcessor {
        cu.allComments.forEach { comment ->
            if (comment.content.contains("Licensed to the Apache Software Foundation (ASF) under one")) {
                comment.remove()
            }
        }

        return this
    }

    fun splitTests(): List<String> {
        val tests = mutableListOf<String>()
        cu.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
            cls.methods.filter{
                it.annotations.any { annotation ->
                    annotation.nameAsString == "Test"
                }
            }.map { method ->
                val test = cu.clone()
                test.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
                    cls.methods.filter { it != method }.forEach { it.remove() }
                }
                tests.add(test.toString())
            }
        }

        return tests
    }

    fun output(): String {
        return cu.toString()
    }
}
