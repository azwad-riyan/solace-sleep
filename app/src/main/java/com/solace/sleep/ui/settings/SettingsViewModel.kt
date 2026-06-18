package com.solace.sleep.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SyncRepository
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.domain.model.Profile
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class SettingsUiState(
    val profile: Profile? = null,
    val driveSyncEnabled: Boolean = false,
    val driveAccountEmail: String? = null,
    val lastSyncTimestamp: Long? = null,
    val isSyncing: Boolean = false,
    val syncError: String? = null
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferences: AppPreferences,
    private val syncRepository: SyncRepository
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        profileRepository.observeActiveProfile(),
        preferences.driveSyncEnabled,
        preferences.driveAccountEmail,
        preferences.lastSyncTimestamp
    ) { profile, syncEnabled, email, lastSync ->
        SettingsUiState(
            profile = profile,
            driveSyncEnabled = syncEnabled,
            driveAccountEmail = email,
            lastSyncTimestamp = lastSync
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), SettingsUiState())

    fun updateSleepGoal(minutes: Int) {
        viewModelScope.launch {
            val profile = uiState.value.profile ?: return@launch
            profileRepository.updateProfile(profile.copy(sleepGoalMinutes = minutes))
        }
    }

    fun updateDetectionWindow(start: LocalTime, end: LocalTime) {
        viewModelScope.launch {
            val profile = uiState.value.profile ?: return@launch
            profileRepository.updateProfile(
                profile.copy(detectionWindowStart = start, detectionWindowEnd = end)
            )
        }
    }

    fun updateSensitivity(sensitivity: DetectionSensitivity) {
        viewModelScope.launch {
            val profile = uiState.value.profile ?: return@launch
            profileRepository.updateProfile(profile.copy(sensitivity = sensitivity))
        }
    }

    fun toggleDriveSync(enabled: Boolean) {
        viewModelScope.launch { preferences.setDriveSyncEnabled(enabled) }
    }

    fun syncNow(authToken: String) {
        viewModelScope.launch {
            syncRepository.syncToCloud(authToken)
        }
    }
}
