@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    alias(libs.plugins.serialization)
    application
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.clikt)
    implementation(libs.kotlin.compiler)

    implementation(libs.serialization.json)

    // Logging
    implementation(libs.logging.slf4j.api)
    implementation(libs.logging.logback.classic)

    implementation("com.squareup.retrofit2:converter-jackson:2.9.0")
    implementation("com.theokanning.openai-gpt3-java:service:0.12.0")

    implementation("org.commonmark:commonmark:0.21.0")

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
    mainClass.set("cc.unitmesh.processor.swagger.MainKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "cc.unitmesh.processor.api.MainKt"))
        }
        // minimize()
        dependencies {
            exclude(dependency("org.junit.jupiter:.*:.*"))
            exclude(dependency("org.junit:.*:.*"))
            exclude(dependency("junit:.*:.*"))
        }
    }
}
