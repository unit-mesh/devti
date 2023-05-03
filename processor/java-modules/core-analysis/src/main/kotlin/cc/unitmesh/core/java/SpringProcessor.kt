package cc.unitmesh.core.java

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
import com.github.javaparser.ast.body.MethodDeclaration
import com.github.javaparser.ast.expr.MarkerAnnotationExpr
import com.github.javaparser.ast.expr.SingleMemberAnnotationExpr

class SpringProcessor(code: String) : JavaProcessor(code) {
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

    fun splitControllerMethods(): List<String> {
        val methods = mutableListOf<String>()
        unit.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
            cls.methods.filter {
                it.annotations.any { annotation ->
                    annotation.nameAsString == "RequestMapping"
                }
            }.map { method ->
                val test = unit.clone()
                test.findAll(ClassOrInterfaceDeclaration::class.java).forEach { cls ->
                    cls.methods.filter { it != method }.forEach { it.remove() }
                }
                methods.add(test.toString())
            }
        }

        return methods
    }
}
