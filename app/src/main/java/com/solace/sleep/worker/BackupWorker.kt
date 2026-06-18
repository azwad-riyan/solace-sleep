package com.solace.sleep.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.solace.sleep.data.preferences.AppPreferences
import com.solace.sleep.data.repository.SyncRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import timber.log.Timber

@HiltWorker
class BackupWorker @AssistedInject constructor(
    @Assisted appContext: Context,
    @Assisted params: WorkerParameters,
    private val syncRepository: SyncRepository,
    private val preferences: AppPreferences
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        return try {
            val syncEnabled = preferences.driveSyncEnabled.first()
            if (!syncEnabled) return Result.success()

            val authToken = getAuthToken() ?: return Result.success()
            syncRepository.syncToCloud(authToken)
                .fold(
                    onSuccess = {
                        preferences.setLastBackupTimestamp(System.currentTimeMillis())
                        Result.success()
                    },
                    onFailure = { e ->
                        Timber.e(e, "BackupWorker failed")
                        Result.retry()
                    }
                )
        } catch (e: Exception) {
            Timber.e(e, "BackupWorker exception")
            Result.retry()
        }
    }

    private fun getAuthToken(): String? = null
}
