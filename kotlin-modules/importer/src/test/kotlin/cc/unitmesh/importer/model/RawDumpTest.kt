package cc.unitmesh.importer.model

import cc.unitmesh.importer.CodeSnippetContext
import io.kotest.matchers.shouldNotBe
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import ktlint.analysis.Code
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class RawDumpTest {
    @Test
    fun test() {
        val sourceCode =
            """package com.cognifide.gradle.aem.instance.tasks\n\nimport com.cognifide.gradle.aem.common.instance.action.AwaitUpAction\nimport com.cognifide.gradle.aem.common.instance.action.ReloadAction\nimport com.cognifide.gradle.aem.common.instance.names\nimport com.cognifide.gradle.aem.common.tasks.Instance\nimport org.gradle.api.tasks.TaskAction\n\nopen class InstanceReload : Instance() {\n\n    private var reloadOptions: ReloadAction.() -\u003e Unit = {}\n\n    fun reload(options: ReloadAction.() -\u003e Unit) {\n        this.reloadOptions = options\n    }\n\n    private var awaitUpOptions: AwaitUpAction.() -\u003e Unit = {}\n\n    fun awaitUp(options: AwaitUpAction.() -\u003e Unit) {\n        this.awaitUpOptions = options\n    }\n\n    @TaskAction\n    fun reload() {\n        instanceManager.awaitReloaded(anyInstances, reloadOptions, awaitUpOptions)\n        common.notifier.lifecycle(\"Instance(s) reloaded\", \"Which: ${"$"}{anyInstances.names}\")\n    }\n\n    init {\n        description = \"Reloads all AEM instance(s).\"\n    }\n\n    companion object {\n        const val NAME = \"instanceReload\"\n    }\n}\n""""
        val originText =
            """{"repo_name":"Cognifide/gradle-aem-plugin","path":"src/main/kotlin/com/cognifide/gradle/aem/instance/tasks/InstanceReload.kt","copies":"1","size":"1052","content":"$sourceCode,"license":"apache-2.0"}"""

        val dump: RawDump = Json.decodeFromString(originText)

        assertEquals("Cognifide/gradle-aem-plugin", dump.repo_name)

        val code = Code.fromSnippet(
            content = dump.content.trimIndent(),
            script = false,
        )

        val unitContext = CodeSnippetContext.createUnitContext(code)
        unitContext.rootNode shouldNotBe null
    }
}