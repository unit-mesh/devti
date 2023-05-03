package cc.unitmesh.core.cli

import cc.unitmesh.core.git.GitCommandManager
import com.charleskorn.kaml.Yaml
import org.slf4j.Logger
import java.io.File
import kotlin.system.exitProcess

object ProcessorUtils {
    private val logger: Logger = org.slf4j.LoggerFactory.getLogger(ProcessorUtils::class.java)
    private fun clonedPath(path: String) = "origindatasets" + File.separator + path

    fun cloneAllRepositories(config: PreProcessorConfig) {
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

    fun loadConfig(): PreProcessorConfig {
        val file = File("processor.yml").let {
            if (!it.exists()) {
                logger.error("Config file not found: ${it.absolutePath}")
                exitProcess(1)
            }

            it
        }

        val content = file.readText()
        return Yaml.default.decodeFromString(deserializer = PreProcessorConfig.serializer(), content)
    }

}
