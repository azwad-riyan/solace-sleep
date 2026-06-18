package com.solace.sleep.data.repository

import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.data.local.entity.ProfileEntity
import com.solace.sleep.data.local.entity.SleepTagEntity
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.domain.model.DetectionSensitivity
import com.solace.sleep.domain.model.Profile
import com.solace.sleep.domain.model.SleepTag
import com.solace.sleep.util.Constants
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalTime
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val profileDao: ProfileDao,
    private val tagDao: TagDao,
    private val preferences: AppPreferences
) {
    fun observeAllProfiles(): Flow<List<Profile>> =
        profileDao.observeAll().map { entities -> entities.map { it.toDomain() } }

    suspend fun getAllProfiles(): List<Profile> =
        profileDao.getAll().map { it.toDomain() }

    suspend fun getProfileById(id: String): Profile? =
        profileDao.getById(id)?.toDomain()

    fun observeProfileById(id: String): Flow<Profile?> =
        profileDao.observeById(id).map { it?.toDomain() }

    fun observeActiveProfile(): Flow<Profile?> =
        preferences.activeProfileId.map { profileId ->
            profileId?.let { profileDao.getById(it)?.toDomain() }
        }

    suspend fun saveProfile(profile: Profile) {
        profileDao.insert(ProfileEntity.fromDomain(profile))
    }

    suspend fun updateProfile(profile: Profile) {
        profileDao.update(ProfileEntity.fromDomain(profile))
    }

    suspend fun deleteProfile(profileId: String) {
        profileDao.deleteById(profileId)
        // If deleted profile was active, switch to another
        val activeId = preferences.activeProfileId.first()
        if (activeId == profileId) {
            val remaining = profileDao.getAll()
            if (remaining.isNotEmpty()) {
                preferences.setActiveProfileId(remaining.first().id)
            }
        }
    }

    suspend fun createDefaultProfile(name: String, emoji: String = "😴"): Profile {
        val profile = Profile(
            id = UUID.randomUUID().toString(),
            name = name,
            avatarEmoji = emoji,
            sleepGoalMinutes = Constants.DEFAULT_SLEEP_GOAL_MINUTES,
            detectionWindowStart = LocalTime.of(
                Constants.DEFAULT_DETECTION_START_HOUR,
                Constants.DEFAULT_DETECTION_START_MINUTE
            ),
            detectionWindowEnd = LocalTime.of(
                Constants.DEFAULT_DETECTION_END_HOUR,
                Constants.DEFAULT_DETECTION_END_MINUTE
            ),
            sensitivity = DetectionSensitivity.MEDIUM,
            createdAt = Instant.now()
        )
        profileDao.insert(ProfileEntity.fromDomain(profile))
        preferences.setActiveProfileId(profile.id)
        insertDefaultTags(profile.id)
        return profile
    }

    suspend fun setActiveProfile(profileId: String) {
        preferences.setActiveProfileId(profileId)
    }

    private suspend fun insertDefaultTags(profileId: String) {
        val defaultTags = listOf(
            SleepTag(UUID.randomUUID().toString(), profileId, "Stressed", "😰", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Caffeine", "☕", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Exercise", "🏃", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Alcohol", "🍷", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Late Meal", "🍕", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Screen Time", "📱", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Good Routine", "✨", true),
            SleepTag(UUID.randomUUID().toString(), profileId, "Travel", "✈️", true)
        )
        tagDao.insertAll(defaultTags.map { SleepTagEntity.fromDomain(it) })
    }

    suspend fun isFirstLaunch(): Boolean = profileDao.count() == 0
}
