package com.solace.sleep.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.domain.model.Profile
import com.solace.sleep.domain.usecase.ManageProfileUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

data class ProfileUiState(
    val profiles: List<Profile> = emptyList(),
    val activeProfileId: String? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val manageProfileUseCase: ManageProfileUseCase,
    private val preferences: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<ProfileUiState> = combine(
        manageProfileUseCase.observeAll(),
        preferences.activeProfileId
    ) { profiles, activeId ->
        ProfileUiState(profiles = profiles, activeProfileId = activeId, isLoading = false)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), ProfileUiState())

    fun createProfile(name: String, emoji: String) {
        viewModelScope.launch {
            manageProfileUseCase.create(name, emoji)
        }
    }

    fun saveProfile(profile: Profile) {
        viewModelScope.launch { manageProfileUseCase.save(profile) }
    }

    fun updateProfile(profile: Profile) {
        viewModelScope.launch { manageProfileUseCase.update(profile) }
    }

    fun deleteProfile(profileId: String) {
        viewModelScope.launch { manageProfileUseCase.delete(profileId) }
    }

    fun switchToProfile(profileId: String) {
        viewModelScope.launch { manageProfileUseCase.switchTo(profileId) }
    }
}
