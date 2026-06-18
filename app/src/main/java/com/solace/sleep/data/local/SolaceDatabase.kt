package com.solace.sleep.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.dao.SleepSessionDao
import com.solace.sleep.data.local.dao.TagDao
import com.solace.sleep.data.local.entity.ProfileEntity
import com.solace.sleep.data.local.entity.SessionTagCrossRef
import com.solace.sleep.data.local.entity.SleepSessionEntity
import com.solace.sleep.data.local.entity.SleepTagEntity

@Database(
    entities = [
        ProfileEntity::class,
        SleepSessionEntity::class,
        SleepTagEntity::class,
        SessionTagCrossRef::class
    ],
    version = 1,
    exportSchema = true
)
abstract class SolaceDatabase : RoomDatabase() {
    abstract fun profileDao(): ProfileDao
    abstract fun sleepSessionDao(): SleepSessionDao
    abstract fun tagDao(): TagDao
}
