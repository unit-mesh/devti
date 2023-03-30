package org.unitmesh.processor.toolsets

import org.unitmesh.processor.command.Command
import org.unitmesh.processor.command.ExecOptions
import org.unitmesh.processor.command.StringListExecListeners

class GitCommandManager(var workingDirectory: String = ".") {
    private val gitEnv: MutableMap<String, String> = mutableMapOf(
        "GIT_TERMINAL_PROMPT" to "0", // Disable git prompt
        "GCM_INTERACTIVE" to "Never" // Disable prompting for git credential manager
    )

    private var gitPath = "git"

    private val exec = Command()

    fun shallowClone(url: String, branch: String = "master"): GitOutput {
        val args = mutableListOf("clone", url, "--branch", branch, "--single-branch", "--depth", "1", ".")
        return execGit(args)
    }

    private fun execGit(args: List<String>, allowAllExitCodes: Boolean = false, silent: Boolean = false): GitOutput {
        val result = GitOutput()

        val env = mutableMapOf<String, String>()
        for ((key, value) in System.getenv()) {
            env[key] = value
        }
        for ((key, value) in gitEnv) {
            env[key] = value
        }

        val stdout = mutableListOf<String>()
        val stderr = mutableListOf<String>()
        val options = ExecOptions(
            cwd = workingDirectory,
            env = env,
            silent = silent,
            ignoreReturnCode = allowAllExitCodes,
            listeners = StringListExecListeners(stdout, stderr)
        )

        result.exitCode = exec.exec(gitPath, args, options)
        result.stdout = stdout.joinToString("\n")

        return result
    }
}

data class GitOutput(
    var stdout: String = "",
    var exitCode: Int = 0
)
