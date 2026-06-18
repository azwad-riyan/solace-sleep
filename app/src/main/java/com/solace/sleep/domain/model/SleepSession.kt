package com.solace.sleep.domain.model

import java.time.Instant

data class SleepSession(
    val id: String,
    val profileId: String,
    val sleepOnset: Instant,
    val wakeTime: Instant,
    val durationMinutes: Int,
    val sessionType: SessionType,
    val source: SessionSource,
    val confidenceScore: Int?,
    val correctionPending: Boolean,
    val qualityScore: Int?,
    val interruptions: List<SleepInterruption>,
    val tags: List<String>,
    val notes: String?,
    val createdAt: Instant,
    val lastModifiedAt: Instant
)

enum class SessionType {
    NIGHT_SLEEP,
    NAP
}

enum class SessionSource {
    AUTO_DETECTED,
    MANUAL,
    AUTO_CORRECTED
}

data class SleepInterruption(
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int
)
