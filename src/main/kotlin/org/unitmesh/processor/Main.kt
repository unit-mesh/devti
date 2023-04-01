package org.unitmesh.processor

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.Logger
import org.unitmesh.processor.java.JavaProcessor
import org.unitmesh.processor.java.ShortClass
import org.unitmesh.processor.java.TestProcessor
import org.unitmesh.processor.toolsets.GitCommandManager
import java.io.File
import kotlin.system.exitProcess

@Serializable
data class TestFilePrompt(
    val classInfo: String,
    val testMethod: String
)

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")
        //  1. load config `processor.yml` and start to scm
        val file = File("processor.yml").let {
            if (!it.exists()) {
                logger.error("Config file not found: ${it.absolutePath}")
                exitProcess(1)
            }

            it
        }

        val content = file.readText()
        val config = Yaml.default.decodeFromString(deserializer = PreProcessorConfig.serializer(), content)

        // 2. clone all repositories
        cloneAllRepositories(config)

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
        val testApiDir = File("datasets" + File.separator + "test-api")
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
                    file.absolutePath.contains("src" + File.separator + "test" + File.separator + "java")
                val isTestFile = file.name.endsWith("Test.java") || file.name.endsWith("Tests.java")
                if (file.isFile && isUnderTestPath && isTestFile) {
                    val fileName = file.nameWithoutExtension
                    val targetPath = getTargetPath(fileName, "test-api")
                    val testProcessor: TestProcessor
                    try {
                        testProcessor = TestProcessor(file.readText())
                    } catch (e: Exception) {
                        logger.error("Failed to parse ${file.absolutePath}")
                        return@forEach
                    }

                    val fullName =
                        testProcessor.packageName() + "." + fileName.removeSuffix("Test").removeSuffix("Tests")
                    // 2. check is exists in classMap
                    if (classMap.containsKey(fullName)) {
                        val shortClass = classMap[fullName]!!

                        // 3. generate prompt
                        testProcessor
                            .removePackage()
                            .removeAllImport()
                            .removeLicenseInfoBeforeImport()
                            .splitTests().forEachIndexed { index, test ->
                                TestFilePrompt(
                                    classInfo = shortClass.toString(),
                                    testMethod = test
                                ).let { prompt ->
                                    val output = Json.encodeToString(prompt)
                                    // https://help.openai.com/en/articles/4936856-what-are-tokens-and-how-to-count-them
                                    // inorder to avoid 400 error, we need to limit the length of prompt
                                    if (output.length > 3600 * 4) {
                                        logger.warn("Prompt is too long: ${output.length}, will skip it")
                                    } else {
                                        File("$targetPath$index.prompt").writeText(output)
                                    }
                                }
                            }
                    }

                }
            }
        }
    }

    private fun cloneAllRepositories(config: PreProcessorConfig) {
        logger.info("Start to Clone code from GitHub")
        config.scm.forEach {
            val path = it.repository.split("/").last()
            val targetPath = clonedPath(path)
            // if directory exits and contains .git, then skip
            if (File(targetPath).exists() && File(targetPath + File.separator + ".git").exists()) {
                logger.info("Skip $targetPath")
                return@forEach
            }

            File(targetPath).mkdirs()

            val gitCommandManager = GitCommandManager(targetPath)
            gitCommandManager.shallowClone(it.repository, it.branch)
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
                    .removeLicenseInfoBeforeImport().splitTests().forEachIndexed { index, test ->
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

    private fun clonedPath(path: String) = "origindatasets" + File.separator + path

    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(Runner::class.java)
    }
}