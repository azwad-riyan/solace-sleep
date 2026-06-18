package com.solace.sleep.data.repository

import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.dao.SleepSessionDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.data.local.entity.ProfileEntity
import com.solace.sleep.data.local.entity.SleepSessionEntity
import com.solace.sleep.data.local.entity.SleepTagEntity
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.remote.DriveApiClient
import com.solace.sleep.data.remote.ProfileDto
import com.solace.sleep.data.remote.SessionDto
import com.solace.sleep.data.remote.SyncDto
import com.solace.sleep.data.remote.TagDto
import timber.log.Timber
import java.time.Instant
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncRepository @Inject constructor(
    private val driveApiClient: DriveApiClient,
    private val profileDao: ProfileDao,
    private val sessionDao: SleepSessionDao,
    private val tagDao: TagDao,
    private val preferences: AppPreferences
) {
    suspend fun syncToCloud(authToken: String): Result<Unit> {
        return try {
            val profiles = profileDao.getAll()
            val sessions = profiles.flatMap { sessionDao.getByProfile(it.id) }
            val tags = profiles.flatMap { tagDao.getByProfile(it.id) }

            val syncDto = buildSyncDto(profiles, sessions, tags)
            val fileName = "solace_backup_${System.currentTimeMillis()}.json"

            driveApiClient.uploadBackup(authToken, syncDto, fileName)
                .map { _ ->
                    preferences.setLastSyncTimestamp(System.currentTimeMillis())
                }
        } catch (e: Exception) {
            Timber.e(e, "Sync to cloud failed")
            Result.failure(e)
        }
    }

    suspend fun restoreFromCloud(authToken: String): Result<Unit> {
        return try {
            val syncDtoResult = driveApiClient.downloadLatestBackup(authToken)
            syncDtoResult.map { syncDto ->
                if (syncDto != null) {
                    restoreData(syncDto)
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Restore from cloud failed")
            Result.failure(e)
        }
    }

    private fun buildSyncDto(
        profiles: List<ProfileEntity>,
        sessions: List<SleepSessionEntity>,
        tags: List<SleepTagEntity>
    ) = SyncDto(
        exportTimestamp = System.currentTimeMillis(),
        deviceId = UUID.randomUUID().toString(),
        profiles = profiles.map { p ->
            ProfileDto(
                id = p.id,
                name = p.name,
                avatarEmoji = p.avatarEmoji,
                sleepGoalMinutes = p.sleepGoalMinutes,
                detectionWindowStartHour = p.detectionWindowStartHour,
                detectionWindowStartMinute = p.detectionWindowStartMinute,
                detectionWindowEndHour = p.detectionWindowEndHour,
                detectionWindowEndMinute = p.detectionWindowEndMinute,
                sensitivity = p.sensitivity,
                createdAt = p.createdAt
            )
        },
        sessions = sessions.map { s ->
            SessionDto(
                id = s.id,
                profileId = s.profileId,
                sleepOnset = s.sleepOnset,
                wakeTime = s.wakeTime,
                durationMinutes = s.durationMinutes,
                sessionType = s.sessionType,
                source = s.source,
                confidenceScore = s.confidenceScore,
                correctionPending = s.correctionPending,
                qualityScore = s.qualityScore,
                interruptionsJson = s.interruptionsJson,
                tags = emptyList(),
                notes = s.notes,
                createdAt = s.createdAt,
                lastModifiedAt = s.lastModifiedAt
            )
        },
        tags = tags.map { t ->
            TagDto(
                id = t.id,
                profileId = t.profileId,
                label = t.label,
                emoji = t.emoji,
                isDefault = t.isDefault
            )
        }
    )

    private suspend fun restoreData(syncDto: SyncDto) {
        val profileEntities = syncDto.profiles.map { p ->
            ProfileEntity(
                id = p.id,
                name = p.name,
                avatarEmoji = p.avatarEmoji,
                sleepGoalMinutes = p.sleepGoalMinutes,
                detectionWindowStartHour = p.detectionWindowStartHour,
                detectionWindowStartMinute = p.detectionWindowStartMinute,
                detectionWindowEndHour = p.detectionWindowEndHour,
                detectionWindowEndMinute = p.detectionWindowEndMinute,
                sensitivity = p.sensitivity,
                createdAt = p.createdAt
            )
        }
        profileDao.insertAll(profileEntities)

        val tagEntities = syncDto.tags.map { t ->
            SleepTagEntity(
                id = t.id,
                profileId = t.profileId,
                label = t.label,
                emoji = t.emoji,
                isDefault = t.isDefault
            )
        }
        tagDao.insertAll(tagEntities)

        val sessionEntities = syncDto.sessions.map { s ->
            SleepSessionEntity(
                id = s.id,
                profileId = s.profileId,
                sleepOnset = s.sleepOnset,
                wakeTime = s.wakeTime,
                durationMinutes = s.durationMinutes,
                sessionType = s.sessionType,
                source = s.source,
                confidenceScore = s.confidenceScore,
                correctionPending = s.correctionPending,
                qualityScore = s.qualityScore,
                interruptionsJson = s.interruptionsJson,
                notes = s.notes,
                createdAt = s.createdAt,
                lastModifiedAt = s.lastModifiedAt
            )
        }
        sessionDao.insertAll(sessionEntities)
    }
}
