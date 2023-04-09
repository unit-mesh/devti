package cc.unitmesh.importer.filter

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

class RepositoryFilter(val rootNode: FileASTNode, val sourceCode: String) {
    var allClasses: List<ASTNode> = listOf()
    var allMethods: List<ASTNode> = listOf()
    init {
        allClasses = rootNode.allClasses()
        allMethods = allClasses.flatMap {
            it.allMethods()
        }
    }

    fun allMethodHasAnnotation(annotationName: String): Boolean {
        if (!sourceCode.contains("@${annotationName}")) {
            return false
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
        if (!sourceCode.contains("@${annotationName}")) {
            return listOf()
        }

        return allMethods.filter {
            val modifiers = allMethods.mapNotNull {
                it.findChildByType(KtNodeTypes.MODIFIER_LIST)
            }

            val annotations = modifiers.mapNotNull {
                it.findChildByType(KtNodeTypes.ANNOTATION_ENTRY)
            }

            val callees = annotations.flatMap {
                it.children()
            }.filter { it.elementType == KtNodeTypes.CONSTRUCTOR_CALLEE }

            callees.any {
                it.text == annotationName
            }
        }.toList()
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