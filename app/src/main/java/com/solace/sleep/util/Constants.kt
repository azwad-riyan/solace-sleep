package com.solace.sleep.util

object Constants {
    // Database
    const val DATABASE_NAME = "solace_database.db"

    // DataStore
    const val PREFERENCES_NAME = "app_preferences"

    // Notification Channels
    const val CHANNEL_DETECTION = "sleep_detection"
    const val CHANNEL_CORRECTION = "sleep_correction"
    const val CHANNEL_SYNC = "sync_backup"

    // Notification IDs
    const val NOTIF_DETECTION_ID = 1001
    const val NOTIF_CORRECTION_ID = 1002
    const val NOTIF_SYNC_ID = 1003

    // WorkManager tags
    const val WORK_DETECTION_WINDOW = "detection_window_work"
    const val WORK_SESSION_FINALIZER = "session_finalizer_work"
    const val WORK_SYNC = "sync_work"
    const val WORK_BACKUP = "backup_work"
    const val WORK_STALE_CLEANUP = "stale_cleanup_work"

    // Sleep Detection
    const val DETECTION_INTERVAL_MINUTES = 30L
    const val ACCEL_MAGNITUDE_THRESHOLD_LOW = 0.08f      // m/s²
    const val ACCEL_MAGNITUDE_THRESHOLD_MEDIUM = 0.05f
    const val ACCEL_MAGNITUDE_THRESHOLD_HIGH = 0.03f
    const val ACCEL_STILL_DURATION_MINUTES_LOW = 25L
    const val ACCEL_STILL_DURATION_MINUTES_MEDIUM = 20L
    const val ACCEL_STILL_DURATION_MINUTES_HIGH = 15L
    const val LIGHT_LUX_THRESHOLD = 5f
    const val LIGHT_DARK_DURATION_MINUTES = 15L
    const val PRE_SLEEP_CONFIDENCE_THRESHOLD = 40
    const val SLEEP_CONFIDENCE_THRESHOLD = 65
    const val WAKE_MOVEMENT_THRESHOLD_MEDIUM = 0.15f

    // Session constraints
    const val MIN_NAP_MINUTES = 20
    const val MIN_NIGHT_SLEEP_MINUTES = 120
    const val MAX_SESSION_HOURS = 14
    const val NIGHT_SLEEP_HOUR_THRESHOLD = 3 // sessions > 3hrs after 6pm classified as night

    // Sensor sampling
    const val SENSOR_BATCH_LATENCY_US = 5_000_000 // 5 seconds
    const val SENSOR_SAMPLE_PERIOD_US = 500_000   // 0.5 seconds

    // Drive Sync
    const val SYNC_INTERVAL_HOURS = 6L
    const val BACKUP_INTERVAL_DAYS = 7L
    const val DRIVE_FOLDER_NAME = "SolaceSleepBackup"
    const val BACKUP_FILE_PREFIX = "solace_backup_"

    // File provider authority suffix
    const val FILE_PROVIDER_SUFFIX = ".fileprovider"

    // Default profile
    const val DEFAULT_SLEEP_GOAL_MINUTES = 480 // 8 hours
    const val DEFAULT_DETECTION_START_HOUR = 21
    const val DEFAULT_DETECTION_START_MINUTE = 0
    const val DEFAULT_DETECTION_END_HOUR = 10
    const val DEFAULT_DETECTION_END_MINUTE = 0
}
