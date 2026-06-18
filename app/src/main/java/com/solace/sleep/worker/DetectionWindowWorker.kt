package com.solace.sleep.worker

import android.content.Context
import android.content.Intent
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.ProfileRepository
import com.solace.sleep.detection.SleepDetectionService
import com.solace.sleep.util.isWithinDetectionWindow
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber
import java.time.LocalTime

@HiltWorker
class DetectionWindowWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val profileRepository: ProfileRepository,
    private val preferences: AppPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val profile = profileRepository.observeActiveProfile().first()
                ?: return Result.success()

            val now = LocalTime.now()
            val inWindow = isWithinDetectionWindow(
                now,
                profile.detectionWindowStart,
                profile.detectionWindowEnd
            )

            val serviceRunning = preferences.detectionServiceRunning.first()

            if (inWindow && !serviceRunning) {
                Timber.d("DetectionWindowWorker: Starting detection service")
                val intent = Intent(applicationContext, SleepDetectionService::class.java)
                applicationContext.startForegroundService(intent)
            } else if (!inWindow && serviceRunning) {
                Timber.d("DetectionWindowWorker: Stopping detection service (outside window)")
                val intent = Intent(applicationContext, SleepDetectionService::class.java)
                applicationContext.stopService(intent)
            }

            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "DetectionWindowWorker failed")
            Result.retry()
        }
    }
}
