# GoalGuru - Android Build Instructions

## Environment Status
- **Replit Environment**: Android SDK is **NOT** available in the Replit container.
- **Development Tooling**: Kotlin/Java 11/17 are available for code editing and syntax validation.
- **Workflow**: The "Code Validation" workflow confirms the AI integration and logic are sound.

## How to Build the APK
Since the Replit environment lacks the Android SDK, follow these steps to generate an APK:

### Option 1: Local Development (Recommended)
1. Download the project zip or use Replit's Git integration to clone the repo.
2. Open the project in **Android Studio**.
3. Let Gradle sync and download the necessary SDK platforms.
4. Go to `Build > Build Bundle(s) / APK(s) > Build APK(s)`.

### Option 2: GitHub Actions (CI/CD)
1. Push this code to a GitHub repository.
2. The included `.github/workflows/` (if present) or a standard Android CI template will have the necessary SDK to build the APK.

## Recent Changes
- Fixed DeepSeek API integration (Base URL: `https://api.deepseek.com/v1/`).
- Resolved namespace issues in `app/build.gradle.kts`.
- Configured `local.properties` to guide local Android Studio setup.
