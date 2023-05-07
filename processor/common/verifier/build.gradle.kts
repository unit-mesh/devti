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

    // UML
    implementation("net.sourceforge.plantuml:plantuml:1.2023.6")

    // SQL
    implementation("com.github.jsqlparser:jsqlparser:4.6")

    // Markdown
    implementation("org.commonmark:commonmark:0.21.0")
    implementation("org.commonmark:commonmark-ext-gfm-tables:0.21.0")

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.logback.classic)
    implementation("org.testng:testng:7.1.0")

    testImplementation(kotlin("test"))
    testImplementation(libs.bundles.test)
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}
