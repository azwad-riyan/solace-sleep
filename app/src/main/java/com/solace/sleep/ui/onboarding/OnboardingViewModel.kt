package com.solace.sleep.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.ProfileRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalTime
import javax.inject.Inject

enum class OnboardingStep {
    WELCOME, PROFILE_CREATE, PERMISSIONS, DETECTION_WINDOW, DONE
}

data class OnboardingState(
    val step: OnboardingStep = OnboardingStep.WELCOME,
    val profileName: String = "",
    val profileEmoji: String = "😴",
    val detectionStart: LocalTime = LocalTime.of(21, 0),
    val detectionEnd: LocalTime = LocalTime.of(10, 0),
    val isLoading: Boolean = false
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val profileRepository: ProfileRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state.asStateFlow()

    fun nextStep() {
        val current = _state.value
        val next = when (current.step) {
            OnboardingStep.WELCOME -> OnboardingStep.PROFILE_CREATE
            OnboardingStep.PROFILE_CREATE -> OnboardingStep.PERMISSIONS
            OnboardingStep.PERMISSIONS -> OnboardingStep.DETECTION_WINDOW
            OnboardingStep.DETECTION_WINDOW -> OnboardingStep.DONE
            OnboardingStep.DONE -> OnboardingStep.DONE
        }
        _state.value = current.copy(step = next)
    }

    fun prevStep() {
        val current = _state.value
        val prev = when (current.step) {
            OnboardingStep.WELCOME -> OnboardingStep.WELCOME
            OnboardingStep.PROFILE_CREATE -> OnboardingStep.WELCOME
            OnboardingStep.PERMISSIONS -> OnboardingStep.PROFILE_CREATE
            OnboardingStep.DETECTION_WINDOW -> OnboardingStep.PERMISSIONS
            OnboardingStep.DONE -> OnboardingStep.DETECTION_WINDOW
        }
        _state.value = current.copy(step = prev)
    }

    fun setProfileName(name: String) { _state.value = _state.value.copy(profileName = name) }
    fun setProfileEmoji(emoji: String) { _state.value = _state.value.copy(profileEmoji = emoji) }
    fun setDetectionStart(time: LocalTime) { _state.value = _state.value.copy(detectionStart = time) }
    fun setDetectionEnd(time: LocalTime) { _state.value = _state.value.copy(detectionEnd = time) }

    fun completeOnboarding(onComplete: () -> Unit) {
        viewModelScope.launch {
            _state.value = _state.value.copy(isLoading = true)
            val s = _state.value
            val profile = profileRepository.createDefaultProfile(
                name = s.profileName.ifBlank { "Me" },
                emoji = s.profileEmoji
            )
            preferences.setOnboardingComplete(true)
            _state.value = _state.value.copy(isLoading = false)
            onComplete()
        }
    }
}
