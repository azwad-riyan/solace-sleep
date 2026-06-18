package com.solace.sleep.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.solace.sleep.util.Constants
import java.util.concurrent.TimeUnit

object WorkScheduler {

    fun scheduleAll(context: Context) {
        val wm = WorkManager.getInstance(context)
        scheduleDetectionWindow(wm)
        scheduleSessionFinalizer(wm)
        scheduleSync(wm)
        scheduleBackup(wm)
        scheduleStaleCleanup(wm)
    }

    private fun scheduleDetectionWindow(wm: WorkManager) {
        val request = PeriodicWorkRequestBuilder<DetectionWindowWorker>(
            Constants.DETECTION_INTERVAL_MINUTES, TimeUnit.MINUTES
        ).addTag(Constants.WORK_DETECTION_WINDOW)
            .build()
        wm.enqueueUniquePeriodicWork(
            Constants.WORK_DETECTION_WINDOW,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleSessionFinalizer(wm: WorkManager) {
        val request = PeriodicWorkRequestBuilder<SessionFinalizerWorker>(1, TimeUnit.DAYS)
            .addTag(Constants.WORK_SESSION_FINALIZER)
            .build()
        wm.enqueueUniquePeriodicWork(
            Constants.WORK_SESSION_FINALIZER,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleSync(wm: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            Constants.SYNC_INTERVAL_HOURS, TimeUnit.HOURS
        )
            .setConstraints(constraints)
            .addTag(Constants.WORK_SYNC)
            .build()
        wm.enqueueUniquePeriodicWork(
            Constants.WORK_SYNC,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleBackup(wm: WorkManager) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<BackupWorker>(
            Constants.BACKUP_INTERVAL_DAYS, TimeUnit.DAYS
        )
            .setConstraints(constraints)
            .addTag(Constants.WORK_BACKUP)
            .build()
        wm.enqueueUniquePeriodicWork(
            Constants.WORK_BACKUP,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun scheduleStaleCleanup(wm: WorkManager) {
        val request = PeriodicWorkRequestBuilder<StaleSessionCleanupWorker>(1, TimeUnit.DAYS)
            .addTag(Constants.WORK_STALE_CLEANUP)
            .build()
        wm.enqueueUniquePeriodicWork(
            Constants.WORK_STALE_CLEANUP,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
