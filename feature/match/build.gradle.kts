plugins {
    id("com.igorwojda.showcase.convention.feature")
}

android {
    namespace = "com.igorwojda.showcase.feature.match"
}

val matchMockFixture = layout.projectDirectory.file("src/main/assets/mock/matches.json")
val matchDetailMockFixture = layout.projectDirectory.file("src/main/assets/mock/match-details.json")

tasks.withType<org.gradle.api.tasks.testing.Test>().configureEach {
    inputs.file(matchMockFixture)
    inputs.file(matchDetailMockFixture)
    systemProperty("matchDetailMockFixturePath", matchDetailMockFixture.asFile.absolutePath)
    systemProperty("matchMockFixturePath", matchMockFixture.asFile.absolutePath)
}
