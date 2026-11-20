import com.igorwojda.showcase.buildlogic.ext.buildConfigFieldFromGradleProperty

plugins {
    id("com.igorwojda.showcase.convention.application")
}

android {
    namespace = "com.igorwojda.showcase.app"

    defaultConfig {
        applicationId = "com.nguyenne.arenaflow"

        versionCode = 1
        versionName = "0.0.1" // SemVer (Major.Minor.Patch)

        buildConfigFieldFromGradleProperty(project, "apiBaseUrl")
        buildConfigFieldFromGradleProperty(project, "apiToken")
        buildConfigField("String", "GRADLE_SUPABASE_URL", "\"${providers.gradleProperty("supabaseUrl").orElse("").get()}\"")
        buildConfigField("String", "GRADLE_SUPABASE_ANON_KEY", "\"${providers.gradleProperty("supabaseAnonKey").orElse("").get()}\"")
    }

    buildTypes {
        getByName("debug") {
            // theem đuôi .debug -> id sẽ thnafh com....arenaflow.debug
            applicationIdSuffix = ".debug"
            // (option) đổi tên app thnafh ->
            resValue("string", "app_name", "ArenaFlow DEBUG")
        }

        getByName("release") {
            // cần key để kí TODO sign keystore
            // id giữ nguyên gốc
            signingConfig = signingConfigs.getByName("debug") // dòng này để kí key
            isMinifyEnabled = false
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
        }
    }

    buildFeatures {
        resValues = true // bat tinh nang resValue
    }
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.ktor.client.okhttp)
    // "projects." Syntax utilizes Gradle TYPESAFE_PROJECT_ACCESSORS feature
    implementation(projects.feature.base)
    implementation(projects.feature.album)
    implementation(projects.feature.settings)
    implementation(projects.feature.favourite)

    implementation(projects.feature.match)
}
