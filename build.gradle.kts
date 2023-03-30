@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.shadow)
    application
}

group = "org.unitmesh"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.picocli)
    implementation(libs.kotlin.compiler)

    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}

kotlin {
    jvmToolchain(11)
}

tasks.jar {
    manifest {
        attributes("Main-Class" to "org.unitmesh.processor.Main")
        attributes("Implementation-Version" to version)
    }
}

application {
    mainClass.set("org.unitmesh.processor.MainKt")
}
