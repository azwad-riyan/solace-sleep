package com.solace.sleep

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

data class MainUiState(
    val isLoading: Boolean = true,
    val onboardingComplete: Boolean = false
)

@HiltViewModel
class MainViewModel @Inject constructor(
    preferences: AppPreferences
) : ViewModel() {

    val uiState: StateFlow<MainUiState> = preferences.onboardingComplete
        .map { complete -> MainUiState(isLoading = false, onboardingComplete = complete) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = MainUiState()
        )
}
