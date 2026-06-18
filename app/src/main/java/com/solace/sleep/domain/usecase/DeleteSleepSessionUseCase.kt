package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.SleepSessionRepository
import javax.inject.Inject

class DeleteSleepSessionUseCase @Inject constructor(
    private val repository: SleepSessionRepository
) {
    suspend operator fun invoke(sessionId: String) {
        repository.deleteSession(sessionId)
    }
}
