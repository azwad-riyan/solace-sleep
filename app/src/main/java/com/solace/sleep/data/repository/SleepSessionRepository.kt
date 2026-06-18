package com.solace.sleep.data.repository

import com.solace.sleep.data.local.dao.SleepSessionDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.data.local.entity.SessionTagCrossRef
import com.solace.sleep.data.local.entity.SleepSessionEntity
import com.solace.sleep.domain.model.SleepSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SleepSessionRepository @Inject constructor(
    private val sessionDao: SleepSessionDao,
    private val tagDao: TagDao
) {
    fun observeSessionsByProfile(profileId: String): Flow<List<SleepSession>> =
        sessionDao.observeByProfile(profileId).map { entities ->
            entities.map { entity ->
                val tags = tagDao.getTagLabelsForSession(entity.id)
                entity.toDomain(tags)
            }
        }

    suspend fun getSessionsByProfile(profileId: String): List<SleepSession> {
        return sessionDao.getByProfile(profileId).map { entity ->
            val tags = tagDao.getTagLabelsForSession(entity.id)
            entity.toDomain(tags)
        }
    }

    suspend fun getSessionsByProfileAndRange(
        profileId: String,
        start: Instant,
        end: Instant
    ): List<SleepSession> {
        return sessionDao.getByProfileAndRange(profileId, start.toEpochMilli(), end.toEpochMilli())
            .map { entity ->
                val tags = tagDao.getTagLabelsForSession(entity.id)
                entity.toDomain(tags)
            }
    }

    fun observeSessionsByProfileAndRange(
        profileId: String,
        start: Instant,
        end: Instant
    ): Flow<List<SleepSession>> =
        sessionDao.observeByProfileAndRange(profileId, start.toEpochMilli(), end.toEpochMilli())
            .map { entities ->
                entities.map { entity ->
                    val tags = tagDao.getTagLabelsForSession(entity.id)
                    entity.toDomain(tags)
                }
            }

    suspend fun getSessionById(id: String): SleepSession? {
        val entity = sessionDao.getById(id) ?: return null
        val tags = tagDao.getTagLabelsForSession(id)
        return entity.toDomain(tags)
    }

    fun observeSessionById(id: String): Flow<SleepSession?> =
        sessionDao.observeById(id).map { entity ->
            entity?.let {
                val tags = tagDao.getTagLabelsForSession(it.id)
                it.toDomain(tags)
            }
        }

    fun observePendingCorrection(profileId: String): Flow<SleepSession?> =
        sessionDao.observePendingCorrection(profileId).map { entity ->
            entity?.let {
                val tags = tagDao.getTagLabelsForSession(it.id)
                it.toDomain(tags)
            }
        }

    suspend fun saveSession(session: SleepSession) {
        val entity = SleepSessionEntity.fromDomain(session)
        sessionDao.insert(entity)
        // Update tags
        tagDao.deleteSessionTags(session.id)
        val tagEntities = tagDao.getByProfile(session.profileId)
        val crossRefs = session.tags.mapNotNull { tagLabel ->
            tagEntities.find { it.label == tagLabel }?.let { tag ->
                SessionTagCrossRef(sessionId = session.id, tagId = tag.id)
            }
        }
        if (crossRefs.isNotEmpty()) {
            tagDao.insertCrossRefs(crossRefs)
        }
    }

    suspend fun updateSession(session: SleepSession) {
        val entity = SleepSessionEntity.fromDomain(session)
        sessionDao.update(entity)
        tagDao.deleteSessionTags(session.id)
        val tagEntities = tagDao.getByProfile(session.profileId)
        val crossRefs = session.tags.mapNotNull { tagLabel ->
            tagEntities.find { it.label == tagLabel }?.let { tag ->
                SessionTagCrossRef(sessionId = session.id, tagId = tag.id)
            }
        }
        if (crossRefs.isNotEmpty()) {
            tagDao.insertCrossRefs(crossRefs)
        }
    }

    suspend fun deleteSession(sessionId: String) {
        sessionDao.deleteById(sessionId)
    }

    suspend fun getLatestSession(profileId: String): SleepSession? {
        val entity = sessionDao.getLatestByProfile(profileId) ?: return null
        val tags = tagDao.getTagLabelsForSession(entity.id)
        return entity.toDomain(tags)
    }

    suspend fun getStaleOpenSessions(profileId: String, cutoffHoursAgo: Long): List<SleepSession> {
        val cutoff = Instant.now().minusSeconds(cutoffHoursAgo * 3600)
        return sessionDao.getStaleOpenSessions(profileId, cutoff.toEpochMilli()).map { entity ->
            val tags = tagDao.getTagLabelsForSession(entity.id)
            entity.toDomain(tags)
        }
    }

    suspend fun getSessionsForDateRange(
        profileId: String,
        startDate: LocalDate,
        endDate: LocalDate,
        zone: ZoneId = ZoneId.systemDefault()
    ): List<SleepSession> {
        val start = startDate.atStartOfDay(zone).toInstant()
        val end = endDate.plusDays(1).atStartOfDay(zone).toInstant()
        return getSessionsByProfileAndRange(profileId, start, end)
    }
}
