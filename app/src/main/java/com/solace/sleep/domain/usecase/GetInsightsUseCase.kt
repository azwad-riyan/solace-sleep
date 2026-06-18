package com.solace.sleep.domain.usecase

import com.solace.sleep.data.repository.InsightsRepository
import com.solace.sleep.domain.model.InsightsData
import javax.inject.Inject

class GetInsightsUseCase @Inject constructor(
    private val repository: InsightsRepository
) {
    suspend fun weekly(profileId: String, goalMinutes: Int): InsightsData =
        repository.getWeeklyInsights(profileId, goalMinutes)

    suspend fun monthly(profileId: String, goalMinutes: Int): InsightsData =
        repository.getMonthlyInsights(profileId, goalMinutes)

    suspend fun allTime(profileId: String, goalMinutes: Int): InsightsData =
        repository.getAllTimeInsights(profileId, goalMinutes)
}
