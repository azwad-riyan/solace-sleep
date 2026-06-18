package com.solace.sleep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.domain.model.Profile
import java.time.Instant
import java.time.LocalTime

@Entity(tableName = "profiles")
data class ProfileEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "avatar_emoji")
    val avatarEmoji: String,

    @ColumnInfo(name = "sleep_goal_minutes")
    val sleepGoalMinutes: Int,

    @ColumnInfo(name = "detection_window_start_hour")
    val detectionWindowStartHour: Int,

    @ColumnInfo(name = "detection_window_start_minute")
    val detectionWindowStartMinute: Int,

    @ColumnInfo(name = "detection_window_end_hour")
    val detectionWindowEndHour: Int,

    @ColumnInfo(name = "detection_window_end_minute")
    val detectionWindowEndMinute: Int,

    @ColumnInfo(name = "sensitivity")
    val sensitivity: String,

    @ColumnInfo(name = "created_at")
    val createdAt: Long
) {
    fun toDomain() = Profile(
        id = id,
        name = name,
        avatarEmoji = avatarEmoji,
        sleepGoalMinutes = sleepGoalMinutes,
        detectionWindowStart = LocalTime.of(detectionWindowStartHour, detectionWindowStartMinute),
        detectionWindowEnd = LocalTime.of(detectionWindowEndHour, detectionWindowEndMinute),
        sensitivity = DetectionSensitivity.valueOf(sensitivity),
        createdAt = Instant.ofEpochMilli(createdAt)
    )

    companion object {
        fun fromDomain(profile: Profile) = ProfileEntity(
            id = profile.id,
            name = profile.name,
            avatarEmoji = profile.avatarEmoji,
            sleepGoalMinutes = profile.sleepGoalMinutes,
            detectionWindowStartHour = profile.detectionWindowStart.hour,
            detectionWindowStartMinute = profile.detectionWindowStart.minute,
            detectionWindowEndHour = profile.detectionWindowEnd.hour,
            detectionWindowEndMinute = profile.detectionWindowEnd.minute,
            sensitivity = profile.sensitivity.name,
            createdAt = profile.createdAt.toEpochMilli()
        )
    }
}
