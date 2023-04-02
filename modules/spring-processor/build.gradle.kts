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

    // java parser
    implementation(libs.swagger.parser)
    implementation(libs.swagger.parser.v3)
    implementation(libs.javaparser)
    implementation(libs.javaparser.serialization)
    implementation(libs.javaparser.symbol.solver.core)

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
    mainClass.set("cc.unitmesh.spring.MainKt")
}

tasks {
    shadowJar {
        manifest {
            attributes(Pair("Main-Class", "cc.unitmesh.spring.MainKt"))
        }
        // minimize()
        dependencies {
            exclude(dependency("org.junit.jupiter:.*:.*"))
            exclude(dependency("org.junit:.*:.*"))
            exclude(dependency("junit:.*:.*"))
        }
    }
}
