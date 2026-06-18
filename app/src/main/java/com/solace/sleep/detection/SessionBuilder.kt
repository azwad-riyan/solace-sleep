package com.solace.sleep.detection

import com.solace.sleep.domain.model.SessionSource
import com.solace.sleep.domain.model.SessionType
import com.solace.sleep.domain.model.SleepInterruption
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.util.Constants
import java.time.Duration
import java.time.Instant
import java.util.UUID

object SessionBuilder {

    fun build(
        profileId: String,
        sleepOnset: Instant,
        wakeTime: Instant,
        confidenceScore: Int,
        interruptions: List<SleepInterruption> = emptyList()
    ): SleepSession? {
        val totalSeconds = Duration.between(sleepOnset, wakeTime).seconds
        val interruptionSeconds = interruptions.sumOf {
            Duration.between(it.startTime, it.endTime).seconds
        }
        val netSleepSeconds = totalSeconds - interruptionSeconds
        val durationMinutes = (netSleepSeconds / 60).toInt()

        if (durationMinutes < Constants.MIN_NAP_MINUTES) return null

        val sessionType = if (durationMinutes >= Constants.MIN_NIGHT_SLEEP_MINUTES) {
            SessionType.NIGHT_SLEEP
        } else {
            SessionType.NAP
        }

        val now = Instant.now()
        return SleepSession(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            sleepOnset = sleepOnset,
            wakeTime = wakeTime,
            durationMinutes = durationMinutes,
            sessionType = sessionType,
            source = SessionSource.AUTO_DETECTED,
            confidenceScore = confidenceScore,
            correctionPending = confidenceScore < 85,
            qualityScore = null,
            interruptions = interruptions,
            tags = emptyList(),
            notes = null,
            createdAt = now,
            lastModifiedAt = now
        )
    }

    fun buildManual(
        profileId: String,
        sleepOnset: Instant,
        wakeTime: Instant,
        qualityScore: Int?,
        tags: List<String>,
        notes: String?
    ): SleepSession? {
        val durationMinutes = Duration.between(sleepOnset, wakeTime).toMinutes().toInt()
        if (durationMinutes < Constants.MIN_NAP_MINUTES) return null

        val sessionType = if (durationMinutes >= Constants.MIN_NIGHT_SLEEP_MINUTES) {
            SessionType.NIGHT_SLEEP
        } else {
            SessionType.NAP
        }

        val now = Instant.now()
        return SleepSession(
            id = UUID.randomUUID().toString(),
            profileId = profileId,
            sleepOnset = sleepOnset,
            wakeTime = wakeTime,
            durationMinutes = durationMinutes,
            sessionType = sessionType,
            source = SessionSource.MANUAL,
            confidenceScore = 100,
            correctionPending = false,
            qualityScore = qualityScore,
            interruptions = emptyList(),
            tags = tags,
            notes = notes,
            createdAt = now,
            lastModifiedAt = now
        )
    }
}
