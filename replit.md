# Goal2 - Android Goal-Setting App

## Project Overview
This is an Android mobile application built with Kotlin and Jetpack libraries. It's a goal-setting and daily task management app that helps users track their goals and complete daily tasks.

**Status:** Code imported successfully. Android build tools unavailable in Replit environment.

## Project Structure
- `app/src/main/java/com/goalguru/` - Main application code
  - `ui/` - Android Activities (MainActivity, DailyTaskActivity, DashboardActivity, etc.)
  - `data/` - Room database entities, DAOs, and database setup
  - `service/` - Firebase service for push notifications
  - `api/` - OpenRouter API client for AI integration
- `app/src/main/res/` - Android resources (layouts, strings, colors, themes)
- `app/build.gradle.kts` - App-level Gradle configuration
- `build.gradle.kts` - Root-level Gradle configuration
- `local.properties` - Local Android SDK configuration

## Technology Stack
- **Language:** Kotlin
- **UI Framework:** Android Jetpack (AndroidX)
- **Database:** Room Database (SQLite)
- **Networking:** Retrofit 2 with OkHttp
- **Async:** Kotlin Coroutines
- **Notifications:** Firebase Cloud Messaging
- **API Integration:** OpenRouter for AI
- **Charts:** MPAndroidChart

## Build Configuration
- **Target SDK:** 34
- **Min SDK:** 24
- **Java Version:** 11
- **Gradle Version:** 8.4

## Known Limitations
- The Replit environment does not provide the Android SDK. Full APK builds (e.g., `gradle assembleDebug`) will fail due to missing SDK components.
- Use the "Code Validation" workflow to verify code logic.
- Refer to `BUILD_INSTRUCTIONS.md` for steps to build the APK locally or via CI.

## Recent Changes
- Fixed DeepSeek API integration in `OpenRouterClient.kt`.
- Repaired Gradle wrapper and script permissions.
- Added comprehensive `BUILD_INSTRUCTIONS.md`.
