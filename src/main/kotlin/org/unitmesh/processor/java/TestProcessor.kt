package org.unitmesh.processor.java

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import org.unitmesh.processor.JvmProcessor

class TestProcessor(val code: String) : JvmProcessor {
    private var unit: CompilationUnit = StaticJavaParser.parse(code)


    fun isJunitTest(): Boolean {
        return unit.findFirst(ClassOrInterfaceDeclaration::class.java).orElseThrow().annotations.any { it.name.identifier == "Test" }
    }

    fun removeLicenseInfoBeforeImport() {
        unit.allComments.forEach { comment ->
            if (comment.content.contains("Licensed to the Apache Software Foundation (ASF) under one")) {
                comment.remove()
            }
        }
    }

    fun output(): String {
        return unit.toString()
    }
}
