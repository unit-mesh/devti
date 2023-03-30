package org.unitmesh.processor.java

import com.github.javaparser.StaticJavaParser
import com.github.javaparser.ast.CompilationUnit
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration
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
}
