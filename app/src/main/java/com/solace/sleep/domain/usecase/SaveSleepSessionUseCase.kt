package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SleepSession
import javax.inject.Inject

class SaveSleepSessionUseCase @Inject constructor(
    private val repository: SleepSessionRepository
) {
    suspend operator fun invoke(session: SleepSession) {
        repository.saveSession(session)
    }
}
