buildscript {
    dependencies {
        classpath(kotlin("gradle-plugin", version = "1.8.0"))
    }
}

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.serialization)
    id("org.jetbrains.kotlinx.dataframe") version "0.9.1"
    id("com.google.devtools.ksp") version "1.8.20-1.0.10"
    application
}

kotlin.sourceSets.getByName("main").kotlin.srcDir("build/generated/ksp/main/kotlin/")

repositories {
    mavenCentral()
}

dependencies {
    implementation(projects.kotlinModules.analysis)

    // KSP
    implementation("com.google.devtools.ksp:symbol-processing-api:1.8.20-1.0.10")

    implementation(libs.clikt)
    implementation(libs.kotlin.compiler)
    implementation(libs.serialization.json)

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.logback.classic)

    implementation(libs.dataframe)

    implementation(libs.kaml)

    testImplementation(kotlin("test"))

    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

application {
    applicationDefaultJvmArgs = listOf("--add-opens", "java.base/java.nio=ALL-UNNAMED")
    mainClass.set("cc.unitmesh.importer.MainKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "cc.unitmesh.importer.MainKt"))
        }
        // minimize()
        dependencies {
            exclude(dependency("org.junit.jupiter:.*:.*"))
            exclude(dependency("org.junit:.*:.*"))
            exclude(dependency("junit:.*:.*"))
        }
    }
}
