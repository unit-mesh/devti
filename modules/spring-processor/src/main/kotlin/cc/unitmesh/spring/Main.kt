package cc.unitmesh.spring

import cc.unitmesh.core.cli.ProcessorUtils
import com.github.ajalt.clikt.core.CliktCommand
import org.slf4j.Logger
import java.io.File

fun main(args: Array<String>) = Runner().main(args)

class Runner : CliktCommand(help = "Action Runner") {
    override fun run() {
        logger.info("Runner started")

        //  1. load config `processor.yml` and start to scm
        val config = ProcessorUtils.loadConfig()

        // 2. clone all repositories
        ProcessorUtils.cloneAllRepositories(config)

        // clean old datasets under datasets/origin
        val outputDir = File("datasets" + File.separator + "spring")
        if (outputDir.exists()) {
            outputDir.deleteRecursively()
        }
        outputDir.mkdirs()
        outputDir.walkTopDown().forEach {
            if (it.isFile) {
                it.delete()
            }
        }

        config.scm.forEach { file ->
            File("origindatasets").walkTopDown().forEach { file ->
                val isJavaPath = file.absolutePath.contains("src" + File.separator + "main" + File.separator + "java")
                if (file.isFile && isJavaPath && file.extension == "java") {

                }
            }
        }
    }

    companion object {
        val logger: Logger = org.slf4j.LoggerFactory.getLogger(Runner::class.java)
    }
}