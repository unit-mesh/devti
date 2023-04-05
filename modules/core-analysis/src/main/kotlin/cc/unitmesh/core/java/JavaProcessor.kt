package cc.unitmesh.core.java

import cc.unitmesh.core.JvmProcessor
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.ImportDeclaration
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration

val LICENSES = listOf(
    "Licensed under the Apache License,",
    "Licensed to the Apache Software Foundation (ASF) under one",
    "under the terms of the MIT License.",
    "Mozilla Public License"
)

open class JavaProcessor(open val code: String) : JvmProcessor {
    var unit: CompilationUnit

    init {
        try {
            unit = StaticJavaParser.parse(code)
        } catch (e: Exception) {
            throw e
        }
    }

    fun toShortClass(): ShortClass? {
        val cls = unit.findFirst(ClassOrInterfaceDeclaration::class.java).orElse(null) ?: return null

        val packageName = unit.packageDeclaration.map { it.nameAsString }.orElse(null)
        val fields = cls.fields?.map {
            ShortField(it.nameAsString, it.typeAsString)
        } ?: emptyList()
        val methods = cls.methods.map { method ->
            ShortMethod(
                method.nameAsString,
                method.typeAsString,
                method.parameters.map { ShortParameter(it.nameAsString, it.typeAsString) })
        }
        val constructors = cls.constructors.map { ShortParameter(it.nameAsString, it.typeAsString) }
        return ShortClass(cls.nameAsString, packageName, fields, methods, constructors)
    }

    fun packageName(): String? {
        return unit.packageDeclaration.map { it.nameAsString }.orElse(null)
    }

    fun removeLicenseInfoBeforeImport(): JavaProcessor {
        unit.allComments.forEach { comment ->
            LICENSES.forEach { license ->
                if (comment.content.contains(license)) {
                    comment.remove()
                }
            }
        }

        return this
    }

    fun removePackage(): JavaProcessor {
        unit.packageDeclaration.ifPresent { it.remove() }
        return this
    }

    fun removeAllImport(): JavaProcessor {
        unit.findAll(ImportDeclaration::class.java).forEach { it.remove() }
        return this
    }

    fun splitMethods(): MutableMap<String, String> {
        // map to method name and method body
        val results = mutableMapOf<String, String>()
        unit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
            cls.methods.map { method ->
                val unit = unit.clone()
                unit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
                    cls.methods.filter { it != method }.forEach { it.remove() }
                }
                results[method.nameAsString] = unit.toString()
            }
        }

        return results
    }
}

private val FieldDeclaration.nameAsString: String
    get() = this.variables.first().nameAsString
private val FieldDeclaration.typeAsString: String
    get() = this.elementType.toString()

private val ConstructorDeclaration.typeAsString: String
    get() = this.parameters.joinToString(", ") { it.typeAsString }
