# GoalGuru - AI-Powered Goal Tracking Android App

GoalGuru is a powerful Android application designed to help users achieve their goals through AI-generated roadmaps, daily task tracking, and motivational push notifications.

## Features

### 1. AI-Powered Goal Roadmap
- Enter any goal (e.g., "Learn Kotlin", "Lose 5kg")
- AI (DeepSeek) generates a structured 30-day roadmap
- Roadmap stored locally on phone
- Step-by-step daily goals with descriptions and tips

### 2. Daily Task Engine
- Displays today's task with clear actions
- Complete or Skip options
- Auto-updates progress tracking
- Offline access with cached tasks

### 3. Roast Notifications (Firebase Cloud Messaging)
- AI-generated motivational push notifications
- Customizable roast level:
  - **Mild**: Gentle reminders
  - **Spicy**: Motivational pushes
  - **Extra-Spicy**: Hilarious roasts
- Language support (English & Hindi)
- Personalized messages based on gender preference

### 4. Progress & Streak Dashboard
- View total and completed tasks
- Track completion percentage
- Monitor current and best streak
- Beautiful card-based UI with animations

### 5. User Settings Panel
- **Roast Level**: Choose your motivation style
- **Language**: English or Hindi
- **Gender**: For personalized messages
- **Notification Time**: Set your reminder time
- **Theme**: Light, Dark, or System

## Tech Stack

- **Language**: Kotlin
- **Database**: Room (SQLite)
- **Networking**: Retrofit2 + OkHttp
- **AI**: DeepSeek via OpenRouter API
- **Push Notifications**: Firebase Cloud Messaging
- **UI**: Material Design 3
- **Architecture**: MVVM + Coroutines

## Installation & Build

### Prerequisites
- Android Studio (Latest version)
- Android SDK 34+
- JDK 11+

### Building the APK

1. **Clone the repository:**
   ```bash
   git clone https://github.com/yourusername/GoalGuru.git
   cd GoalGuru
   ```

2. **Setup Firebase:**
   - Create a Firebase project at [firebase.google.com](https://firebase.google.com)
   - Download `google-services.json`
   - Place it in the `app/` directory

3. **Configure API Keys:**
   - Get your OpenRouter API key from [openrouter.io](https://openrouter.io)
   - Update `AIService.kt` with your key or set as environment variable

4. **Build Release APK:**
   ```bash
   ./gradlew assembleRelease
   ```
   APK will be at: `app/build/outputs/apk/release/app-release.apk`

5. **Build Debug APK:**
   ```bash
   ./gradlew assembleDebug
   ```
   APK will be at: `app/build/outputs/apk/debug/app-debug.apk`

## Project Structure

```
GoalGuru/
├── app/src/main/
│   ├── java/com/goalguru/
│   │   ├── data/           # Room entities, DAOs, database
│   │   ├── api/            # OpenRouter API client
│   │   ├── service/        # AI service, Firebase service
│   │   └── ui/             # Activities (splash, main, etc)
│   ├── res/
│   │   ├── layout/         # Activity layouts
│   │   ├── values/         # Strings, colors, themes
│   │   ├── drawable/       # UI elements
│   │   └── xml/            # Backup/extraction rules
│   └── AndroidManifest.xml
├── build.gradle.kts        # Root build config
├── settings.gradle.kts     # Project structure
└── README.md
```

## Key Components

### Data Layer
- **Goal**: User goals with AI-generated roadmaps
- **Task**: Daily tasks from goals
- **UserPreferences**: User settings (roast level, language, etc)

### Services
- **AIService**: Generates goal roadmaps and roast messages using DeepSeek
- **GoalGuruFirebaseService**: Handles FCM push notifications

### Activities
- **SplashActivity**: Splash screen
- **MainActivity**: Main navigation hub
- **GoalEntryActivity**: Create new goals
- **DailyTaskActivity**: View and complete today's task
- **DashboardActivity**: Progress overview
- **SettingsActivity**: User preferences

## API Integration

### OpenRouter (DeepSeek)
Generates both goal roadmaps and roast notifications using the DeepSeek model:
```
https://openrouter.io/api/v1/chat/completions
```

### Firebase
Handles push notifications for task reminders with the configured roast style.

## Database Schema

### Goals Table
- id, title, description, roadmap (JSON), createdAt, isActive

### Tasks Table
- id, goalId, title, description, dayNumber, isCompleted, completedAt, isSkipped, createdAt

### UserPreferences Table
- id, roastLevel, language, gender, notificationTime, theme, currentStreak, bestStreak, totalTasks, completedTasks

## Future Enhancements

- [ ] Charts for progress visualization (MPAndroidChart)
- [ ] Social sharing features
- [ ] Offline sync when connection returns
- [ ] Custom goal templates
- [ ] Leaderboards
- [ ] Community goals
- [ ] Habit tracking
- [ ] Integration with calendar apps
- [ ] Voice commands
- [ ] Wearable support

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## License

This project is licensed under the MIT License - see the LICENSE file for details.

## Contact

For questions or suggestions, please open an issue on GitHub.

---

**Created with ❤️ to help you achieve your goals!**
