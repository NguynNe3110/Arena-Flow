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

    val releaseName = "release"

    signingConfigs {
        create(releaseName) {
            val keystorePath =
                System.getenv("KEYSTORE_PATH")
                    ?: (project.findProperty("KEYSTORE_PATH") as? String)
            val storePasswordProp =
                System.getenv("KEYSTORE_PASSWORD")
                    ?: (project.findProperty("KEYSTORE_PASSWORD") as? String)
            val keyAliasProp =
                System.getenv("KEY_ALIAS")
                    ?: (project.findProperty("KEY_ALIAS") as? String)
            val keyPasswordProp =
                System.getenv("KEY_PASSWORD")
                    ?: (project.findProperty("KEY_PASSWORD") as? String)

            val resolvedFile =
                keystorePath?.let { path ->
                    val f = file(path)
                    if (f.exists()) f else rootProject.file(path)
                }

            if (resolvedFile?.exists() == true) {
                storeFile = resolvedFile
                storePassword = storePasswordProp
                keyAlias = keyAliasProp
                keyPassword = keyPasswordProp
            } else {
                // Fallback về debug key khi build local không có keystore release
                initWith(getByName("debug"))
            }
        }
    }

    buildTypes {
        getByName("debug") {
            // thêm đuôi .debug -> id sẽ thành com....arenaflow.debug
            applicationIdSuffix = ".debug"
            // đổi tên app thành ArenaFlow DEBUG
            resValue("string", "app_name", "ArenaFlow DEBUG")
        }

        getByName(releaseName) {
            signingConfig = signingConfigs.getByName(releaseName)
            isMinifyEnabled = false
            proguardFiles("proguard-android.txt", "proguard-rules.pro")
        }
    }

    buildFeatures {
        resValues = true // bật tính năng resValue
    }
}

dependencies {
    implementation(platform(libs.supabase.bom))
    implementation(libs.bundles.supabase)
    implementation(libs.ktor.client.okhttp)
    // "projects." Syntax utilizes Gradle TYPESAFE_PROJECT_ACCESSORS feature
    implementation(projects.feature.base)
    implementation(projects.feature.auth)
    implementation(projects.feature.profile)
    implementation(projects.feature.team)
    implementation(projects.feature.venue)
    implementation(projects.feature.match)
    implementation(projects.feature.pairing)
    implementation(projects.feature.mercenary)
    implementation(projects.feature.chat)
    implementation(projects.feature.notification)
    implementation(projects.feature.reputation)
    implementation(projects.feature.admin)
}
