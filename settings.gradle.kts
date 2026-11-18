rootProject.name = "ArenaFlow"

include(
    ":app",
    ":feature:album",
    ":feature:settings",
    ":feature:favourite",
    ":feature:base",
    ":library:test-utils",
)
include(":konsist-test") // có thể chỉ viết thành 1line

pluginManagement {
    includeBuild("build-logic") // cơ chế composite build
    // vì thế có thể đặt tên folder là gì cũng được (tìm hiểu về buildSrc root project)
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
        // Added for testing local Konsist artifacts
        mavenLocal()
        mavenCentral()
    }
}

// Generate type safe accessors when referring to other projects eg.
// Before: implementation(project(":feature_album"))
// After: implementation(projects.featureAlbum)
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
