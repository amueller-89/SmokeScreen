# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

SmokeScreen is an Android application built with Kotlin and Jetpack Compose. This is a modern Android project using the latest Android Gradle Plugin (8.13.0) and Kotlin (2.0.21) with Compose UI toolkit.

## Architecture

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose (BOM version 2024.09.00)
- **Build System**: Gradle with Kotlin DSL
- **Minimum SDK**: 24 (Android 7.0)
- **Target SDK**: 36
- **Java Version**: 11

### Project Structure

```
app/src/
├── main/java/com/example/smokescreen/
│   ├── MainActivity.kt                 # Main activity with Compose setup
│   └── ui/theme/                      # Compose theme components
│       ├── Color.kt                   # Color definitions
│       ├── Theme.kt                   # Theme implementation
│       └── Type.kt                    # Typography definitions
├── test/                              # Unit tests
└── androidTest/                       # Instrumented tests
```

## Development Commands

**Note**: Requires Java Runtime Environment to be installed. If not installed, visit http://www.java.com

### Build Commands
- `./gradlew build` - Build the project
- `./gradlew assembleDebug` - Build debug APK
- `./gradlew assembleRelease` - Build release APK

### Testing Commands
- `./gradlew test` - Run unit tests
- `./gradlew connectedAndroidTest` - Run instrumented tests (requires device/emulator)

### Development Tasks
- `./gradlew installDebug` - Install debug build on connected device
- `./gradlew clean` - Clean build artifacts

## Dependencies

The project uses Gradle Version Catalogs (`gradle/libs.versions.toml`) for dependency management:

- **Core**: AndroidX Core KTX, Lifecycle Runtime KTX
- **UI**: Jetpack Compose (UI, Material3, Activity Compose)
- **Testing**: JUnit, AndroidX Test (JUnit, Espresso)

## Key Files

- `build.gradle.kts` - Root project build configuration
- `app/build.gradle.kts` - App module build configuration  
- `gradle/libs.versions.toml` - Centralized dependency version management
- `settings.gradle.kts` - Project settings and module declarations