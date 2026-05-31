# Kalkan

Kalkan is a premium Android emergency and disaster platform built with Kotlin, Jetpack Compose, Material 3, Firebase, Room, MVVM, Repository Pattern, Hilt and Navigation Compose.

## Product Principle

The home screen must stay simple. New features should live inside their own modules unless they directly support these first-screen actions:

- Share safe status
- Request help
- Share location
- Send SOS
- See recent critical information
- Reach emergency contacts

## Current Foundation

- Android Compose project scaffold
- Material 3 theme with Kalkan brand colors
- Bottom navigation: Home, Earthquakes, Map, Family, Profile
- Super Admin placeholder screen
- Domain models for users, earthquakes, emergency status, contacts, family, announcements and offline info
- Repository contracts and initial Firebase-backed implementations
- Room database scaffold for offline emergency contacts and offline information
- Hilt modules for Firebase, Room and repositories

## Firebase Setup

1. Create a Firebase Android app with package `com.zgrcan.kalkan`.
2. Download `google-services.json`.
3. Place it at `app/google-services.json`.
4. Enable this line in `app/build.gradle.kts`:

```kotlin
alias(libs.plugins.google.services)
```

## Font

The requested product font is Plus Jakarta Sans. Add the font files under `app/src/main/res/font/` and update `core/design/theme/Type.kt` to use that family. Until font files are added, the app uses the platform sans-serif fallback.
