# to-dodo 🐾

> "Your routine. Your pace. Your progress. 🌱"

A cute, privacy-first, fully offline Android routine and habit app that helps users organize their daily routines, build consistency, track progress, receive local reminders, and stay motivated.

---

## ✨ Overview

**to-dodo** is a polished, open-source Android application built with modern Kotlin and Jetpack Compose. Designed for everyday productivity without clutter, it combines a flexible routine manager, a smart universal category system, intelligent local notifications, a context-aware motivational quote engine, and local backup/restore capabilities—all operating 100% locally on your device.

---

## 🌟 Core Features

### 📝 Custom Routines
- **Flexible Scheduling**: Create recurring tasks, choose specific days of the week, set start/end times, and manage task status (enable/disable).
- **Historical Integrity**: Changes to routines respect effective dates, ensuring past weekly history and statistics are never retroactively rewritten.
- **Free Days**: Take planned breaks without breaking your streak or incurring progress penalties.

### 🏷️ Universal Categories
Designed for everyone, **to-dodo** features 11 universal default categories plus the ability to create unlimited custom categories tailored to your personal life and projects.
- **Default Categories**: Study & Learning, Work, Health & Fitness, Personal, Family, Projects, Hobbies, Self Growth, Errands, Rest, Other.
- **Custom Categories**: Add your own custom categories (e.g., *Photography*, *Freelancing*, *Cooking*, *Meditation*) with built-in validation, whitespace trimming, and case-insensitive duplicate prevention.
- **Legacy Compatibility**: Automatically maps older category formats (e.g., *Study*, *Guitar*) to maintain historical data integrity.

### ⏰ Time & Date Engine
- **Dynamic Greetings**: Time-sensitive home screen greetings that adapt to your day.
- **Current & Next Task**: Real-time task status tracking.
- **Logical Routine Day**: Features a smart 04:00 AM transition cutoff (`00:00–03:59` counts towards the previous routine day; `04:00` onward starts the new routine day) for night owls and overnight tasks.

### 💬 Motivational Quotes
- **Local Quote Library**: Bundled with 78 handcrafted local motivational quotes spanning multiple categories and time contexts.
- **Context-Aware Selection**: Smart daily rotation based on completion status, time of day, current task category, and free days.
- **Repetition Prevention**: Recent quote history tracking ensures fresh inspiration without online APIs or AI generation.

### 🔔 Local Notifications
- **Smart Reminders**: Task-specific reminders, upcoming task alerts, and optional morning summaries.
- **Robust Scheduling**: Powered by `AlarmManager` and `BroadcastReceiver` with automatic restoration upon device reboot or package updates.
- **Permission Gracefulness**: Fully operational even if notification permissions are disabled in system settings.

### 🔥 Streak & 🏆 Credit Score
- **Streak Calculation**: Earn your streak when completing **70% or more** of scheduled tasks for the logical day. Free days maintain a neutral status.
- **Credit Score Rewards**: Achieving **100% completion** awards **+1 Credit Score**. The award is strictly idempotent—ensuring the same logical date never awards duplicate credits upon app restart or recomposition.

### 👤 Personalization & Onboarding
- **Seamless Onboarding**: Quick setup supporting custom user names, starter routines, or clean empty routines.
- **Existing User Detection**: Detects existing data to prevent unexpected onboarding loops during upgrades.

### backup & Restore
- **JSON Export / Import**: Easily back up your routine, task history, custom categories, streak, and preferences to a local JSON file or restore from a previous backup.
- **Robust Validation**: Validates backup schemas safely against malformed inputs or older backup versions.

---

## 🔒 Privacy First

**to-dodo** is designed to keep your data local and private:
- 📴 **100% Offline**: Operates fully without a cloud backend, Firebase, remote databases, or online user accounts.
- 🔐 **Local Storage**: All tasks, completion records, custom categories, credit scores, and settings reside securely on your device using **Room Database** and **DataStore**.
- 🚫 **No Tracking / No AI**: Zero telemetry, analytics, or external AI API calls.

---

## 🏛️ Architecture & Tech Stack

**to-dodo** follows modern Android architectural best practices (MVVM, Clean Architecture separation of concerns):

```text
Android UI (Jetpack Compose, Material 3)
    ↓
ViewModel & StateFlow
    ↓
Local Application State
    ├── Room Database (v3 with explicit migrations)
    ├── Jetpack DataStore (Preferences)
    ├── Time Engine & Routine Logic
    └── Local Notification Scheduler (AlarmManager)
```

### Key Technologies
- **Language**: 100% Kotlin & Coroutines / Flows
- **UI Framework**: Jetpack Compose & Material 3 dynamic theming
- **Local Database**: Room (Version 3 with explicit migrations `v1 → v2 → v3`)
- **Persistence**: Jetpack DataStore
- **Navigation & Serialization**: Navigation Compose & `kotlinx.serialization`
- **Testing**: Robolectric & JUnit local JVM test suite

---

## 🧪 Testing

The repository includes a comprehensive Robolectric and local JVM test suite covering database migrations, routine calculations, time engine boundaries, quote rotation, notification scheduling, and category management.

```text
Test Status
───────────
Passed  ✓ (100%)
Failed  0
Skipped 0
```

---

## 📄 License

Distributed under the MIT License. See `LICENSE` for more information.
