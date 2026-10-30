# Habit Tracker 🌱

A beautiful, modern habit tracker for Android with an iOS-inspired design. Build lasting habits with streak tracking, statistics, and a clean, minimal interface.

## Features

- **Create & Track Habits** — Boolean daily check-ins with flexible scheduling (daily, weekly, specific days)
- **Streak Tracking** — See your current streak, best streak, and running days for each habit  
- **Habit Strength Score** — Weighted algorithm that rewards consistency with exponential decay
- **Beautiful Statistics** — Completion rates, bar charts, per-habit breakdowns
- **iOS-Inspired Design** — Soft pastels, large corner radii, smooth animations
- **Dark Mode** — Full dark theme support
- **Reminders** — Set per-habit notification reminders
- **Archive & Manage** — Archive habits you're pausing, delete ones you're done with
- **Offline First** — All data stored locally with Room database

## Tech Stack

- **Kotlin** + **Jetpack Compose** (Material 3)
- **Room** for local persistence
- **Hilt** for dependency injection
- **Navigation Compose** for screen routing
- **Coroutines + Flow** for reactive data

## Architecture

MVVM with Repository pattern:

```
UI (Compose Screens)
  ↓
ViewModels (State + Business Logic)
  ↓
Repository (Data Coordination)
  ↓
Room DAOs (Local Database)
```

## Building

1. Open in Android Studio Hedgehog or newer
2. Sync Gradle
3. Run on device/emulator (min SDK 26)

## Screenshots

_Coming soon_

---

Made with ❤️ by Bhuvnendra
