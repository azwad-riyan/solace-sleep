package com.solace.sleep

import app.cash.turbine.test
import com.solace.sleep.data.repository.InsightsRepository
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SessionSource
import com.solace.sleep.domain.model.SessionType
import com.solace.sleep.domain.model.SleepSession
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class InsightsRepositoryTest {

    private lateinit var sessionRepository: SleepSessionRepository
    private lateinit var profileRepository: ProfileRepository
    private lateinit var insightsRepository: InsightsRepository

    @BeforeEach
    fun setUp() {
        sessionRepository = mockk()
        profileRepository = mockk()
        insightsRepository = InsightsRepository(sessionRepository, profileRepository)
    }

    private fun makeSleepSession(
        date: LocalDate,
        durationMinutes: Int,
        qualityScore: Int? = null
    ): SleepSession {
        val zone = ZoneId.systemDefault()
        val onset = date.atTime(22, 0).atZone(zone).toInstant()
        val wake = onset.plusSeconds(durationMinutes * 60L)
        val now = Instant.now()
        return SleepSession(
            id = java.util.UUID.randomUUID().toString(),
            profileId = "test-profile",
            sleepOnset = onset,
            wakeTime = wake,
            durationMinutes = durationMinutes,
            sessionType = SessionType.NIGHT_SLEEP,
            source = SessionSource.AUTO_DETECTED,
            confidenceScore = 80,
            correctionPending = false,
            qualityScore = qualityScore,
            interruptions = emptyList(),
            tags = emptyList(),
            notes = null,
            createdAt = now,
            lastModifiedAt = now
        )
    }

    @Test
    fun `weekly insights returns correct average`() = runTest {
        val today = LocalDate.now()
        val sessions = (0..6).map { offset ->
            makeSleepSession(today.minusDays(offset.toLong()), 450) // 7.5 hrs
        }
        coEvery {
            sessionRepository.getSessionsForDateRange(any(), any(), any(), any())
        } returns sessions

        val insights = insightsRepository.getWeeklyInsights("test-profile", 480)
        assertEquals(450, insights.averageDurationMinutes)
    }

    @Test
    fun `sleep debt is calculated correctly`() = runTest {
        val today = LocalDate.now()
        // Only 5 of 7 days have sleep, at 400 min each (80 min deficit per day)
        val sessions = (0..4).map { offset ->
            makeSleepSession(today.minusDays(offset.toLong()), 400)
        }
        coEvery {
            sessionRepository.getSessionsForDateRange(any(), any(), any(), any())
        } returns sessions

        val insights = insightsRepository.getWeeklyInsights("test-profile", 480)
        // 5 days × 80 deficit + 2 full days × 480 deficit = 400 + 960 = 1360
        // Actually: 5 days have 400 (80 deficit each = 400 total) + 2 days have 0 (480 deficit each = 960)
        assertEquals(400 + 960, insights.sleepDebtMinutes)
    }
}
