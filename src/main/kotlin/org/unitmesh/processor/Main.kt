package org.unitmesh.processor

import com.charleskorn.kaml.Yaml
import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger
import org.unitmesh.processor.java.TestProcessor
import org.unitmesh.processor.toolsets.GitCommandManager
import java.io.File
import kotlin.system.exitProcess

fun main(args: Array<String>) = Runner().main(args)
class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")
        //  1. load config `processor.yml` and start to scm
        val file = File("processor.yml").let {
            if(!it.exists()) {
                logger.error("Config file not found: ${it.absolutePath}")
                exitProcess(1)
            }

            it
        }

        val content = file.readText()
        val config = Yaml.default.decodeFromString(deserializer = PreProcessorConfig.serializer(), content)

        // 2. clone all repositories
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

        // 3. filter test cases
        logger.info("Start to Filter Test Cases")
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

        val originFileCount = File("origindatasets").walkTopDown().count { it.isFile }
        File("origindatasets").walkTopDown().forEach {
            // if a file ends with `Test.java` or `Tests.java`, then copy it to `datasets`
            if (it.isFile && (it.name.endsWith("Test.java") || it.name.endsWith("Tests.java"))) {
                val fileName = it.nameWithoutExtension
                val targetPath = getTargetPath(fileName)
                val testProcessor: TestProcessor
                try {
                    testProcessor = TestProcessor(it.readText())
                } catch (e: Exception) {
                    logger.error("Failed to parse ${it.absolutePath}")
                    return@forEach
                }

                testProcessor.removeLicenseInfoBeforeImport().splitTests().forEachIndexed { index, test ->
                    File("$targetPath$index.${it.extension}").writeText(test)
                }
            }
        }
        val targetFileCount = File("datasets").walkTopDown().count { it.isFile }

        logger.info("Origin file count: $originFileCount")
        logger.info("Target file count: $targetFileCount")

        logger.info("Runner finished")
    }

    private fun getTargetPath(fileName: String) =
        "datasets" + File.separator + "origin" + File.separator + fileName

    private fun clonedPath(path: String) = "origindatasets" + File.separator + path

    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(Runner::class.java)
    }
}