package com.solace.sleep.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

@Singleton
class AppPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.dataStore

    companion object {
        val KEY_ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val KEY_ACTIVE_PROFILE_ID = stringPreferencesKey("active_profile_id")
        val KEY_DRIVE_SYNC_ENABLED = booleanPreferencesKey("drive_sync_enabled")
        val KEY_DRIVE_ACCOUNT_EMAIL = stringPreferencesKey("drive_account_email")
        val KEY_LAST_SYNC_TIMESTAMP = longPreferencesKey("last_sync_timestamp")
        val KEY_LAST_BACKUP_TIMESTAMP = longPreferencesKey("last_backup_timestamp")
        val KEY_DETECTION_SERVICE_RUNNING = booleanPreferencesKey("detection_service_running")
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_ONBOARDING_COMPLETE] ?: false
    }

    val activeProfileId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_ACTIVE_PROFILE_ID]
    }

    val driveSyncEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DRIVE_SYNC_ENABLED] ?: false
    }

    val driveAccountEmail: Flow<String?> = dataStore.data.map { prefs ->
        prefs[KEY_DRIVE_ACCOUNT_EMAIL]
    }

    val lastSyncTimestamp: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_SYNC_TIMESTAMP]
    }

    val lastBackupTimestamp: Flow<Long?> = dataStore.data.map { prefs ->
        prefs[KEY_LAST_BACKUP_TIMESTAMP]
    }

    val detectionServiceRunning: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[KEY_DETECTION_SERVICE_RUNNING] ?: false
    }

    suspend fun setOnboardingComplete(complete: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setActiveProfileId(profileId: String) {
        dataStore.edit { prefs -> prefs[KEY_ACTIVE_PROFILE_ID] = profileId }
    }

    suspend fun setDriveSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_DRIVE_SYNC_ENABLED] = enabled }
    }

    suspend fun setDriveAccountEmail(email: String?) {
        dataStore.edit { prefs ->
            if (email != null) prefs[KEY_DRIVE_ACCOUNT_EMAIL] = email
            else prefs.remove(KEY_DRIVE_ACCOUNT_EMAIL)
        }
    }

    suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { prefs -> prefs[KEY_LAST_SYNC_TIMESTAMP] = timestamp }
    }

    suspend fun setLastBackupTimestamp(timestamp: Long) {
        dataStore.edit { prefs -> prefs[KEY_LAST_BACKUP_TIMESTAMP] = timestamp }
    }

    suspend fun setDetectionServiceRunning(running: Boolean) {
        dataStore.edit { prefs -> prefs[KEY_DETECTION_SERVICE_RUNNING] = running }
    }

    suspend fun clearAll() {
        dataStore.edit { it.clear() }
    }
}
