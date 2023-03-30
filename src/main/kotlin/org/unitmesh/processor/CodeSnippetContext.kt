package org.unitmesh.processor

import com.pinterest.ktlint.rule.Code
import com.pinterest.ktlint.rule.KotlinPsiFileFactoryProvider
import com.pinterest.ktlint.rule.KtLintParseException
import com.pinterest.ktlint.rule.buildPositionInTextLocator
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile

internal typealias LineAndColumn = Pair<Int, Int>

internal const val UTF8_BOM = "\uFEFF"

private val KOTLIN_PSI_FILE_FACTORY_PROVIDER = KotlinPsiFileFactoryProvider()

class CodeSnippetContext private constructor(
    val code: Code,
    val rootNode: FileASTNode,
    val positionInTextLocator: (offset: Int) -> LineAndColumn,
) {
    fun functionByName(functionName: String): ASTNode? {
        val functionNode = rootNode
            .findChildByType(KtNodeTypes.FUN)
            ?.takeIf { it.text.contains("fun $functionName") }

        return functionNode
    }

    companion object {
        fun createUnitContext(code: Code): CodeSnippetContext {
            val psiFileFactory = KOTLIN_PSI_FILE_FACTORY_PROVIDER.getKotlinPsiFileFactory(true)

            val normalizedText = normalizeText(code.content)
            val positionInTextLocator = buildPositionInTextLocator(normalizedText)
            val psiFileName =
                code.fileName
                    ?: if (code.script) {
                        "File.kts"
                    } else {
                        "File.kt"
                    }
            val psiFile =
                psiFileFactory.createFileFromText(
                    psiFileName,
                    KotlinLanguage.INSTANCE,
                    normalizedText,
                ) as KtFile
            psiFile
                .findErrorElement()
                ?.let { errorElement ->
                    val (line, col) = positionInTextLocator(errorElement.textOffset)
                    throw KtLintParseException(line, col, errorElement.errorDescription)
                }

            val rootNode = psiFile.node

            return CodeSnippetContext(
                code,
                rootNode,
                positionInTextLocator,
            )
        }

        private fun normalizeText(text: String): String {
            return text
                .replace("\r\n", "\n")
                .replace("\r", "\n")
                .replaceFirst(UTF8_BOM, "")
        }

        private fun PsiElement.findErrorElement(): PsiErrorElement? {
            if (this is PsiErrorElement) {
                return this
            }
            this.children.forEach { child ->
                val errorElement = child.findErrorElement()
                if (errorElement != null) {
                    return errorElement
                }
            }
            return null
        }
    }
}