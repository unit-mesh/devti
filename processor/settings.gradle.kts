@file:Suppress("UnstableApiUsage")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "UnitProcessor"

include(
    ":common:core",
    ":common:cli-core",
    ":common:prompter",

    ":examples:unit-demo",

    ":kotlin-modules:analysis",
    ":kotlin-modules:importer",

    ":java-modules:core-analysis",
    ":java-modules:codegen-processor",
    ":java-modules:spring-processor",
    ":java-modules:test-processor",

    ":modules:api-processor",
    ":modules:plantuml-verifier",
    ":modules:sql-verifier",
)
