package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow
import java.time.Instant
import javax.inject.Inject

class GetSleepSessionsUseCase @Inject constructor(
    private val repository: SleepSessionRepository
) {
    operator fun invoke(profileId: String): Flow<List<SleepSession>> =
        repository.observeSessionsByProfile(profileId)

    fun forRange(profileId: String, start: Instant, end: Instant): Flow<List<SleepSession>> =
        repository.observeSessionsByProfileAndRange(profileId, start, end)
}
