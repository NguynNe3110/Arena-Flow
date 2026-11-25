rootProject.name = "ArenaFlow"

include(
    ":app",
    ":feature:base",
    ":feature:auth",
    ":feature:profile",
    ":feature:team",
    ":feature:venue",
    ":feature:match",
    ":feature:pairing",
    ":feature:mercenary",
    ":feature:chat",
    ":feature:notification",
    ":feature:reputation",
    ":feature:admin",
    ":library:test-utils",
    ":konsist-test",
)

pluginManagement {
    includeBuild("build-logic")
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
    }
}

@Suppress("UnstableApiUsage")
dependencyResolutionManagement {
    repositories {
        google()
        mavenLocal()
        mavenCentral()
    }
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
