package org.unitmesh.processor.java

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.unitmesh.processor.JvmProcessor

//
val LICENSES = listOf(
    "Licensed under the Apache License,",
    "Licensed to the Apache Software Foundation (ASF) under one",
    "under the terms of the MIT License."
)

class TestProcessor(val code: String) : JvmProcessor {
    private var cu: CompilationUnit = try {
        StaticJavaParser.parse(code)
    } catch (e: Exception) {
        throw e
    }

    fun removeLicenseInfoBeforeImport(): TestProcessor {
        cu.allComments.forEach { comment ->
            LICENSES.forEach { license ->
                if (comment.content.contains(license)) {
                    comment.remove()
                }
            }
        }

        return this
    }

    fun removePackage(): TestProcessor {
        cu.packageDeclaration.ifPresent { it.remove() }
        return this
    }

    fun removeAllImport(): TestProcessor {
        cu.findAll(ImportDeclaration::class.java).forEach { it.remove() }
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
