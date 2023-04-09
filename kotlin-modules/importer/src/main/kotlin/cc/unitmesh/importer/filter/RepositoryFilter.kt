package cc.unitmesh.importer.filter

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.psi.psiUtil.children

class RepositoryFilter(val rootNode: FileASTNode) {
    fun allMethodHasAnnotation(annotationName: String): Boolean {
        return rootNode
            .children()
            .filter { it.elementType == KtNodeTypes.FUN }
            .all { funNode ->
                funNode
                    .findChildByType(KtNodeTypes.ANNOTATION_ENTRY)
                    ?.takeIf { it.text.contains("@$annotationName") }
                    ?.let { true }
                    ?: false
            }
    }
}
