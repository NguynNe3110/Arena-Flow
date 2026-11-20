plugins {
    id("com.igorwojda.showcase.convention.feature")
}

android {
    namespace = "com.igorwojda.showcase.feature.match"
}

val matchMockFixture = layout.projectDirectory.file("src/main/assets/mock/matches.json")

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    inputs.file(matchMockFixture)
    systemProperty("matchMockFixturePath", matchMockFixture.asFile.absolutePath)
}
