package cc.unitmesh.importer.filter

import ktlint.analysis.Code
import ktlint.analysis.KOTLIN_PSI_FILE_FACTORY_PROVIDER
import ktlint.analysis.KtLintParseException
import ktlint.analysis.LineAndColumn
import ktlint.analysis.UTF8_BOM
import ktlint.analysis.buildPositionInTextLocator
import org.jetbrains.kotlin.KtNodeTypes
import org.jetbrains.kotlin.com.intellij.lang.ASTNode
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.lexer.KtTokens
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.psiUtil.children

class CodeSnippetContext private constructor(
    val code: Code,
    val rootNode: FileASTNode,
    val positionInTextLocator: (offset: Int) -> LineAndColumn,
) {
    fun allImports(): List<ASTNode> {
        return rootNode
            .findChildByType(KtNodeTypes.IMPORT_LIST)
            ?.children()
            ?.toList()
            ?: emptyList()
    }

    fun classNames(): List<String> {
        return rootNode
            .children()
            .filter { it.elementType == KtNodeTypes.CLASS }
            .map { it.findChildByType(KtTokens.IDENTIFIER)?.text }
            .filterNotNull()
            .toList()
    }

    /**
     * file level function
     */
    fun topLevelFunByName(functionName: String): ASTNode? {
        return rootNode
            .findChildByType(KtNodeTypes.FUN)
            ?.takeIf { it.text.contains("fun $functionName") }
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