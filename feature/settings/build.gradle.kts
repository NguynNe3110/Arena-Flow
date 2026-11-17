plugins {
    id("com.igorwojda.showcase.convention.feature") // khai báo kế thừa (build-logic)
}

android {
    namespace = "com.igorwojda.showcase.feature.settings"
}

dependencies {
    implementation(libs.aboutlibraries.compose)
}
