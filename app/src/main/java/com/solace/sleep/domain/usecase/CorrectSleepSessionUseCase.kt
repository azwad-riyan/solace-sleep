package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SessionSource
import com.solace.sleep.domain.model.SleepSession
import java.time.Duration
import java.time.Instant
import javax.inject.Inject

class CorrectSleepSessionUseCase @Inject constructor(
    private val repository: SleepSessionRepository
) {
    suspend operator fun invoke(
        session: SleepSession,
        newSleepOnset: Instant,
        newWakeTime: Instant,
        qualityScore: Int?,
        tags: List<String>,
        notes: String?
    ): SleepSession {
        val durationMinutes = Duration.between(newSleepOnset, newWakeTime).toMinutes().toInt()
        val corrected = session.copy(
            sleepOnset = newSleepOnset,
            wakeTime = newWakeTime,
            durationMinutes = durationMinutes,
            source = SessionSource.AUTO_CORRECTED,
            correctionPending = false,
            qualityScore = qualityScore,
            tags = tags,
            notes = notes,
            lastModifiedAt = Instant.now()
        )
        repository.updateSession(corrected)
        return corrected
    }

    suspend fun confirm(session: SleepSession, qualityScore: Int?, tags: List<String>, notes: String?): SleepSession {
        val confirmed = session.copy(
            correctionPending = false,
            qualityScore = qualityScore,
            tags = tags,
            notes = notes,
            lastModifiedAt = Instant.now()
        )
        repository.updateSession(confirmed)
        return confirmed
    }
}
