package cc.unitmesh.importer.filter

import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.kdoc.lexer.KDocTokens
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.psiUtil.children

class KotlinCodeProcessor(private val rootNode: FileASTNode, private val sourceCode: String) {
    private var allClasses: List<ASTNode> = listOf()
    private var allMethods: List<ASTNode> = listOf()

    init {
        allClasses = rootNode.allClasses()
        allMethods = allClasses.flatMap {
            it.allMethods()
        }
    }

    fun allClassNodes(): List<ASTNode> {
        return allClasses
    }

    fun allMethodHasAnnotation(annotationName: String): Boolean {
        if (!sourceCode.contains("@${annotationName}")) {
            return false
        }

        val annotations = allAnnotations()

        val callees = annotations.flatMap {
            it.children()
        }.filter { it.elementType == KtNodeTypes.CONSTRUCTOR_CALLEE }

        return callees.any {
            it.text == annotationName
        }
    }

    private fun allAnnotations(): List<ASTNode> {
        return allMethods.flatMap(ASTNode::annotations)
    }

    fun getMethodByAnnotationName(annotationName: String): List<ASTNode> {
        if (!sourceCode.contains("@${annotationName}")) {
            return listOf()
        }

        return filterByAnnotation(allMethods, arrayOf(annotationName))
    }

    /**
     * class Repository { fun method1() {} fun method2() {} }
     * split to
     * class Repository { fun method1() {} }
     * class Repository { fun method2() {} }
     */
    fun splitClassMethodsToManyClass(classNode: ASTNode): List<ASTNode> {
        val methods = classNode.allMethods()
        return splitMethods(methods, classNode)
    }

    fun splitClassMethodsByAnnotationName(classNode: ASTNode, vararg annotationName: String): List<ASTNode> {
        val methods = filterByAnnotation(classNode.allMethods(), annotationName)
        return splitMethods(methods, allClasses.first())
    }

    private fun filterByAnnotation(funcNodes: List<ASTNode>, annotationName: Array<out String>) =
        funcNodes.filter { method ->
            val annotations = method.annotations()

            val callees = annotations.flatMap {
                it.children()
            }.filter { it.elementType == KtNodeTypes.CONSTRUCTOR_CALLEE }

            callees.any {
                annotationName.contains(it.text)
            }
        }.toList()

    private fun splitMethods(
        methods: List<ASTNode>,
        classNode: ASTNode
    ): List<ASTNode> {
        return methods.map { method ->
            val newClassNode = classNode.clone() as ASTNode
            newClassNode.removeComments()

            val classBody = newClassNode.findChildByType(KtNodeTypes.CLASS_BODY) ?: return@map newClassNode

            val children = classBody.children().toList()

            children.filter { it.elementType == KtNodeTypes.FUN }.forEach {
                if (it.text != method.text) {
                    classBody.removeChild(it)
                }
            }

            // remove KtTokens.WHITE_SPACE if it has three continuous KtTokens.WHITE_SPACE
            children.filter { it.elementType == KtTokens.WHITE_SPACE }.forEach {
                val prev = it.treePrev
                val next = it.treeNext

                if (prev != null && next != null && prev.elementType == KtTokens.WHITE_SPACE && next.elementType == KtTokens.WHITE_SPACE) {
                    classBody.removeChild(it)
                }
            }

            newClassNode
        }
    }
}

fun ASTNode.removeComments() {
    this.findChildByType(KtTokens.EOL_COMMENT)?.let {
        this.removeChild(it)
    }
    this.findChildByType(KtTokens.BLOCK_COMMENT)?.let {
        this.removeChild(it)
    }
    this.findChildByType(KDocTokens.KDOC)?.let {
        this.removeChild(it)
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

fun ASTNode.annotations(): List<ASTNode> {
    return this.findChildByType(KtNodeTypes.MODIFIER_LIST)?.children()?.filter {
        it.elementType == KtNodeTypes.ANNOTATION_ENTRY
    }?.toList() ?: listOf()
}