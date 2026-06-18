package com.solace.sleep.domain.model

data class InsightsData(
    val periodLabel: String,
    val averageDurationMinutes: Int,
    val goalMinutes: Int,
    val goalAchievedPercent: Float,
    val sleepDebtMinutes: Int,
    val currentStreakDays: Int,
    val bestStreakDays: Int,
    val totalSessions: Int,
    val averageQualityScore: Float?,
    val weekdayAvgMinutes: Int,
    val weekendAvgMinutes: Int,
    val dailyDurations: List<DailyDuration>,
    val qualityTrend: List<QualityPoint>
)

data class DailyDuration(
    val dayLabel: String,
    val minutes: Int,
    val goalMinutes: Int
)

data class QualityPoint(
    val dateLabel: String,
    val qualityScore: Float
)
