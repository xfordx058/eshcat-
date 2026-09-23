# eSHCAT Android app

Native Android implementation of eSHCAT, built with Kotlin, Jetpack Compose, Room, and Material 3.

## Run locally

Open `android-app/` in Android Studio, or run:

```powershell
.\gradlew.bat :app:assembleDebug
```

## Email configuration

SMTP credentials are intentionally not stored in source control. For local development, add the following to your user Gradle properties file (`%USERPROFILE%\.gradle\gradle.properties`):

```text
SMTP_USERNAME=your-email@example.com
SMTP_PASSWORD=your-app-password
```

For a production release, send email through a backend endpoint; values bundled into an Android APK can be extracted.
