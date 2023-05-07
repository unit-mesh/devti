@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.serialization)
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.serialization.json)

    implementation("net.sourceforge.plantuml:plantuml:1.2023.6")
    implementation("com.github.jsqlparser:jsqlparser:4.6")

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.logback.classic)

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
