@file:Suppress("UnstableApiUsage")

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "PreProcessor"

include(
    ":modules:core-analysis",
    ":modules:swagger-processor",
    ":modules:spring-processor",
    ":modules:test-processor"
)