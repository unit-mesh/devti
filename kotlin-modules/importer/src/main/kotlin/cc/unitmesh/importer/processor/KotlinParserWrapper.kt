package cc.unitmesh.importer.processor

import ktlint.analysis.CodeInfo
import ktlint.analysis.KOTLIN_PSI_FILE_FACTORY_PROVIDER
import ktlint.analysis.KtLintParseException
import ktlint.analysis.UTF8_BOM
import ktlint.analysis.buildPositionInTextLocator
import org.jetbrains.kotlin.com.intellij.lang.FileASTNode
import org.jetbrains.kotlin.com.intellij.psi.PsiElement
import org.jetbrains.kotlin.com.intellij.psi.PsiErrorElement
import org.jetbrains.kotlin.idea.KotlinLanguage
import org.jetbrains.kotlin.psi.KtFile

class KotlinParserWrapper private constructor(
    val codeInfo: CodeInfo,
    val rootNode: FileASTNode,
) {
    companion object {
        fun createUnitContext(codeInfo: CodeInfo): KotlinParserWrapper {
            val psiFileFactory = KOTLIN_PSI_FILE_FACTORY_PROVIDER.getKotlinPsiFileFactory(true)

            val normalizedText = normalizeText(codeInfo.content)
            val positionInTextLocator = buildPositionInTextLocator(normalizedText)
            val psiFileName =
                codeInfo.fileName
                    ?: if (codeInfo.script) {
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

            return KotlinParserWrapper(
                codeInfo,
                rootNode,
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