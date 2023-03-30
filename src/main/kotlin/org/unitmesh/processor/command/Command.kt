package org.unitmesh.processor.command

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File

/**
 * Executes a command and returns the output.
 */
class Command {
    fun execJar(args: List<String>, workdir: String, options: ExecOptions = ExecOptions(cwd = workdir)): Int {
        val processBuilder = ProcessBuilder("java", "-jar", *args.toTypedArray())
        return doExecute(processBuilder, options)
    }

    fun exec(commandLine: String, args: List<String> = listOf(), options: ExecOptions): Int {
        if (!options.silent) {
            logger.info("Executing: $commandLine ${args.joinToString(" ")}")
        }

        val processBuilder = ProcessBuilder(commandLine, *args.toTypedArray())
        return doExecute(processBuilder, options)
    }

    private fun doExecute(processBuilder: ProcessBuilder, options: ExecOptions): Int {
        val process = processBuilder
            .directory(File(options.cwd))
            .start()

        val exitCode = process.waitFor()

        process.inputStream.bufferedReader().use { it ->
            it.forEachLine { line ->
                options.listeners.stdout(line)
            }
        }

        if (exitCode != 0 && !options.ignoreReturnCode) {
            process.errorStream.bufferedReader().use {
                it.forEachLine { line ->
                    options.listeners.stderr(line)
                }
            }
        }

        return exitCode
    }

    companion object {
        private val logger: Logger = LoggerFactory.getLogger(Command::class.java)

        fun exec(commandLine: String, args: List<String> = listOf(), options: ExecOptions = ExecOptions()): Int {
            return Command().exec(commandLine, args, options)
        }
    }
}

class ExecOptions(
    val cwd: String = "",
    val env: MutableMap<String, String> = mutableMapOf(),
    val silent: Boolean = false,
    val ignoreReturnCode: Boolean = false,
    val listeners: ExecListeners = object : ExecListeners {}
)

