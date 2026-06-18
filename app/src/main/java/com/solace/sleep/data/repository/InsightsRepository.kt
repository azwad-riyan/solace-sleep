package com.solace.sleep.data.repository

import com.solace.sleep.domain.model.DailyDuration
import com.solace.sleep.domain.model.InsightsData
import com.solace.sleep.domain.model.QualityPoint
import com.solace.sleep.domain.model.SessionType
import com.solace.sleep.domain.model.SleepSession
import com.solace.sleep.util.TimeExtensions.toLocalDate
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class InsightsRepository @Inject constructor(
    private val sessionRepository: SleepSessionRepository,
    private val profileRepository: ProfileRepository
) {
    suspend fun getWeeklyInsights(profileId: String, goalMinutes: Int): InsightsData {
        val today = LocalDate.now()
        val start = today.minusDays(6)
        val sessions = sessionRepository.getSessionsForDateRange(profileId, start, today)
            .filter { it.sessionType == SessionType.NIGHT_SLEEP }
        return buildInsightsData("This Week", sessions, goalMinutes, 7, start, today)
    }

    suspend fun getMonthlyInsights(profileId: String, goalMinutes: Int): InsightsData {
        val today = LocalDate.now()
        val start = today.minusDays(29)
        val sessions = sessionRepository.getSessionsForDateRange(profileId, start, today)
            .filter { it.sessionType == SessionType.NIGHT_SLEEP }
        return buildInsightsData("This Month", sessions, goalMinutes, 30, start, today)
    }

    suspend fun getAllTimeInsights(profileId: String, goalMinutes: Int): InsightsData {
        val sessions = sessionRepository.getSessionsByProfile(profileId)
            .filter { it.sessionType == SessionType.NIGHT_SLEEP }
        if (sessions.isEmpty()) {
            return emptyInsightsData("All Time", goalMinutes)
        }
        val today = LocalDate.now()
        val start = sessions.minOf { it.sleepOnset }.toLocalDate()
        val days = (today.toEpochDay() - start.toEpochDay()).toInt() + 1
        return buildInsightsData("All Time", sessions, goalMinutes, days, start, today)
    }

    private fun buildInsightsData(
        label: String,
        sessions: List<SleepSession>,
        goalMinutes: Int,
        days: Int,
        startDate: LocalDate,
        endDate: LocalDate
    ): InsightsData {
        if (sessions.isEmpty()) return emptyInsightsData(label, goalMinutes)

        val zone = ZoneId.systemDefault()
        val sessionsByDate = sessions.groupBy { it.sleepOnset.toLocalDate(zone) }

        val avgDuration = sessions.map { it.durationMinutes }.average().toInt()
        val goalPercent = if (goalMinutes > 0) avgDuration.toFloat() / goalMinutes * 100f else 0f

        val sleepDebt = calculateSleepDebt(sessions, goalMinutes, days)
        val currentStreak = calculateCurrentStreak(sessionsByDate, goalMinutes)
        val bestStreak = calculateBestStreak(sessionsByDate, goalMinutes, startDate, endDate)

        val weekdaySessions = sessions.filter {
            val dow = it.sleepOnset.toLocalDate(zone).dayOfWeek
            dow != DayOfWeek.SATURDAY && dow != DayOfWeek.SUNDAY
        }
        val weekendSessions = sessions.filter {
            val dow = it.sleepOnset.toLocalDate(zone).dayOfWeek
            dow == DayOfWeek.SATURDAY || dow == DayOfWeek.SUNDAY
        }

        val weekdayAvg = if (weekdaySessions.isNotEmpty())
            weekdaySessions.map { it.durationMinutes }.average().toInt() else 0
        val weekendAvg = if (weekendSessions.isNotEmpty())
            weekendSessions.map { it.durationMinutes }.average().toInt() else 0

        val avgQuality = sessions.mapNotNull { it.qualityScore }
            .takeIf { it.isNotEmpty() }?.average()?.toFloat()

        val formatter = DateTimeFormatter.ofPattern("MMM d")
        val dailyDurations = (0 until minOf(days, 30)).map { offset ->
            val date = endDate.minusDays(offset.toLong())
            val dayMinutes = sessionsByDate[date]?.sumOf { it.durationMinutes } ?: 0
            DailyDuration(
                dayLabel = formatter.format(date),
                minutes = dayMinutes,
                goalMinutes = goalMinutes
            )
        }.reversed()

        val qualityTrend = sessions.sortedBy { it.sleepOnset }
            .mapNotNull { session ->
                session.qualityScore?.let { score ->
                    QualityPoint(
                        dateLabel = formatter.format(session.sleepOnset.toLocalDate(zone)),
                        qualityScore = score.toFloat()
                    )
                }
            }

        return InsightsData(
            periodLabel = label,
            averageDurationMinutes = avgDuration,
            goalMinutes = goalMinutes,
            goalAchievedPercent = goalPercent,
            sleepDebtMinutes = sleepDebt,
            currentStreakDays = currentStreak,
            bestStreakDays = bestStreak,
            totalSessions = sessions.size,
            averageQualityScore = avgQuality,
            weekdayAvgMinutes = weekdayAvg,
            weekendAvgMinutes = weekendAvg,
            dailyDurations = dailyDurations,
            qualityTrend = qualityTrend
        )
    }

    private fun calculateSleepDebt(sessions: List<SleepSession>, goalMinutes: Int, days: Int): Int {
        val today = LocalDate.now()
        val windowStart = today.minusDays(days.toLong())
        val zone = ZoneId.systemDefault()
        val sessionMap = sessions
            .groupBy { it.sleepOnset.toLocalDate(zone) }
        var debt = 0
        var day = windowStart
        while (!day.isAfter(today)) {
            val dayDuration = sessionMap[day]?.sumOf { it.durationMinutes } ?: 0
            val deficit = goalMinutes - dayDuration
            if (deficit > 0) debt += deficit
            day = day.plusDays(1)
        }
        return debt
    }

    private fun calculateCurrentStreak(
        sessionsByDate: Map<LocalDate, List<SleepSession>>,
        goalMinutes: Int
    ): Int {
        var streak = 0
        var date = LocalDate.now()
        while (true) {
            val dayMinutes = sessionsByDate[date]?.sumOf { it.durationMinutes } ?: 0
            if (dayMinutes >= goalMinutes) {
                streak++
                date = date.minusDays(1)
            } else break
        }
        return streak
    }

    private fun calculateBestStreak(
        sessionsByDate: Map<LocalDate, List<SleepSession>>,
        goalMinutes: Int,
        start: LocalDate,
        end: LocalDate
    ): Int {
        var best = 0
        var current = 0
        var date = start
        while (!date.isAfter(end)) {
            val dayMinutes = sessionsByDate[date]?.sumOf { it.durationMinutes } ?: 0
            if (dayMinutes >= goalMinutes) {
                current++
                if (current > best) best = current
            } else {
                current = 0
            }
            date = date.plusDays(1)
        }
        return best
    }

    private fun emptyInsightsData(label: String, goalMinutes: Int) = InsightsData(
        periodLabel = label,
        averageDurationMinutes = 0,
        goalMinutes = goalMinutes,
        goalAchievedPercent = 0f,
        sleepDebtMinutes = 0,
        currentStreakDays = 0,
        bestStreakDays = 0,
        totalSessions = 0,
        averageQualityScore = null,
        weekdayAvgMinutes = 0,
        weekendAvgMinutes = 0,
        dailyDurations = emptyList(),
        qualityTrend = emptyList()
    )
}
