@file:Suppress("UnstableApiUsage")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "UnitProcessor"

include(
    ":modules:core-analysis",

    ":modules:codegen-processor",
    ":modules:swagger-processor",
    ":modules:spring-processor",
    ":modules:test-processor",
    ":modules:requirements-diff-processor"
)