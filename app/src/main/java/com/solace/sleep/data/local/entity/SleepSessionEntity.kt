package com.solace.sleep.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.solace.sleep.domain.model.SessionSource
import com.solace.sleep.domain.model.SessionType
import com.solace.sleep.domain.model.SleepInterruption
import com.solace.sleep.domain.model.SleepSession
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant

@Entity(
    tableName = "sleep_sessions",
    foreignKeys = [
        ForeignKey(
            entity = ProfileEntity::class,
            parentColumns = ["id"],
            childColumns = ["profile_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["profile_id"]),
        Index(value = ["sleep_onset"]),
        Index(value = ["correction_pending"])
    ]
)
data class SleepSessionEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "profile_id")
    val profileId: String,

    @ColumnInfo(name = "sleep_onset")
    val sleepOnset: Long,

    @ColumnInfo(name = "wake_time")
    val wakeTime: Long,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "session_type")
    val sessionType: String,

    @ColumnInfo(name = "source")
    val source: String,

    @ColumnInfo(name = "confidence_score")
    val confidenceScore: Int?,

    @ColumnInfo(name = "correction_pending")
    val correctionPending: Boolean,

    @ColumnInfo(name = "quality_score")
    val qualityScore: Int?,

    @ColumnInfo(name = "interruptions_json")
    val interruptionsJson: String,

    @ColumnInfo(name = "notes")
    val notes: String?,

    @ColumnInfo(name = "created_at")
    val createdAt: Long,

    @ColumnInfo(name = "last_modified_at")
    val lastModifiedAt: Long
) {
    fun toDomain(tags: List<String> = emptyList()): SleepSession {
        val interruptions = try {
            Json.decodeFromString<List<InterruptionDto>>(interruptionsJson).map { it.toDomain() }
        } catch (e: Exception) {
            emptyList()
        }
        return SleepSession(
            id = id,
            profileId = profileId,
            sleepOnset = Instant.ofEpochMilli(sleepOnset),
            wakeTime = Instant.ofEpochMilli(wakeTime),
            durationMinutes = durationMinutes,
            sessionType = SessionType.valueOf(sessionType),
            source = SessionSource.valueOf(source),
            confidenceScore = confidenceScore,
            correctionPending = correctionPending,
            qualityScore = qualityScore,
            interruptions = interruptions,
            tags = tags,
            notes = notes,
            createdAt = Instant.ofEpochMilli(createdAt),
            lastModifiedAt = Instant.ofEpochMilli(lastModifiedAt)
        )
    }

    companion object {
        fun fromDomain(session: SleepSession): SleepSessionEntity {
            val interruptionsJson = Json.encodeToString(
                session.interruptions.map { InterruptionDto.fromDomain(it) }
            )
            return SleepSessionEntity(
                id = session.id,
                profileId = session.profileId,
                sleepOnset = session.sleepOnset.toEpochMilli(),
                wakeTime = session.wakeTime.toEpochMilli(),
                durationMinutes = session.durationMinutes,
                sessionType = session.sessionType.name,
                source = session.source.name,
                confidenceScore = session.confidenceScore,
                correctionPending = session.correctionPending,
                qualityScore = session.qualityScore,
                interruptionsJson = interruptionsJson,
                notes = session.notes,
                createdAt = session.createdAt.toEpochMilli(),
                lastModifiedAt = session.lastModifiedAt.toEpochMilli()
            )
        }
    }
}

@kotlinx.serialization.Serializable
data class InterruptionDto(
    val startTime: Long,
    val endTime: Long,
    val durationMinutes: Int
) {
    fun toDomain() = SleepInterruption(
        startTime = Instant.ofEpochMilli(startTime),
        endTime = Instant.ofEpochMilli(endTime),
        durationMinutes = durationMinutes
    )

    companion object {
        fun fromDomain(interruption: SleepInterruption) = InterruptionDto(
            startTime = interruption.startTime.toEpochMilli(),
            endTime = interruption.endTime.toEpochMilli(),
            durationMinutes = interruption.durationMinutes
        )
    }
}
