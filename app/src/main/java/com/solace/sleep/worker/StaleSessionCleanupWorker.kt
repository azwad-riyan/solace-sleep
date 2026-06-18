package com.solace.sleep.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class StaleSessionCleanupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sessionRepository: SleepSessionRepository,
    private val profileRepository: ProfileRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val profiles = profileRepository.getAllProfiles()
            profiles.forEach { profile ->
                val staleSessions = sessionRepository.getStaleOpenSessions(profile.id, cutoffHoursAgo = 20)
                staleSessions.forEach { session ->
                    Timber.d("StaleCleanup: Removing stale session ${session.id}")
                    sessionRepository.deleteSession(session.id)
                }
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "StaleSessionCleanupWorker failed")
            Result.retry()
        }
    }
}
