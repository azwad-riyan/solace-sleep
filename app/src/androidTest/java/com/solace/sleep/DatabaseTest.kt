package com.solace.sleep

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.solace.sleep.data.local.SolaceDatabase
import com.solace.sleep.data.local.dao.ProfileDao
import com.solace.sleep.data.local.entity.ProfileEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class DatabaseTest {

    private lateinit var profileDao: ProfileDao
    private lateinit var db: SolaceDatabase

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, SolaceDatabase::class.java).build()
        profileDao = db.profileDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun insertAndReadProfile() = runTest {
        val profile = ProfileEntity(
            id = "test-id",
            name = "Test User",
            avatarEmoji = "😴",
            sleepGoalMinutes = 480,
            detectionWindowStartHour = 21,
            detectionWindowStartMinute = 0,
            detectionWindowEndHour = 10,
            detectionWindowEndMinute = 0,
            sensitivity = "MEDIUM",
            createdAt = System.currentTimeMillis()
        )
        profileDao.insert(profile)
        val profiles = profileDao.observeAll().first()
        assert(profiles.isNotEmpty())
        assert(profiles.first().name == "Test User")
    }

    @Test
    fun deleteProfile() = runTest {
        val profile = ProfileEntity(
            id = "del-id",
            name = "Delete Me",
            avatarEmoji = "🗑️",
            sleepGoalMinutes = 480,
            detectionWindowStartHour = 21,
            detectionWindowStartMinute = 0,
            detectionWindowEndHour = 10,
            detectionWindowEndMinute = 0,
            sensitivity = "MEDIUM",
            createdAt = System.currentTimeMillis()
        )
        profileDao.insert(profile)
        profileDao.deleteById("del-id")
        val profiles = profileDao.observeAll().first()
        assert(profiles.none { it.id == "del-id" })
    }
}
