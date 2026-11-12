# ArenaFlow

ArenaFlow is an Android application under development, based on
[Android Showcase](https://github.com/igorwojda/android-showcase) by Igor Wojda.
The current codebase retains the template's album browsing, favorites, and settings
features as a starting point; these are not yet the final ArenaFlow product.

## Project identity

- App name: **ArenaFlow**
- Application ID: `com.nguyenne.arenaflow`
- Version: `0.0.1` (version code `1`)
- Repository: https://github.com/NguynNe3110/Arena-Flow-App

Internal Kotlin packages, Android namespaces, convention plugin IDs, and theme names
still use `com.igorwojda.showcase` / `Showcase` to avoid an unrelated source-wide migration.

## Technology and modules

Kotlin, Jetpack Compose, Clean Architecture, Koin, Retrofit/OkHttp, Room, and Supabase.

- `app`: application entry point and dependency wiring.
- `feature:base`: shared feature infrastructure.
- `feature:album`, `feature:favourite`, `feature:settings`: existing template features.
- `library:test-utils`: shared testing utilities.
- `konsist-test`: architecture checks.
- `build-logic`: Gradle convention plugins.
- `gradle/libs.versions.toml`: dependency and plugin versions.

## Getting started

Requirements:
- Android Studio supporting Android Gradle Plugin **9.2.0**.
- JDK **17** (also required by the build-logic Java toolchain).
- Android SDK Platform **37**; minimum device API **28**, target SDK **35**.
- Gradle **9.4.1**, downloaded automatically by the wrapper.

```bash
git clone https://github.com/NguynNe3110/Arena-Flow-App.git
cd Arena-Flow-App
```

Open the repository root in Android Studio and sync Gradle.
Keep local SDK paths and private backend configuration outside version control.
Do not commit API credentials, signing keys, or service secrets.

On Windows PowerShell:

```powershell
.\gradlew.bat :app:assembleDebug
.\gradlew.bat :konsist-test:test
```

On macOS/Linux, use `./gradlew` instead of `.\gradlew.bat`.
The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

## Quality checks

Existing CI includes debug builds, Android lint, Detekt, Konsist architecture tests,
and Spotless formatting checks:

```powershell
.\gradlew.bat lint
.\gradlew.bat detektCheck
.\gradlew.bat spotlessCheck
```

See [DeveloperReadme.md](DeveloperReadme.md) for inherited tooling notes.

## Attribution and licenses

The original Android Showcase template was authored by **Igor Wojda**.
See [LICENSE.txt](LICENSE.txt) for the source license and retained copyright notices.

Inherited animations are distributed under Creative Commons License 2.0:
- [Error screen](https://lottiefiles.com/8049-error-screen) by Chetan Potnuru.
- [Building Screen](https://lottiefiles.com/1271-building-screen) by Carolina Cajazeira.
