package cc.unitmesh.processor.codegen

import cc.unitmesh.core.cli.ProcessorUtils
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import cc.unitmesh.core.java.JavaProcessor
import cc.unitmesh.core.java.ShortClass
import cc.unitmesh.core.java.TestProcessor
import cc.unitmesh.core.cli.PreProcessorConfig
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import java.io.File

@Serializable
data class CodegenPrompt(
    val instruction: String,
    val input: String,
    val output: String
)

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")
        //  1. load config `processor.yml` and start to scm
        val config = ProcessorUtils.loadConfig()

        // 2. clone all repositories
        ProcessorUtils.cloneAllRepositories(config)

        // clean old datasets under datasets/origin
        val outputDir = File("datasets" + File.separator + "origin")
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()
        outputDir.walkTopDown().forEach {
            if (it.isFile) {
                it.delete()
            }
        }

        // create dir datasets/test-api
        val testApiDir = File("datasets" + File.separator + "codegen")
        if (testApiDir.exists()) {
            testApiDir.deleteRecursively()
        }

        generateTestsPrompts(testApiDir, config)

//        processTestCases()
        logger.info("Runner finished")
    }

    private fun generateTestsPrompts(testApiDir: File, config: PreProcessorConfig) {
        testApiDir.mkdirs()
        // generate prompt for test methods
        config.scm.forEach { file ->
            // 1. get all java files
            val classMap = mutableMapOf<String, ShortClass>()
            File("origindatasets").walkTopDown().forEach { file ->
                // file path should under src/main/java
                val isJavaPath = file.absolutePath.contains("src" + File.separator + "main" + File.separator + "java")
                if (file.isFile && isJavaPath && file.extension == "java") {
                    try {
                        JavaProcessor(file.readText()).toShortClass()?.let { shortClass ->
                            classMap[shortClass.name] = shortClass
                        }
                    } catch (e: Exception) {
                        logger.error("Failed to parse ${file.absolutePath}")
                        return@forEach
                    }
                }
            }

            // 2. get all test files
            File("origindatasets").walkTopDown().forEach { file ->
                // file path should under src/test/java
                val isUnderTestPath =
                    file.absolutePath.contains("src" + File.separator + "main" + File.separator + "java")
                val isJavaFile = file.name.endsWith(".java")
                if (file.isFile && isUnderTestPath && isJavaFile) {
                    val fileName = file.nameWithoutExtension
                    val targetPath = getTargetPath(fileName, "codegen")
                    val javaProcessor: JavaProcessor
                    try {
                        javaProcessor = JavaProcessor(file.readText())
                    } catch (e: Exception) {
                        logger.error("Failed to parse ${file.absolutePath}")
                        return@forEach
                    }

                    val shotClass = javaProcessor.toShortClass() ?: return@forEach

                    javaProcessor
                        .removePackage()
                        .removeAllImport()
                        .removeLicenseInfoBeforeImport()

                    javaProcessor.splitMethods().forEach { (key, value) ->
                        CodegenPrompt(
                            instruction = "Implement the method $key",
                            input = shotClass.toString(),
                            output = value
                        ).let { prompt ->
                            val output = Json.encodeToString(prompt)
                            File("$targetPath${key}.json").writeText(output)
                        }
                    }
                }
            }
        }
    }

    private fun processTestCases() {
        // 3. filter test cases
        logger.info("Start to Filter Test Cases")
        val originFileCount = File("origindatasets").walkTopDown().count { it.isFile }
        File("origindatasets").walkTopDown().forEach {
            // if a file ends with `Test.java` or `Tests.java`, then copy it to `datasets`
            if (it.isFile && (it.name.endsWith("Test.java") || it.name.endsWith("Tests.java"))) {
                val fileName = it.nameWithoutExtension
                val targetPath = getTargetPath(fileName, "origin")
                val testProcessor: TestProcessor
                try {
                    testProcessor = TestProcessor(it.readText())
                } catch (e: Exception) {
                    logger.error("Failed to parse ${it.absolutePath}")
                    return@forEach
                }

                testProcessor
                    .removePackage()
                    .removeAllImport()
                    .removeLicenseInfoBeforeImport()

                testProcessor.splitTests().forEachIndexed { index, test ->
                    File("$targetPath$index.${it.extension}").writeText(test)
                }
            }
        }
        val targetFileCount = File("datasets").walkTopDown().count { it.isFile }


        logger.info("Origin file count: $originFileCount")
        logger.info("Target file count: $targetFileCount")

    }

    private fun getTargetPath(fileName: String, targetType: String) =
        "datasets" + File.separator + targetType + File.separator + fileName

    companion object {
        val logger: Logger = LoggerFactory.getLogger(Runner::class.java)
    }
}