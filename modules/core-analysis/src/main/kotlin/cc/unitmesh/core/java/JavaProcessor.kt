package cc.unitmesh.core.java

import cc.unitmesh.core.JvmProcessor
import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.ConstructorDeclaration
import com.github.javaparser.ast.body.FieldDeclaration

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
}

private val FieldDeclaration.nameAsString: String
    get() = this.variables.first().nameAsString
private val FieldDeclaration.typeAsString: String
    get() = this.elementType.toString()

private val ConstructorDeclaration.typeAsString: String
    get() = this.parameters.joinToString(", ") { it.typeAsString }
