package com.solace.sleep.domain.model

import java.time.Instant
import java.time.LocalTime

data class Profile(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val sleepGoalMinutes: Int,
    val detectionWindowStart: LocalTime,
    val detectionWindowEnd: LocalTime,
    val sensitivity: DetectionSensitivity,
    val createdAt: Instant
)

enum class DetectionSensitivity {
    LOW,
    MEDIUM,
    HIGH
}
