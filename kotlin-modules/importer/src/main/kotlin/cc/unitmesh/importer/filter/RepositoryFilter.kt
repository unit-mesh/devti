package cc.unitmesh.importer.filter

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

class RepositoryFilter(val rootNode: FileASTNode, val sourceCode: String) {
    fun allMethodHasAnnotation(annotationName: String): Boolean {
        if (!sourceCode.contains("@${annotationName}")) {
            return false
        }

        val allMethods: List<ASTNode> = rootNode.allClasses().flatMap {
            it.allMethods()
        }

        val modifiers = allMethods.mapNotNull {
            it.findChildByType(KtNodeTypes.MODIFIER_LIST)
        }

        val annotations = modifiers.mapNotNull {
            it.findChildByType(KtNodeTypes.ANNOTATION_ENTRY)
        }

        val callees = annotations.flatMap {
            it.children()
        }.filter { it.elementType == KtNodeTypes.CONSTRUCTOR_CALLEE }

        return callees.any {
            it.text == annotationName
        }
    }

    fun getMethodByAnnotationName(annotationName: String): List<ASTNode> {
        return rootNode
            .children()
            .filter { it.elementType == KtNodeTypes.FUN }
            .map { funNode ->
                funNode
                    .findChildByType(KtNodeTypes.ANNOTATION_ENTRY)
                    ?.takeIf { it.text.contains("@$annotationName") }
                    ?.let { funNode }
            }.filterNotNull().toList()
    }
}

fun FileASTNode.allClasses(): List<ASTNode> {
    return this.children()
        .filter { it.elementType == KtNodeTypes.CLASS }
        .toList()
}

fun ASTNode.allMethods(): List<ASTNode> {
    return this.findChildByType(KtNodeTypes.CLASS_BODY)
        ?.children()
        ?.filter { it.elementType == KtNodeTypes.FUN }
        ?.toList() ?: listOf()
}