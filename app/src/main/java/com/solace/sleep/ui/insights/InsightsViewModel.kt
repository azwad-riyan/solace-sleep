package com.solace.sleep.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.domain.model.InsightsData
import com.solace.sleep.domain.usecase.GetInsightsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class InsightsPeriod { WEEKLY, MONTHLY, ALL_TIME }

data class InsightsUiState(
    val period: InsightsPeriod = InsightsPeriod.WEEKLY,
    val data: InsightsData? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class InsightsViewModel @Inject constructor(
    private val getInsightsUseCase: GetInsightsUseCase,
    private val profileRepository: ProfileRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            profileRepository.observeActiveProfile().collectLatest { profile ->
                if (profile != null) {
                    loadInsights(profile.id, profile.sleepGoalMinutes, _uiState.value.period)
                }
            }
        }
    }

    fun selectPeriod(period: InsightsPeriod) {
        _uiState.value = _uiState.value.copy(period = period)
        viewModelScope.launch {
            val profile = profileRepository.observeActiveProfile().first() ?: return@launch
            loadInsights(profile.id, profile.sleepGoalMinutes, period)
        }
    }

    private suspend fun loadInsights(profileId: String, goalMinutes: Int, period: InsightsPeriod) {
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        try {
            val data = when (period) {
                InsightsPeriod.WEEKLY -> getInsightsUseCase.weekly(profileId, goalMinutes)
                InsightsPeriod.MONTHLY -> getInsightsUseCase.monthly(profileId, goalMinutes)
                InsightsPeriod.ALL_TIME -> getInsightsUseCase.allTime(profileId, goalMinutes)
            }
            _uiState.value = _uiState.value.copy(data = data, isLoading = false)
        } catch (e: Exception) {
            _uiState.value = _uiState.value.copy(isLoading = false, error = e.message)
        }
    }
}
