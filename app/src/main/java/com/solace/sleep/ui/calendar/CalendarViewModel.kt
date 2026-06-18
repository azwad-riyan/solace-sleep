package com.solace.sleep.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.Profile
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.util.TimeExtensions.toLocalDate
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import javax.inject.Inject

data class CalendarUiState(
    val currentMonth: YearMonth = YearMonth.now(),
    val sessions: List<SleepSession> = emptyList(),
    val activeProfile: Profile? = null,
    val pendingCorrectionSession: SleepSession? = null,
    val isLoading: Boolean = true
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val sessionRepository: SleepSessionRepository,
    private val profileRepository: ProfileRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val _currentMonth = MutableStateFlow(YearMonth.now())

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<CalendarUiState> = combine(
        profileRepository.observeActiveProfile(),
        _currentMonth
    ) { profile, month -> Pair(profile, month) }
        .flatMapLatest { (profile, month) ->
            if (profile == null) return@flatMapLatest flowOf(CalendarUiState(isLoading = false))
            val zone = ZoneId.systemDefault()
            val start = month.atDay(1).atStartOfDay(zone).toInstant()
            val end = month.atEndOfMonth().plusDays(1).atStartOfDay(zone).toInstant()
            combine(
                sessionRepository.observeSessionsByProfileAndRange(profile.id, start, end),
                sessionRepository.observePendingCorrection(profile.id)
            ) { sessions, pending ->
                CalendarUiState(
                    currentMonth = month,
                    sessions = sessions,
                    activeProfile = profile,
                    pendingCorrectionSession = pending,
                    isLoading = false
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), CalendarUiState())

    fun goToPreviousMonth() {
        _currentMonth.update { it.minusMonths(1) }
    }

    fun goToNextMonth() {
        _currentMonth.update { it.plusMonths(1) }
    }

    fun goToToday() {
        _currentMonth.value = YearMonth.now()
    }

    fun getSessionsForDate(date: LocalDate): List<SleepSession> {
        val zone = ZoneId.systemDefault()
        return uiState.value.sessions.filter { session ->
            session.sleepOnset.toLocalDate(zone) == date
        }
    }
}
