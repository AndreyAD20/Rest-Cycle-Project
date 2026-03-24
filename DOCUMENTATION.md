# Rest Cycle - Code Documentation

## Overview
Rest Cycle is an Android application designed to help users manage their digital habits, sleep routines, and parental controls. The app is built using Kotlin, Jetpack Compose, and follows a layered architecture with clear separation of concerns.

## Table of Contents
- [Architecture Overview](#architecture-overview)
- [Key Modules and Packages](#key-modules-and-packages)
- [Core Components](#core-components)
- [Data Flow](#data-flow)
- [Background Services](#background-services)
- [Dependency Injection](#dependency-injection)
- [Testing](#testing)
- [Build and Deployment](#build-and-deployment)

## Architecture Overview
The app follows a combination of MVVM (Model-View-ViewModel) and Clean Architecture principles:
- **Presentation Layer**: Jetpack Compose UI, ViewModels
- **Domain Layer**: Use cases, business logic (if separated)
- **Data Layer**: Repositories, data sources (local and remote)

Given the app's scope, the architecture is pragmatic, focusing on:
- Clear separation between UI, business logic, and data
- Use of Jetpack Architecture Components (ViewModel, LiveData, StateFlow)
- Repository pattern for data abstraction
- Kotlin Coroutines for asynchronous operations

## Key Modules and Packages
```
com.example.rest
├── BaseComposeActivity.kt          # Base activity for Compose setup
├── DowntimeManager.kt              # Singleton for local schedule management
├── UsageMonitorService.kt          # Main background service for DND/grayscale
├── utils/                          # Utility functions and helpers
├── data/                           # Data handling (local, remote, models)
│   ├── models/                     # Data classes (Schedule, Horario, etc.)
│   └── repository/                 # Repository interfaces and implementations
├── features/                       # Feature-specific code (screen-level)
│   ├── auth/                       # Authentication screens and logic
│   ├── home/                       # Main home/dashboard screens
│   ├── tools/                      # Utility features (schedule, notes, etc.)
│   ├── parental/                   # Parental control features
│   ├── habits/                     # Habit tracking and statistics
│   └── settings/                   # User settings and configuration
└── services/                       # Android services (background work)
```

## Core Components

### 1. DowntimeManager (Singleton)
- **File**: `DowntimeManager.kt`
- **Purpose**: Manages local storage of schedules (SharedPreferences via Gson) and provides core logic for determining if a schedule is active.
- **Key Functions**:
  - `getSchedules(context): List<HorarioDescanso>` - Loads all schedules
  - `saveSchedule(context, schedule)` - Saves a single schedule
  - `deleteSchedule(context, scheduleId)` - Deletes a schedule by ID
  - `isScheduleActive(schedule): Boolean` - Checks if a schedule should be active now (handles midnight-crossing schedules)
  - `parseMinutes(timeStr): Int` - Converts "HH:mm" or "HH:mm:ss" to minutes since midnight

### 2. UsageMonitorService (Foreground Service)
- **File**: `UsageMonitorService.kt`
- **Purpose**: Runs in the background to check active schedules every 3 seconds and applies/removes DND and grayscale mode accordingly.
- **Key Mechanisms**:
  - Uses `Handler + Runnable` for periodic execution (`checkDowntime()` every 3 seconds)
  - Runs as a foreground service with persistent notification (required for long-running background work on Android)
  - Manages DND via `NotificationManager.InterruptionFilter`
  - Manages grayscale via `Settings.Secure` (requires `WRITE_SECURE_SETTINGS` permission, typically granted via ADB)
  - Checks `DowntimeManager.isScheduleActive()` for each local schedule

### 3. HorarioRepository
- **File**: `HorarioRepository.kt` (in `data/repository/`)
- **Purpose**: Handles remote data operations with Supabase (cloud synchronization).
- **Key Features**:
  - Implements offline-first strategy: saves locally first, then attempts to sync with Supabase
  - Handles conversion between local (`HorarioDescanso`) and remote (`Horario`, `DiasHorario`) models
  - Manages relationships (schedules to days of week)
  - Provides CRUD operations for schedules and related entities

### 4. Feature Modules (under `features/`)
Each feature follows a similar pattern:
- **UI Layer**: Jetpack Compose `@Composable` functions
- **State Management**: ViewModels or state hoisted to Activities
- **Data Access**: Repositories or direct calls to managers/services
- **Navigation**: Implicit via activities (single-activity model with multiple screens)

Notable features:
- **auth**: Login, registration, verification, password recovery
- **home**: Main dashboard, profile, habit statistics
- **tools**: Schedule management (horas de descanso), notes, app blocker, break timer, task manager, calendar
- **parental**: Child management, location tracking, remote blocking
- **habits**: Statistics and analytics for habit tracking

## Data Flow

### Schedule Creation (Example)
1. User creates a schedule in `HoraDescansoComposeActivity` or `DialogoCrearHorario`
2. Locally saved via `DowntimeManager.saveSchedule()`
3. Service restarted via `restartService()` to pick up new schedule immediately
4. If device ID available, attempt to sync with Supabase via `HorarioRepository.crearHorario()`
5. On Supabase success: create related `DiasHorario` entries
6. On Supabase failure: show toast indicating local-only save (offline-first)

### Schedule Evaluation (Background)
1. `UsageMonitorService` wakes up every 3 seconds via `Handler.postDelayed`
2. `checkDowntime()` called:
   - Gets all local schedules via `DowntimeManager.getSchedules()`
   - For each schedule, calls `DowntimeManager.isScheduleActive(schedule)`
   - If any schedule is active and has `bedtimeMode=true`, enables DND and grayscale
   - Otherwise, disables DND and grayscale
3. DND managed via `NotificationManager.setInterruptionFilter()`
4. Grayscale managed via `Settings.Secure.putInt()` for `accessibility_display_daltonizer_enabled` and `accessibility_display_daltonizer`

## Background Services
The app uses several Android services for background work:

### UsageMonitorService
- **Trigger**: Started from `HoraDescansoComposeActivity` via `restartService()`
- **Type**: Foreground service (shows persistent notification)
- **Work**: Periodic schedule checking and DND/grayscale control
- **Permissions**: 
  - `FOREGROUND_SERVICE` (base)
  - `FOREGROUND_SERVICE_DATA_SYNC` (for data sync type)
  - `POST_NOTIFICATIONS` (for showing notification)
  - `WRITE_SECURE_SETTINGS` (for grayscale - dangerous, requires special handling)
  - `ACCESS_NOTIFICATION_POLICY` (for DND control)

### Other Services
- `ChatHeadOverlayService`: For floating chat heads (messenger-style UI)
- `AppMonitorService`: Tracks app usage for blocking functionality
- `BootReceiver` / `services.BootReceiver`: Restart services after device reboot
- `TaskAlarmReceiver`: Handles exact alarms for task notifications

## Dependency Management
The app uses Gradle Version Catalogs (in `gradle/libs.versions.toml`) for dependency management. Key dependencies include:

- **Jetpack Compose**: For modern declarative UI
- **Coroutines**: For asynchronous programming (`kotlinx-coroutines-android/core`)
- **Lifecycle**: ViewModel, LiveData for UI state management
- **Retrofit + Gson**: For HTTP communication with Supabase
- **Ktor**: Alternative HTTP client used for Supabase SDK
- **Supabase Kotlin SDK**: Official Supabase client for authentication and database
- **Google Maps**: Maps SDK and Maps Compose for location features
- **WorkManager**: For deferrable background tasks
- **Security**: JCrypt for password encryption, Android Security Crypto for SharedPreferences encryption
- **Testing**: JUnit, Espresso, Compose UI testing

## Testing Strategy
- **Unit Tests**: For pure logic (e.g., `DowntimeManager` time calculations)
- **Instrumented Tests**: For Android-specific functionality (Services, ContentProviders)
- **UI Tests**: Using Compose testing APIs for composables and screens
- **Test Organization**: 
  - Unit tests: `src/test/java/`
  - Instrumented tests: `src/androidTest/java/`

## Build and Deployment
### Build Variants
- **debug**: For development and testing
- **release**: For production (with ProGuard/R8 minification and obfuscation)

### Signing
- Release builds require a signing key (keystore) configured in:
  - `gradle.properties` (not committed) or
  - Manual signing via Android Studio Build > Generate Signed Bundle / APK

### Play Store Publishing
1. Generate release AAB (Android App Bundle): `./gradlew bundleRelease`
2. Upload AAB to Google Play Console
3. Complete store listing (description, screenshots, etc.)
4. Set content rating and pricing/distribution
5. Publish to internal/test/open tracks before production

## Important Notes for Developers

### Permissions
The app requests numerous dangerous permissions. Always:
- Check permission status at runtime (especially for Android 6.0+)
- Explain to users why each permission is needed
- Handle permission denials gracefully

### Background Execution Limits
Be aware of Android background execution limits:
- Foreground services are required for long-running background work
- Use `JobScheduler` or `WorkManager` for deferrable tasks when possible
- Respect doze mode and app standby buckets

### Compose Specific
- Remember that Compose recomposes frequently; avoid expensive operations in `@Composable` functions
- Use `remember`, `rememberSaveable`, and `derivedStateOf` appropriately
- Follow unidirectional data flow (state hoisting) for testability

### Supabase Integration
- Offline-first design ensures core functionality works without internet
- Handle network errors gracefully and provide user feedback
- Respect rate limits and implement exponential backoff if needed

## Future Improvements
Consider implementing:
- Dependency injection (Hilt) for better testability
- Clear separation of domain and data layers
- More comprehensive unit and integration testing
- Feature modules with independent navigation graphs
- Theming improvements and dark mode enhancements
- Analytics and crash reporting (Firebase Analytics, Crashlytics)