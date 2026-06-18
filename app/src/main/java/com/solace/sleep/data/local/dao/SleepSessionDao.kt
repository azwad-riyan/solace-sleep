package com.solace.sleep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solace.sleep.data.local.entity.SleepSessionEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SleepSessionDao {

    @Query("SELECT * FROM sleep_sessions WHERE profile_id = :profileId ORDER BY sleep_onset DESC")
    fun observeByProfile(profileId: String): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE profile_id = :profileId ORDER BY sleep_onset DESC")
    suspend fun getByProfile(profileId: String): List<SleepSessionEntity>

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId
        AND sleep_onset >= :startEpoch
        AND sleep_onset <= :endEpoch
        ORDER BY sleep_onset DESC
    """)
    suspend fun getByProfileAndRange(
        profileId: String,
        startEpoch: Long,
        endEpoch: Long
    ): List<SleepSessionEntity>

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId
        AND sleep_onset >= :startEpoch
        AND sleep_onset <= :endEpoch
        ORDER BY sleep_onset DESC
    """)
    fun observeByProfileAndRange(
        profileId: String,
        startEpoch: Long,
        endEpoch: Long
    ): Flow<List<SleepSessionEntity>>

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    suspend fun getById(id: String): SleepSessionEntity?

    @Query("SELECT * FROM sleep_sessions WHERE id = :id")
    fun observeById(id: String): Flow<SleepSessionEntity?>

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId AND correction_pending = 1
        ORDER BY created_at DESC
        LIMIT 1
    """)
    fun observePendingCorrection(profileId: String): Flow<SleepSessionEntity?>

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId AND correction_pending = 1
        ORDER BY created_at DESC
    """)
    suspend fun getPendingCorrections(profileId: String): List<SleepSessionEntity>

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId
        ORDER BY sleep_onset DESC
        LIMIT 1
    """)
    suspend fun getLatestByProfile(profileId: String): SleepSessionEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(session: SleepSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(sessions: List<SleepSessionEntity>)

    @Update
    suspend fun update(session: SleepSessionEntity)

    @Delete
    suspend fun delete(session: SleepSessionEntity)

    @Query("DELETE FROM sleep_sessions WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("""
        SELECT * FROM sleep_sessions
        WHERE profile_id = :profileId
        AND wake_time IS NULL
        AND sleep_onset < :cutoffEpoch
    """)
    suspend fun getStaleOpenSessions(profileId: String, cutoffEpoch: Long): List<SleepSessionEntity>

    @Query("SELECT COUNT(*) FROM sleep_sessions WHERE profile_id = :profileId")
    suspend fun countByProfile(profileId: String): Int
}
