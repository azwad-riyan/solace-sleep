package com.solace.sleep.ui.daydetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.domain.usecase.DeleteSleepSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DayDetailUiState(
    val sessions: List<SleepSession> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class DayDetailViewModel @Inject constructor(
    private val sessionRepository: SleepSessionRepository,
    private val profileRepository: ProfileRepository,
    private val deleteSessionUseCase: DeleteSleepSessionUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(DayDetailUiState())
    val uiState: StateFlow<DayDetailUiState> = _uiState.asStateFlow()

    fun loadDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = DayDetailUiState(isLoading = true)
            val profile = profileRepository.observeActiveProfile().first()
            if (profile != null) {
                val sessions = sessionRepository.getSessionsForDateRange(profile.id, date, date)
                _uiState.value = DayDetailUiState(sessions = sessions, isLoading = false)
            } else {
                _uiState.value = DayDetailUiState(isLoading = false)
            }
        }
    }

    fun deleteSession(sessionId: String) {
        viewModelScope.launch {
            deleteSessionUseCase(sessionId)
            val current = _uiState.value
            _uiState.value = current.copy(sessions = current.sessions.filter { it.id != sessionId })
        }
    }
}
