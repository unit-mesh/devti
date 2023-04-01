package org.unitmesh.processor.java

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr
import org.unitmesh.processor.JvmProcessor

class JavaProcessor(val code: String) : JvmProcessor {
    private var unit: CompilationUnit = StaticJavaParser.parse(code)
    fun isSpringController(): Boolean {
        for (annotationExpr in unit.findFirst(ClassOrInterfaceDeclaration::class.java).orElseThrow().annotations) {
            if (annotationExpr is SingleMemberAnnotationExpr || annotationExpr is MarkerAnnotationExpr) {
                val name = annotationExpr.name
                if (name.identifier == "RestController" || name.identifier == "Controller") {
                    // Check if the class contains at least one method with a @RequestMapping annotation
                    return unit.findFirst(MethodDeclaration::class.java)
                        .filter { m -> m.getAnnotationByName("RequestMapping").isPresent() }
                        .isPresent
                }
            }
        }

        return false
    }

    fun isAnnotationWith(name: String): Boolean {
        return unit.findFirst(ClassOrInterfaceDeclaration::class.java).orElseThrow().annotations.any { it.name.identifier == name }
    }

    fun toShortClass(): ShortClass? {
        val cls = unit.findFirst(ClassOrInterfaceDeclaration::class.java).orElse(null) ?: return null

        val packageName = unit.packageDeclaration.map { it.nameAsString }.orElse(null)
        val fields = cls.fields?.map {
            ShortField(it.nameAsString, it.typeAsString)
        } ?: emptyList()
        val methods = cls.methods.map { ShortMethod(it.nameAsString, it.typeAsString, it.parameters.map { ShortParameter(it.nameAsString, it.typeAsString) }) }
        val constructors = cls.constructors.map { ShortParameter(it.nameAsString, it.typeAsString) }
        return ShortClass(cls.nameAsString, packageName, fields, methods, constructors)
    }
}

private val FieldDeclaration.nameAsString: String
    get() = this.variables.first().nameAsString
private val FieldDeclaration.typeAsString: String
    get() = this.elementType.toString()

private val ConstructorDeclaration.typeAsString: String
    get() = this.parameters.joinToString(", ") { it.typeAsString }
