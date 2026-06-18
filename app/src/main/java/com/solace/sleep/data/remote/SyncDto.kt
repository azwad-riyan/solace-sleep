package com.solace.sleep.data.remote

import kotlinx.serialization.Serializable

@Serializable
data class SyncDto(
    val exportVersion: Int = 1,
    val exportTimestamp: Long,
    val deviceId: String,
    val profiles: List<ProfileDto>,
    val sessions: List<SessionDto>,
    val tags: List<TagDto>
)

@Serializable
data class ProfileDto(
    val id: String,
    val name: String,
    val avatarEmoji: String,
    val sleepGoalMinutes: Int,
    val detectionWindowStartHour: Int,
    val detectionWindowStartMinute: Int,
    val detectionWindowEndHour: Int,
    val detectionWindowEndMinute: Int,
    val sensitivity: String,
    val createdAt: Long
)

@Serializable
data class SessionDto(
    val id: String,
    val profileId: String,
    val sleepOnset: Long,
    val wakeTime: Long,
    val durationMinutes: Int,
    val sessionType: String,
    val source: String,
    val confidenceScore: Int?,
    val correctionPending: Boolean,
    val qualityScore: Int?,
    val interruptionsJson: String,
    val tags: List<String>,
    val notes: String?,
    val createdAt: Long,
    val lastModifiedAt: Long
)

@Serializable
data class TagDto(
    val id: String,
    val profileId: String,
    val label: String,
    val emoji: String,
    val isDefault: Boolean
)
