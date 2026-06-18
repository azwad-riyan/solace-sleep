# Solace — Sleep Tracker Android App
## Technical Specification Document

**Version:** 1.0.0  
**Target Platform:** Android 10+ (API 29+)  
**Language:** Kotlin  
**UI Framework:** Jetpack Compose (Material 3)  
**Architecture:** MVVM + Clean Architecture  
**Distribution:** Free, no ads  
**Localization:** English only  

---

## Table of Contents

1. [Product Overview](#1-product-overview)
2. [Feature List](#2-feature-list)
3. [System Architecture](#3-system-architecture)
4. [Module Breakdown](#4-module-breakdown)
5. [Automatic Sleep Detection Engine](#5-automatic-sleep-detection-engine)
6. [Data Models](#6-data-models)
7. [Database Schema](#7-database-schema)
8. [Background Services & WorkManager](#8-background-services--workmanager)
9. [Permissions](#9-permissions)
10. [UI Screens & Navigation](#10-ui-screens--navigation)
11. [Calendar View Specification](#11-calendar-view-specification)
12. [Multi-Profile System](#12-multi-profile-system)
13. [Notification Strategy](#13-notification-strategy)
14. [Cloud Sync & Backup](#14-cloud-sync--backup)
15. [Statistics & Insights Engine](#15-statistics--insights-engine)
16. [Export System](#16-export-system)
17. [Tech Stack & Dependencies](#17-tech-stack--dependencies)
18. [Project Folder Structure](#18-project-folder-structure)
19. [Key Algorithms](#19-key-algorithms)
20. [Edge Cases & Failure Handling](#20-edge-cases--failure-handling)
21. [Testing Strategy](#21-testing-strategy)
22. [Open Questions / Future Considerations](#22-open-questions--future-considerations)

---

## 1. Product Overview

**Solace** is a free, ad-free Android sleep tracking application that automatically detects when a user falls asleep and wakes up — without requiring manual input. It uses passive phone sensor signals (screen state, touch activity, accelerometer, and ambient light) to infer sleep onset and wake time. All tracked sessions are displayed in a calendar-style interface. Because automatic detection is inherently imperfect, the app provides a frictionless correction flow whenever the user opens the app after waking.

The app supports multiple user profiles on a single device (e.g., family members), stores data locally, syncs with Google Account, and offers optional cloud backup.

---

## 2. Feature List

### 2.1 Core Features

| # | Feature | Description |
|---|---|---|
| F-01 | Auto Sleep Detection | Passively infers sleep onset using screen off, zero touch, motion, and light signals |
| F-02 | Auto Wake Detection | Detects device pickup and first screen-on after inactivity window |
| F-03 | Calendar View | Monthly/weekly calendar showing sleep bars per day with color coding |
| F-04 | Sleep Session Record | Each night stored with: bed time, wake time, duration, quality score, source (auto/manual), tags |
| F-05 | Correction Flow | On first app open after sleep, user sees an edit sheet to confirm/fix bed time & wake time |
| F-06 | Multi-Profile | Multiple named profiles per device; each has independent data and settings |
| F-07 | Sleep Insights | Trends, streaks, weekly averages, sleep debt, quality score breakdown |
| F-08 | Export | Export data as CSV or PDF (per profile, date-range selectable) |
| F-09 | Local Storage | Room database; all data stored on-device |
| F-10 | Google Sync | Optional sync via Google Drive App Data folder (hidden, per-app storage) |
| F-11 | Cloud Backup | Optional periodic backup to Google Drive; restore on new device |

### 2.2 Detection & Intelligence Features

| # | Feature | Description |
|---|---|---|
| F-12 | Active Window Guard | Detection only activates between 10:00 PM and 08:00 AM (configurable per profile) |
| F-13 | Confidence Score | Each auto-detected session carries an internal confidence score (0–100) |
| F-14 | Motion Confirmation | Accelerometer stillness required for at least 20 minutes before marking sleep onset |
| F-15 | Light Confirmation | Ambient light sensor must read near-zero (darkness) for corroboration |
| F-16 | False Positive Guard | If user picks up phone during the night and uses it for > 5 minutes, the session is split or adjusted |
| F-17 | Minimum Sleep Duration | Sessions shorter than 30 minutes are discarded as false positives |
| F-18 | Nap Detection | Short sessions (30 min–3 hrs) between 10 AM and 8 PM are tagged as "Nap" not "Night Sleep" |

### 2.3 User Experience Features

| # | Feature | Description |
|---|---|---|
| F-19 | Silent Post-Sleep Review | No disruptive notifications; correction prompt appears inside the app on first open |
| F-20 | Quick Edit Sheet | Bottom sheet with time pickers for bed time and wake time; confirms with one tap |
| F-21 | Day Detail View | Tap any calendar day to see full session breakdown, tags, notes |
| F-22 | Manual Entry | User can manually add or delete a sleep session for any day |
| F-23 | Sleep Tags | User can tag sessions: "Stressed," "Caffeine," "Exercise," "Sick," etc. |
| F-24 | Notes Field | Free-text notes per sleep session |
| F-25 | Dark Mode | Full Material 3 dynamic color + manual dark/light toggle |
| F-26 | Home Screen Widget | Glance API widget showing last night's sleep summary |
| F-27 | Profile Switcher | Quick profile switch from top of any screen |
| F-28 | Onboarding | First-launch walkthrough: profile creation, permissions, detection window setup |

### 2.4 Settings Features

| # | Feature | Description |
|---|---|---|
| F-29 | Detection Window | Configurable start/end time per profile (default: 10 PM – 8 AM) |
| F-30 | Sleep Goal | Set target sleep hours per profile; used in debt calculation |
| F-31 | Sensitivity Tuning | Low/Medium/High sensitivity for detection (adjusts thresholds) |
| F-32 | Sync Settings | Enable/disable Google sync; configure sync frequency |
| F-33 | Backup & Restore | Manual backup trigger; restore from Google Drive |
| F-34 | Data Management | Delete session, delete profile, export before deletion |

---

## 3. System Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                        UI Layer                             │
│  Jetpack Compose Screens + ViewModels (MVVM)                │
└───────────────────┬─────────────────────────────────────────┘
                    │ StateFlow / Events
┌───────────────────▼─────────────────────────────────────────┐
│                    Domain Layer                             │
│  Use Cases: DetectSleepUseCase, CorrectSessionUseCase,      │
│  GetCalendarDataUseCase, ExportUseCase, SyncUseCase, etc.   │
└───────────────────┬─────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│                    Data Layer                               │
│  ┌──────────────┐  ┌───────────────┐  ┌──────────────────┐  │
│  │  Room DB     │  │  DataStore    │  │  Google Drive    │  │
│  │  (sessions,  │  │  (prefs,      │  │  API (sync /     │  │
│  │   profiles,  │  │   profiles,   │  │   backup)        │  │
│  │   tags)      │  │   settings)   │  │                  │  │
│  └──────────────┘  └───────────────┘  └──────────────────┘  │
└─────────────────────────────────────────────────────────────┘
                    │
┌───────────────────▼─────────────────────────────────────────┐
│               Background Services Layer                     │
│  ┌──────────────────────────┐  ┌───────────────────────┐    │
│  │  SleepDetectionService   │  │  WorkManager Tasks    │    │
│  │  (Foreground Service)    │  │  - SyncWorker         │    │
│  │                          │  │  - BackupWorker       │    │
│  │  Sensors consumed:       │  │  - SessionFinalizer   │    │
│  │  - Screen receiver       │  └───────────────────────┘    │
│  │  - Touch activity        │                               │
│  │  - Accelerometer         │                               │
│  │  - Ambient light         │                               │
│  └──────────────────────────┘                               │
└─────────────────────────────────────────────────────────────┘
```

**Single Activity, multiple Compose destinations via Navigation Compose.**  
**Hilt** for dependency injection throughout all layers.

---

## 4. Module Breakdown

The app is organized as a single-module project with feature-based packages (can be extracted to Gradle modules later).

| Package | Responsibility |
|---|---|
| `detection` | Sensor reading, sleep state machine, session builder |
| `data.local` | Room entities, DAOs, database definition |
| `data.remote` | Google Drive API wrapper, sync/backup DTOs |
| `data.repository` | Repository implementations |
| `domain.model` | Pure Kotlin domain models (SleepSession, Profile, Tag, etc.) |
| `domain.usecase` | One class per use case |
| `ui.calendar` | Calendar screen + ViewModel |
| `ui.daydetail` | Day detail sheet + ViewModel |
| `ui.correction` | Wake-up correction bottom sheet + ViewModel |
| `ui.insights` | Stats/insights screen + ViewModel |
| `ui.settings` | Settings screen + ViewModel |
| `ui.onboarding` | First-launch flow |
| `ui.profile` | Profile management screen |
| `ui.export` | Export screen + ViewModel |
| `ui.theme` | Color, typography, shapes (Material 3) |
| `widget` | Glance API home screen widget |
| `di` | Hilt modules (DatabaseModule, RepoModule, SensorModule, etc.) |
| `util` | Extensions, date formatters, constants |
| `worker` | WorkManager worker classes |
| `service` | SleepDetectionService (foreground) |

---

## 5. Automatic Sleep Detection Engine

### 5.1 Overview

The detection engine runs as a **foreground service** with a persistent (but minimal) notification during the active detection window. It is **not** always-on — it is started and stopped by a `WorkManager` periodic task that checks whether the current time falls within the profile's detection window.

### 5.2 Sleep State Machine

```
          ┌─────────┐
          │  IDLE   │ ◄──── Outside detection window
          └────┬────┘
               │ Window opens (e.g., 10:00 PM)
               ▼
          ┌─────────┐
          │WATCHING │ ◄──── Screen on, user active
          └────┬────┘
               │ Screen off + no touch for 5 min
               ▼
          ┌──────────────┐
          │ PRE_SLEEP    │ ◄──── Awaiting motion + light confirmation
          └────┬─────────┘
               │ Motion still + light dark for 20 min
               ▼
          ┌──────────────┐
          │  SLEEPING    │ ◄──── Session start timestamp recorded
          └────┬─────────┘
               │ Screen on (or significant motion sustained > 2 min)
               ▼
          ┌──────────────┐
          │ POST_SLEEP   │ ◄──── Awaiting confirmation this is a real wake
          └────┬─────────┘
               │ User interacts with phone > 3 min OR window closes
               ▼
          ┌──────────────┐
          │  FINALIZED   │ ◄──── Session written to DB; correction pending
          └──────────────┘
```

**States:**

- **IDLE:** Service is stopped. WorkManager restarts service when window opens.
- **WATCHING:** Service is running. Monitoring screen events and touch input.
- **PRE_SLEEP:** Screen has been off and untouched for 5+ minutes. Accelerometer and light sensor now sampled at 1 Hz to confirm stillness and darkness.
- **SLEEPING:** Confirmed sleep onset. `sleepOnsetTimestamp` = screen-off time minus brief debounce.
- **POST_SLEEP:** First screen-on detected. Monitoring if user stays awake.
- **FINALIZED:** `wakeTimestamp` confirmed. Session saved to Room DB with `correctionPending = true`.

### 5.3 Sensor Signals & Thresholds

| Signal | Source | Sleep Threshold | Wake Threshold |
|---|---|---|---|
| Screen state | `BroadcastReceiver` (ACTION_SCREEN_OFF / ON) | Screen OFF | Screen ON |
| Touch activity | `WindowManager` via `AccessibilityService` OR `UsageStatsManager` | No touch for 5 min | Any touch |
| Accelerometer | `SensorManager` (TYPE_ACCELEROMETER) | Magnitude delta < 0.05 m/s² over 20 min | Magnitude delta > 0.8 m/s² for 2 min |
| Ambient light | `SensorManager` (TYPE_LIGHT) | < 5 lux for 15 min | > 50 lux |

**Sensitivity levels** (user-configurable in Settings):

| Level | Motion Threshold | Screen-off Debounce | Min Confirmation Window |
|---|---|---|---|
| Low | < 0.10 m/s² | 10 min | 30 min |
| Medium (default) | < 0.05 m/s² | 5 min | 20 min |
| High | < 0.02 m/s² | 2 min | 15 min |

### 5.4 Confidence Score Calculation

Each session receives a confidence score (0–100) calculated at finalization:

```
score = 0
+ 30 if ambient light was below threshold for ≥ 80% of session
+ 30 if accelerometer was still for ≥ 80% of session
+ 20 if session duration ≥ 4 hours
+ 10 if session falls entirely within detection window
+ 10 if no mid-session phone use interruption
```

Sessions with score < 40 are flagged as `LOW_CONFIDENCE` and always prompt the correction flow, even if the user has previously dismissed it for high-confidence sessions.

### 5.5 Mid-Night Phone Use Handling

If during `SLEEPING` state, the screen turns on and the user actively uses the phone for > 5 minutes:

- If usage is < 15 minutes: session continues, usage gap is recorded as an interruption in metadata.
- If usage is ≥ 15 minutes: session is split into two candidate segments. The longer segment is kept as the primary session; the shorter may be discarded (if < 30 min) or saved as a separate session. User is shown both in the correction flow and can confirm, merge, or discard.

### 5.6 Nap vs Night Sleep Classification

```
Session duration 30 min – 3 hrs AND start time between 10:00 AM – 8:00 PM
→ classified as NAP

Session duration ≥ 30 min AND start time within detection window (default 10PM–8AM)
→ classified as NIGHT_SLEEP
```

Both types are stored in the same table with a `sessionType` field.

---

## 6. Data Models

### 6.1 Domain Models (Kotlin)

```kotlin
data class Profile(
    val id: String,               // UUID
    val name: String,
    val avatarEmoji: String,      // e.g., "😴"
    val sleepGoalMinutes: Int,    // e.g., 480 for 8 hours
    val detectionWindowStart: LocalTime,  // e.g., 22:00
    val detectionWindowEnd: LocalTime,    // e.g., 08:00
    val sensitivity: DetectionSensitivity,
    val createdAt: Instant
)

enum class DetectionSensitivity { LOW, MEDIUM, HIGH }

data class SleepSession(
    val id: String,               // UUID
    val profileId: String,
    val sleepOnset: Instant,      // bed time (when sleep detected/set)
    val wakeTime: Instant,        // wake time (when wake detected/set)
    val durationMinutes: Int,     // computed = wakeTime - sleepOnset
    val sessionType: SessionType,
    val source: SessionSource,
    val confidenceScore: Int,     // 0–100; null if manually entered
    val correctionPending: Boolean,
    val qualityScore: Int?,       // null until user rates or computed
    val interruptions: List<SleepInterruption>,
    val tags: List<String>,
    val notes: String?,
    val createdAt: Instant,
    val lastModifiedAt: Instant
)

enum class SessionType { NIGHT_SLEEP, NAP }
enum class SessionSource { AUTO_DETECTED, MANUAL, AUTO_CORRECTED }

data class SleepInterruption(
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int
)

data class SleepTag(
    val id: String,
    val profileId: String,
    val label: String,
    val emoji: String,
    val isDefault: Boolean        // pre-seeded tags vs user-created
)
```

---

## 7. Database Schema

**Room Database** — `SolaceDatabase.kt`  
**Version:** 1 (migration strategy documented for future versions)

### Tables

#### `profiles`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT PK | UUID |
| `name` | TEXT | |
| `avatar_emoji` | TEXT | |
| `sleep_goal_minutes` | INTEGER | |
| `window_start_hour` | INTEGER | 0–23 |
| `window_start_minute` | INTEGER | 0–59 |
| `window_end_hour` | INTEGER | |
| `window_end_minute` | INTEGER | |
| `sensitivity` | TEXT | ENUM string |
| `created_at` | INTEGER | epoch millis |

#### `sleep_sessions`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT PK | UUID |
| `profile_id` | TEXT FK → profiles | |
| `sleep_onset_ms` | INTEGER | epoch millis |
| `wake_time_ms` | INTEGER | epoch millis |
| `duration_minutes` | INTEGER | |
| `session_type` | TEXT | NIGHT_SLEEP / NAP |
| `source` | TEXT | AUTO / MANUAL / AUTO_CORRECTED |
| `confidence_score` | INTEGER | nullable |
| `correction_pending` | INTEGER | 0 or 1 (boolean) |
| `quality_score` | INTEGER | nullable |
| `interruptions_json` | TEXT | serialized JSON array |
| `notes` | TEXT | nullable |
| `created_at` | INTEGER | |
| `last_modified_at` | INTEGER | |

Index on `(profile_id, sleep_onset_ms)` for fast calendar queries.

#### `session_tags`

| Column | Type | Notes |
|---|---|---|
| `session_id` | TEXT FK → sleep_sessions | |
| `tag_id` | TEXT FK → sleep_tags | |
| Composite PK on `(session_id, tag_id)` | | |

#### `sleep_tags`

| Column | Type | Notes |
|---|---|---|
| `id` | TEXT PK | UUID |
| `profile_id` | TEXT FK → profiles | |
| `label` | TEXT | |
| `emoji` | TEXT | |
| `is_default` | INTEGER | 0 or 1 |

### Pre-seeded Default Tags

`Stressed 😰`, `Caffeine ☕`, `Exercise 🏃`, `Sick 🤒`, `Late Night 🌙`, `Nap 💤`, `Travel ✈️`, `Weekend 🎉`

---

## 8. Background Services & WorkManager

### 8.1 SleepDetectionService (Foreground Service)

**Type:** `ForegroundService` with `foregroundServiceType = "dataSync"` (API 29+)  
**Notification:** Persistent, minimal — "Solace is monitoring sleep" with dismiss option (dismiss stops service until next window).  
**Lifecycle:** Started by `WindowManagerWorker`; stops itself when window closes or session is finalized.

```
Service responsibilities:
1. Register BroadcastReceiver for SCREEN_ON / SCREEN_OFF
2. Register SensorEventListener for accelerometer + ambient light
3. Maintain sleep state machine
4. Write finalized sessions to Room via Repository
5. Set correctionPending = true flag
6. Stop self after session finalized (or window closes)
```

### 8.2 WorkManager Tasks

| Worker | Schedule | Responsibility |
|---|---|---|
| `DetectionWindowWorker` | Every 30 minutes (PeriodicWork) | Checks if current time is in detection window; starts/stops SleepDetectionService |
| `SessionFinalizerWorker` | Once daily at 9:00 AM | Ensures any orphaned in-progress sessions from the previous night are properly closed |
| `SyncWorker` | Every 6 hours (when network available) | Pushes new/modified sessions to Google Drive App Data folder |
| `BackupWorker` | Weekly | Full local DB export encrypted backup to Google Drive |
| `StaleSessionCleanupWorker` | Daily at 3:00 AM | Discards LOW_CONFIDENCE sessions older than 1 day that were never confirmed by user |

All workers use `ExistingPeriodicWorkPolicy.KEEP` to avoid duplicate scheduling.

### 8.3 BroadcastReceiver — BootReceiver

Registered in manifest for `BOOT_COMPLETED` and `ACTION_MY_PACKAGE_REPLACED`. On receipt, re-enqueues all WorkManager tasks and restarts service if currently in detection window.

---

## 9. Permissions

| Permission | Purpose | Required / Optional | When Requested |
|---|---|---|---|
| `FOREGROUND_SERVICE` | Run SleepDetectionService | Required | Automatic (manifest) |
| `FOREGROUND_SERVICE_DATA_SYNC` | API 34+ foreground type | Required | Automatic (manifest) |
| `RECEIVE_BOOT_COMPLETED` | Re-schedule on boot | Required | Automatic (manifest) |
| `ACTIVITY_RECOGNITION` | Accelerometer via Activity Recognition API (API 29+) | Required | Onboarding step 2 |
| `HIGH_SAMPLING_RATE_SENSORS` | Accelerometer > 200 Hz sampling | Optional | Requested if available |
| `POST_NOTIFICATIONS` | Foreground service notification (API 33+) | Required | Onboarding step 1 |
| `INTERNET` | Google Drive sync | Optional | When user enables sync |
| `ACCESS_NETWORK_STATE` | Check network before sync | Optional | Automatic (manifest) |
| `REQUEST_INSTALL_PACKAGES` | Not needed | — | — |

**Permission Rationale strings** are included in `strings.xml` for every runtime permission with a clear, non-technical user-facing explanation shown before the system dialog.

---

## 10. UI Screens & Navigation

### Navigation Graph (Type-Safe Compose Navigation)

```
OnboardingFlow (conditional — only first launch)
    └── Welcome → ProfileCreate → PermissionsSetup → DetectionWindowSetup → Done

MainGraph (NavHost)
    ├── CalendarScreen (start destination)
    │       └── → DayDetailSheet (bottom sheet)
    │               └── → SessionEditScreen
    ├── InsightsScreen
    ├── ProfileScreen
    │       └── → ProfileEditScreen
    │       └── → ProfileCreateScreen
    ├── SettingsScreen
    │       └── → ExportScreen
    │       └── → SyncSettingsScreen
    │       └── → BackupRestoreScreen
    └── CorrectionSheet (modal bottom sheet, shown on app open if correctionPending)
```

### Screen Descriptions

#### CalendarScreen
Main home screen. Shows a monthly calendar grid where each day cell displays a color-coded sleep bar. Bottom tab / top navigation leads to Insights and Profile.

#### CorrectionSheet (Modal Bottom Sheet)
Appears automatically on app open when `correctionPending = true` for the most recent session. Cannot be fully dismissed until user taps "Looks right" or "Fix it". If user taps outside, sheet re-appears on next open (not permanently dismissible without acting).

Contains:
- Summary card: "You slept approximately X hrs last night"
- Detected bed time + wake time (auto-filled)
- Two time picker rows: **Went to sleep** / **Woke up**
- Session type toggle: Night Sleep / Nap
- "Looks right" button (confirms as-is)
- "Fix it" expands inline time pickers
- After confirmation: quality rating prompt (1–5 stars, skippable)
- Tag selector (horizontal chip row)
- Optional note field

#### DayDetailSheet
Tapping a calendar day opens a sheet showing:
- Sleep bar visualization for that day
- Bed time, wake time, duration
- Quality score (stars)
- Tags
- Notes
- Source badge (Auto / Manual / Corrected)
- Confidence indicator (only visible if AUTO source; shown as a subtle dot)
- Edit button → SessionEditScreen
- Delete button (with confirmation dialog)

#### InsightsScreen
Tabs: **Weekly** | **Monthly** | **All Time**

Sections per tab:
- Average sleep duration (vs goal — shown as progress ring)
- Sleep debt (cumulative hours below goal)
- Best/worst nights
- Streak: consecutive nights hitting sleep goal
- Missed nights: days with no recorded session
- Tag correlation: most common tag on nights with low duration
- Sleep quality trend (line chart — Vico or MPAndroidChart)
- Weekday vs weekend comparison (bar chart)
- Sleep consistency score (how regular the bed/wake time is, as a percentage)

---

## 11. Calendar View Specification

### Layout

- **Default view:** Monthly grid (7 columns × 5–6 rows)
- **Each day cell** shows:
  - Date number (top left)
  - Sleep bar: vertical or arc-style bar representing duration, color-coded by quality score
  - Small dot if session was manually corrected
  - Dimmed if no session recorded; "?" icon if phone was not used that night (ambiguous — no detection possible)
- Swipe left/right to navigate months
- Tap any day → DayDetailSheet

### Color Coding (by sleep duration vs goal)

| Duration vs Goal | Color |
|---|---|
| ≥ 100% of goal | Green (#4CAF50) |
| 80–99% | Teal (#009688) |
| 60–79% | Amber (#FFC107) |
| < 60% | Red (#F44336) |
| No data | Surface/muted gray |

### Weekly Strip View (optional toggle)

User can toggle from monthly grid to a horizontal weekly strip showing a more detailed bar chart per day. This view also shows the exact hours as a timeline (midnight to midnight) with the sleep block shaded in.

---

## 12. Multi-Profile System

### Profile Rules

- Maximum 6 profiles per device.
- Each profile has: name (max 20 chars), avatar emoji, sleep goal, detection window, sensitivity setting.
- Profiles are stored in Room and in DataStore (active profile ID persisted across app restarts).
- All sleep sessions, tags, and settings are scoped by `profileId`.
- Switching profiles: tap avatar/name at top of any screen → profile switcher bottom sheet.
- The detection service always operates for the **active profile** at the time the detection window opens. If the user switches profiles mid-window, the service uses whichever profile was active when the window opened (no mid-night profile switching supported — a warning is shown if user tries).

### Profile Data Isolation

- Each profile's data is entirely isolated in Room (all queries include `WHERE profile_id = ?`).
- Export, sync, and backup are performed per-profile (user selects which profiles to include).

---

## 13. Notification Strategy

Solace follows a **silent-first** philosophy — no intrusive notifications for sleep data. The only mandatory notification is the foreground service notification while detection is active.

### Notification Channels

| Channel ID | Name | Importance | Purpose |
|---|---|---|---|
| `ch_detection` | "Sleep Detection" | LOW | Foreground service persistent notification |
| `ch_correction` | "Sleep Review" | DEFAULT | Shown only if app has not been opened for > 18 hrs after a session is finalized (optional reminder that a review is pending) |
| `ch_weekly` | "Weekly Summary" | LOW | Optional weekly summary notification (user opt-in in Settings) |

### Correction Reminder Notification

- Sent at most **once per session**.
- Only sent if user has NOT opened the app since session was finalized AND it has been > 18 hours.
- Tapping opens the app directly to the CorrectionSheet.
- No sound, no vibration by default.

### Weekly Summary Notification (Optional)

- Sent Sunday evening at 8 PM (configurable).
- Shows: average sleep last week, streak count, longest sleep.
- User can disable in Settings.

---

## 14. Cloud Sync & Backup

### 14.1 Google Account Sign-In

- Uses **Google Sign-In (Credential Manager API)** — modern approach for API 29+.
- Scopes requested: `Drive.APPDATA` (hidden app-specific folder — user cannot see these files in Google Drive).
- Sign-in is optional; prompted in Settings → Sync.

### 14.2 Sync Strategy

- **What is synced:** All `SleepSession` records and `Profile` metadata. Tags are synced as part of session records.
- **Format:** JSON files per profile, named `profile_{uuid}_sessions.json`.
- **Conflict resolution:** Last-write-wins based on `lastModifiedAt` timestamp. Merge is NOT attempted — latest version of each session wins.
- **Frequency:** Every 6 hours via `SyncWorker` (only when Wi-Fi or unmetered network).
- **On correction:** Session is synced immediately after correction is confirmed (one-time sync trigger).

### 14.3 Backup & Restore

- **Backup format:** Single ZIP containing JSON exports of all profiles + sessions.
- **Backup location:** Google Drive App Data folder, file named `solace_backup_{timestamp}.zip`.
- **Retention:** Last 5 backups kept; older ones deleted automatically.
- **Restore:** User taps "Restore from Backup" → sees list of available backups with dates → selects one → local DB is replaced after confirmation.
- **Manual trigger:** Available in Settings → Backup & Restore.
- **Automatic:** Weekly via `BackupWorker`.

---

## 15. Statistics & Insights Engine

All statistics are computed in `InsightsRepository` using pure SQL queries on Room, exposed as `Flow<InsightsData>`.

### 15.1 Computed Metrics

| Metric | Formula | Scope |
|---|---|---|
| Average sleep duration | `SUM(duration_minutes) / COUNT(sessions)` | Weekly / Monthly / All-time |
| Sleep debt | `SUM(goal_minutes - duration_minutes)` for nights below goal | Rolling 7 days |
| Sleep consistency score | `100 - (stddev(sleep_onset_hour) + stddev(wake_hour)) * 5` clamped 0–100 | Weekly |
| Current streak | Consecutive days where duration ≥ goal | All-time |
| Longest streak | Historical max consecutive days at goal | All-time |
| Missed nights | Days in range with no session and phone was in use that day (inferred from UsageStats) | Monthly |
| Tag frequency | Count of each tag across sessions in range | Per period |
| Quality score average | `AVG(quality_score)` | Per period |
| Weekday avg | Filter sessions where `STRFTIME('%w', session_date) IN (1,2,3,4,5)` | Monthly |
| Weekend avg | Filter sessions where `STRFTIME('%w', session_date) IN (0,6)` | Monthly |

### 15.2 Quality Score Computation

If the user has not manually rated a session, an automatic quality score is estimated:

```
quality = 50 (base)
+ 20 if duration ≥ goal
+ 10 if duration between goal and goal + 1 hr
- 10 if > 2 interruptions
- 20 if duration < 60% of goal
+ 10 if sleep onset consistent with last 7-day avg (within ±30 min)
+ 10 if confidence score ≥ 80
clamped to 0–100, then mapped to 1–5 stars
```

---

## 16. Export System

### 16.1 CSV Export

**File name:** `solace_{profile_name}_{date_range}.csv`

**Columns:**  
`Date`, `Bed Time`, `Wake Time`, `Duration (hrs)`, `Session Type`, `Source`, `Quality Score`, `Tags`, `Notes`, `Confidence Score`, `Interruptions Count`

Each row = one sleep session. Interruption details are included as a separate sheet / appended section.

### 16.2 PDF Export

**Library:** iText or Apache PDFBox (lightweight wrapper)  
**Format:** A4 portrait  

**Sections:**
1. Cover: Profile name, date range, generated date
2. Summary table: total sessions, avg duration, sleep debt, streak
3. Monthly calendar visualization (rendered as table with color-coded cells)
4. Per-session table (same columns as CSV)
5. Charts: duration trend line, weekday vs weekend bar chart (rendered as SVG into PDF)

**Delivery:** Share sheet (Android `ShareCompat`) — user can save to Files, share via email, etc.

### 16.3 Export Screen UI

- Profile selector (pre-filled with active profile)
- Date range picker (presets: Last 7 days, Last 30 days, Last 3 months, Custom)
- Format toggle: CSV / PDF
- Include charts toggle (PDF only)
- "Export" button → progress indicator → share sheet

---

## 17. Tech Stack & Dependencies

### Core

| Library | Purpose | Version (approx.) |
|---|---|---|
| Kotlin | Language | 2.0+ |
| Jetpack Compose | UI | BOM 2025.x |
| Material 3 | Design system | 1.3+ |
| Hilt | Dependency injection | 2.51+ |
| KSP | Annotation processing | 2.0+ |
| Coroutines | Async / concurrency | 1.8+ |
| Room | Local database | 2.6+ |
| DataStore (Preferences) | Settings storage | 1.1+ |
| Navigation Compose | Screen routing | 2.8+ |
| WorkManager | Background tasks | 2.9+ |
| Kotlinx Serialization | JSON | 1.7+ |

### Sensors & System

| Library | Purpose |
|---|---|
| Android SensorManager | Accelerometer, ambient light |
| BroadcastReceiver | Screen on/off |
| UsageStatsManager | Phone usage inferred for "missed nights" |
| Credential Manager | Google Sign-In |
| Google Drive API (REST) | Sync and backup |

### UI Extras

| Library | Purpose |
|---|---|
| Glance API | Home screen widget |
| Vico | Sleep duration charts (line + bar) |
| Accompanist Permissions | Runtime permission handling |
| Coil | Image loading (profile avatars if photo-based in future) |

### Export

| Library | Purpose |
|---|---|
| Apache PDFBox Android | PDF generation |
| OpenCSV or Kotlin CSV | CSV generation |

### Testing

| Library | Purpose |
|---|---|
| JUnit 5 | Unit tests |
| MockK | Mocking |
| Turbine | Flow testing |
| Espresso | UI tests |
| Hilt Testing | DI in tests |
| Robolectric | Android unit tests without emulator |

---

## 18. Project Folder Structure

```
app/src/main/
├── java/com.solace.sleep/
│   ├── SolaceApp.kt                  ← @HiltAndroidApp
│   ├── MainActivity.kt               ← @AndroidEntryPoint, NavHost root
│   │
│   ├── di/
│   │   ├── DatabaseModule.kt
│   │   ├── RepositoryModule.kt
│   │   ├── SensorModule.kt
│   │   └── NetworkModule.kt
│   │
│   ├── data/
│   │   ├── local/
│   │   │   ├── SolaceDatabase.kt
│   │   │   ├── entity/
│   │   │   │   ├── ProfileEntity.kt
│   │   │   │   ├── SleepSessionEntity.kt
│   │   │   │   └── SleepTagEntity.kt
│   │   │   └── dao/
│   │   │       ├── ProfileDao.kt
│   │   │       ├── SleepSessionDao.kt
│   │   │       └── TagDao.kt
│   │   ├── remote/
│   │   │   ├── DriveApiClient.kt
│   │   │   └── dto/
│   │   │       └── SyncDto.kt
│   │   ├── preferences/
│   │   │   └── AppPreferences.kt     ← DataStore wrapper
│   │   └── repository/
│   │       ├── SleepSessionRepository.kt
│   │       ├── ProfileRepository.kt
│   │       ├── InsightsRepository.kt
│   │       └── SyncRepository.kt
│   │
│   ├── domain/
│   │   ├── model/
│   │   │   ├── SleepSession.kt
│   │   │   ├── Profile.kt
│   │   │   ├── SleepTag.kt
│   │   │   └── InsightsData.kt
│   │   └── usecase/
│   │       ├── GetCalendarDataUseCase.kt
│   │       ├── CorrectSessionUseCase.kt
│   │       ├── GetInsightsUseCase.kt
│   │       ├── ExportSessionsUseCase.kt
│   │       ├── SyncUseCase.kt
│   │       └── ManageProfilesUseCase.kt
│   │
│   ├── detection/
│   │   ├── SleepDetectionService.kt   ← Foreground service
│   │   ├── SleepStateMachine.kt
│   │   ├── SensorSampler.kt           ← Accelerometer + light readings
│   │   ├── ScreenStateReceiver.kt     ← BroadcastReceiver
│   │   └── SessionBuilder.kt          ← Assembles raw data into SleepSession
│   │
│   ├── service/
│   │   └── BootReceiver.kt
│   │
│   ├── worker/
│   │   ├── DetectionWindowWorker.kt
│   │   ├── SessionFinalizerWorker.kt
│   │   ├── SyncWorker.kt
│   │   ├── BackupWorker.kt
│   │   └── StaleSessionCleanupWorker.kt
│   │
│   ├── ui/
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Typography.kt
│   │   │   └── Theme.kt
│   │   ├── navigation/
│   │   │   ├── AppNavGraph.kt
│   │   │   └── Routes.kt
│   │   ├── onboarding/
│   │   │   ├── OnboardingScreen.kt
│   │   │   └── OnboardingViewModel.kt
│   │   ├── calendar/
│   │   │   ├── CalendarScreen.kt
│   │   │   └── CalendarViewModel.kt
│   │   ├── correction/
│   │   │   ├── CorrectionSheet.kt
│   │   │   └── CorrectionViewModel.kt
│   │   ├── daydetail/
│   │   │   ├── DayDetailSheet.kt
│   │   │   └── DayDetailViewModel.kt
│   │   ├── insights/
│   │   │   ├── InsightsScreen.kt
│   │   │   └── InsightsViewModel.kt
│   │   ├── profile/
│   │   │   ├── ProfileScreen.kt
│   │   │   └── ProfileViewModel.kt
│   │   ├── settings/
│   │   │   ├── SettingsScreen.kt
│   │   │   └── SettingsViewModel.kt
│   │   └── export/
│   │       ├── ExportScreen.kt
│   │       └── ExportViewModel.kt
│   │
│   ├── widget/
│   │   └── SleepSummaryWidget.kt      ← Glance API
│   │
│   └── util/
│       ├── TimeExtensions.kt
│       ├── DateFormatter.kt
│       └── Constants.kt
│
└── res/
    ├── drawable/
    ├── xml/
    │   ├── network_security_config.xml
    │   └── provider_paths.xml          ← FileProvider for share sheet
    └── values/
        ├── strings.xml
        ├── colors.xml
        └── themes.xml
```

---

## 19. Key Algorithms

### 19.1 Sleep Onset Timestamp Calculation

When state transitions to `SLEEPING`, the onset time is NOT the exact moment of transition — it is backdated:

```
sleep_onset = screen_off_timestamp - PRE_SLEEP_DEBOUNCE_MINUTES
```

This accounts for the fact that users typically fall asleep a few minutes after putting the phone down. Default debounce: 5 minutes. Configurable per sensitivity level.

### 19.2 Wake Timestamp Calculation

```
wake_time = first_screen_on_timestamp (from POST_SLEEP transition)
```

No backdating applied to wake time since screen-on is the most accurate proxy for waking.

### 19.3 Detection Window Spanning Midnight

The detection window can span midnight (e.g., 10 PM – 8 AM). Logic for window check:

```kotlin
fun isWithinDetectionWindow(now: LocalTime, start: LocalTime, end: LocalTime): Boolean {
    return if (start > end) {
        // Spans midnight
        now >= start || now <= end
    } else {
        now in start..end
    }
}
```

### 19.4 Sleep Debt Rolling Calculation

```kotlin
fun calculateSleepDebt(sessions: List<SleepSession>, goalMinutes: Int, days: Int): Int {
    val today = LocalDate.now()
    val windowStart = today.minusDays(days.toLong())
    
    val sessionMap = sessions
        .filter { it.sessionType == NIGHT_SLEEP }
        .groupBy { it.sleepOnset.toLocalDate() }
    
    var debt = 0
    for (day in windowStart..today) {
        val dayDuration = sessionMap[day]?.sumOf { it.durationMinutes } ?: 0
        val deficit = goalMinutes - dayDuration
        if (deficit > 0) debt += deficit
    }
    return debt // in minutes
}
```

---

## 20. Edge Cases & Failure Handling

| Scenario | Handling |
|---|---|
| User never opens app to confirm | `StaleSessionCleanupWorker` auto-confirms LOW_CONFIDENCE sessions after 48 hrs; HIGH_CONFIDENCE sessions auto-confirmed after 7 days if uncorrected |
| Phone dies during sleep | `SessionFinalizerWorker` at 9 AM checks for open (finalized=false) sessions and closes them using last known accelerometer timestamp |
| User charges phone overnight away from them | Motion and light signals will still confirm sleep; if phone is in another room, all signals will be flat — session confidence will be MEDIUM at best |
| Two profiles, one phone | Each profile must be active when their respective detection window opens. If profiles have overlapping windows, a warning is shown in Settings. Only the active profile's session is tracked at any time |
| No sessions for many days | Calendar shows empty/dimmed cells; Insights show "Not enough data" placeholder for streak/trend computations |
| Session under 30 minutes | Discarded silently with a log entry; not shown to user |
| Sensor unavailable (some devices lack ambient light) | App functions without that sensor; confidence score cap is reduced by 30 points |
| Google Drive sync fails | Retry with exponential backoff (WorkManager handles); data is always safe locally |
| Room migration | Every schema change increments DB version; migration scripts provided; fallback = `fallbackToDestructiveMigration()` is NOT used — migrations must be explicit |
| User manually adds session for a day already auto-detected | Auto session is preserved; manual session added alongside; UI shows both and prompts user to keep one or merge |

---

## 21. Testing Strategy

### Unit Tests

- `SleepStateMachine` — test all state transitions with mock sensor inputs
- `SessionBuilder` — verify onset/wake calculation with edge cases (midnight spanning, interruptions)
- `InsightsRepository` — verify all metric calculations against known datasets
- `SyncRepository` — mock Drive API; verify merge logic
- All Use Cases — pure Kotlin tests with MockK

### Integration Tests

- `SleepSessionDao` — Room in-memory database tests (full CRUD + complex queries)
- `ProfileDao` — isolation between profiles

### UI Tests (Espresso / Compose Testing)

- CorrectionSheet — verify time picker updates, confirm flow
- CalendarScreen — verify session color rendering
- Navigation — verify all routes reachable and back stack correct

### Manual QA Scenarios

- Leave phone face-down in dark room for 8 hours → verify session created
- Use phone for 20 minutes at 2 AM → verify interruption recorded or session split
- Switch profiles mid-day → verify no data contamination
- Kill app during detection → verify `SessionFinalizerWorker` recovers session
- Restore backup on fresh install → verify all profiles and sessions present

---

## 22. Open Questions / Future Considerations

These items are out of scope for v1.0 but should be kept in mind during architecture decisions:

| # | Topic | Notes |
|---|---|---|
| OQ-1 | Wearable integration | If user has WearOS watch, heart rate + movement from watch would dramatically improve accuracy. Architecture supports pluggable sensor sources — `SensorSampler` can be extended. |
| OQ-2 | Machine learning model | Replace threshold-based detection with an on-device ML model trained on confirmed sessions. TFLite model could be shipped as an update without changing app architecture. |
| OQ-3 | Health Connect integration | Android Health Connect API allows reading/writing sleep data shared with Google Fit and other health apps. Could be added as a sync target in v1.1. |
| OQ-4 | Sleep stage detection | Estimating light/deep/REM stages requires motion data at higher fidelity; feasible with accelerometer but requires model training. |
| OQ-5 | Alarm integration | Smart alarm that wakes the user at the optimal point in a sleep cycle within a 30-minute window. Would require additional wake-window logic on top of existing state machine. |
| OQ-6 | Bangla localization | Architecture uses standard Android string resources — adding a `values-bn/strings.xml` is the only change needed. |

---

*Document prepared for the Solace Android app — v1.0 specification. All decisions are locked as agreed during requirements gathering. Implementation should follow this document as the source of truth.*
