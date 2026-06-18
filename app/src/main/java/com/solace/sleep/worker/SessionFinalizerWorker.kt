package com.solace.sleep.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.data.repository.SleepSessionRepository
import com.solace.sleep.domain.model.SessionSource
import com.solace.sleep.domain.model.SleepSession
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.Instant

@HiltWorker
class SessionFinalizerWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val sessionRepository: SleepSessionRepository,
    private val profileRepository: ProfileRepository
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val profile = profileRepository.observeActiveProfile().first()
                ?: return Result.success()

            // Close sessions older than MAX_SESSION_HOURS
            val maxHours = 14L
            val staleSessions = sessionRepository.getStaleOpenSessions(profile.id, maxHours)
            staleSessions.forEach { session ->
                val closedSession = session.copy(
                    wakeTime = session.sleepOnset.plusSeconds(maxHours * 3600),
                    source = SessionSource.AUTO_CORRECTED,
                    correctionPending = true,
                    lastModifiedAt = Instant.now()
                )
                sessionRepository.updateSession(closedSession)
                Timber.d("SessionFinalizer: Closed stale session ${session.id}")
            }
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "SessionFinalizerWorker failed")
            Result.retry()
        }
    }
}
