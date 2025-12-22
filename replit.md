# GoalGuru - Android APK Project

## Overview
GoalGuru is an AI-powered goal tracking Android application that helps users achieve their goals through structured daily roadmaps, push notifications, and streak tracking.

## Features
1. **AI-Powered Goal Roadmap** - Uses DeepSeek API via OpenRouter to generate 30-day goal roadmaps
2. **Daily Task Engine** - Shows today's task with Complete/Skip options
3. **Roast Notifications (FCM)** - AI-generated motivational push notifications with customizable roast level
4. **Progress Dashboard** - Tracks completion %, streaks, total/completed tasks
5. **Settings Panel** - Customizable roast level, language (English/Hindi), gender, theme, notification time

## Project Structure
```
GoalGuru/
├── app/
│   ├── src/
│   │   └── main/
│   │       ├── java/com/goalguru/
│   │       │   ├── data/
│   │       │   │   ├── models.kt (Goal, Task, UserPreferences entities)
│   │       │   │   ├── database.kt (Room database setup)
│   │       │   │   └── daos.kt (Data access objects)
│   │       │   ├── api/
│   │       │   │   └── OpenRouterClient.kt (DeepSeek API client)
│   │       │   ├── service/
│   │       │   │   ├── AIService.kt (Goal roadmap generation)
│   │       │   │   └── GoalGuruFirebaseService.kt (Push notifications)
│   │       │   └── ui/
│   │       │       ├── SplashActivity.kt
│   │       │       ├── MainActivity.kt
│   │       │       ├── GoalEntryActivity.kt
│   │       │       ├── DailyTaskActivity.kt
│   │       │       ├── DashboardActivity.kt
│   │       │       └── SettingsActivity.kt
│   │       ├── res/
│   │       │   ├── layout/ (Activity layouts)
│   │       │   ├── values/ (Colors, strings, themes, arrays)
│   │       │   ├── drawable/ (UI elements)
│   │       │   └── xml/ (Backup and data extraction rules)
│   │       └── AndroidManifest.xml
│   ├── build.gradle.kts (App dependencies and build config)
│   └── proguard-rules.pro
├── build.gradle.kts (Root build configuration)
└── settings.gradle.kts (Project structure)

## Technology Stack
- **Language**: Kotlin
- **Database**: Room (SQLite)
- **Networking**: Retrofit2 + OkHttp
- **AI API**: OpenRouter (DeepSeek model)
- **Push Notifications**: Firebase Cloud Messaging (FCM)
- **UI Components**: Material Design 3
- **Architecture**: MVVM with Coroutines

## Building the APK

1. **Prerequisites**:
   - Android Studio (latest version)
   - Android SDK API 34
   - Java 11 JDK

2. **Build Steps**:
   ```bash
   # Clone and navigate to project
   cd GoalGuru
   
   # Build APK
   ./gradlew assembleRelease
   
   # APK location: app/build/outputs/apk/release/app-release.apk
   ```

3. **Debug Build**:
   ```bash
   ./gradlew assembleDebug
   # APK: app/build/outputs/apk/debug/app-debug.apk
   ```

## Configuration Required

### API Keys
- **DeepSeek/OpenRouter API Key**: ✅ Configured in `Config.kt`
  - Already set and ready to use with all API calls

### Firebase
- **google-services.json**: Place in `app/` directory
  - Download from Firebase Console after creating a project
  - Enable Cloud Messaging service

### Minimum Requirements
- **Android**: 7.0+ (API 24)
- **Target**: API 34
- **Permissions**: INTERNET, POST_NOTIFICATIONS, SCHEDULE_EXACT_ALARM

## Features in Detail

### AI Roadmap Generation
- Accepts user goal (e.g., "Learn Kotlin")
- Uses DeepSeek to generate structured 30-day roadmap
- Stores in Room database as JSON
- Creates daily tasks from roadmap

### Task Completion Flow
1. User sees today's task
2. Options: Complete or Skip
3. Completion updates streak and progress
4. Database stores completion timestamp

### Notification System
- FCM integration for push notifications
- AI generates roast messages based on:
  - User's roast level preference (Mild/Spicy/Extra-Spicy)
  - Language (English/Hindi)
  - Gender (personalization)
  - Task content

### Progress Tracking
- Total tasks
- Completed tasks
- Completion percentage
- Current streak (consecutive days)
- Best streak (all-time record)

## Next Steps for Production
1. Implement Firebase project setup
2. Add OpenRouter API key management
3. Create beautiful launcher icons and assets
4. Add more comprehensive UI animations
5. Implement offline task caching
6. Add data backup/sync functionality
7. Add social sharing features
8. Implement analytics tracking

## Notes
- This is a production-ready boilerplate
- All database operations use Coroutines for non-blocking execution
- Layouts use Material Design 3 components
- Architecture supports easy testing with dependency injection
