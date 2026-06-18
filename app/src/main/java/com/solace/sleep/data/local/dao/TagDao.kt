package com.solace.sleep.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.solace.sleep.data.local.entity.SessionTagCrossRef
import com.solace.sleep.data.local.entity.SleepTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Query("SELECT * FROM sleep_tags WHERE profile_id = :profileId ORDER BY is_default DESC, label ASC")
    fun observeByProfile(profileId: String): Flow<List<SleepTagEntity>>

    @Query("SELECT * FROM sleep_tags WHERE profile_id = :profileId ORDER BY is_default DESC, label ASC")
    suspend fun getByProfile(profileId: String): List<SleepTagEntity>

    @Query("""
        SELECT st.label FROM sleep_tags st
        INNER JOIN session_tag_cross_ref ref ON st.id = ref.tag_id
        WHERE ref.session_id = :sessionId
    """)
    suspend fun getTagLabelsForSession(sessionId: String): List<String>

    @Query("""
        SELECT st.* FROM sleep_tags st
        INNER JOIN session_tag_cross_ref ref ON st.id = ref.tag_id
        WHERE ref.session_id = :sessionId
    """)
    suspend fun getTagsForSession(sessionId: String): List<SleepTagEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: SleepTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<SleepTagEntity>)

    @Update
    suspend fun update(tag: SleepTagEntity)

    @Delete
    suspend fun delete(tag: SleepTagEntity)

    @Query("DELETE FROM sleep_tags WHERE id = :id")
    suspend fun deleteById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRef(crossRef: SessionTagCrossRef)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCrossRefs(crossRefs: List<SessionTagCrossRef>)

    @Query("DELETE FROM session_tag_cross_ref WHERE session_id = :sessionId")
    suspend fun deleteSessionTags(sessionId: String)

    @Query("DELETE FROM session_tag_cross_ref WHERE session_id = :sessionId AND tag_id = :tagId")
    suspend fun deleteSessionTag(sessionId: String, tagId: String)
}
