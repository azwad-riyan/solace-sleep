package com.solace.sleep.ui.correction

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.domain.usecase.CorrectSleepSessionUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.Instant
import javax.inject.Inject

@HiltViewModel
class CorrectionViewModel @Inject constructor(
    private val correctSleepSessionUseCase: CorrectSleepSessionUseCase
) : ViewModel() {

    fun correctSession(
        session: SleepSession,
        newSleepOnset: Instant,
        newWakeTime: Instant,
        qualityScore: Int?,
        tags: List<String>,
        notes: String?
    ) {
        viewModelScope.launch {
            correctSleepSessionUseCase(
                session = session,
                newSleepOnset = newSleepOnset,
                newWakeTime = newWakeTime,
                qualityScore = qualityScore,
                tags = tags,
                notes = notes
            )
        }
    }

    fun confirmSession(
        session: SleepSession,
        qualityScore: Int?,
        tags: List<String>,
        notes: String?
    ) {
        viewModelScope.launch {
            correctSleepSessionUseCase.confirm(
                session = session,
                qualityScore = qualityScore,
                tags = tags,
                notes = notes
            )
        }
    }
}
