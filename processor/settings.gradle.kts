@file:Suppress("UnstableApiUsage")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "UnitProcessor"

include(
    ":kotlin-modules:analysis",
    ":kotlin-modules:unit-demo",
    ":kotlin-modules:importer",

    ":java-modules:core-analysis",

    ":java-modules:codegen-processor",
    ":java-modules:swagger-processor",
    ":java-modules:spring-processor",
    ":java-modules:test-processor",
    ":java-modules:requirements-diff-processor"
)