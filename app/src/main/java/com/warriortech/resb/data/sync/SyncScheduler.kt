package com.warriortech.resb.data.sync

import android.content.Context
import androidx.work.*
import dagger.hilt.android.qualifiers.ApplicationContext
import timber.log.Timber
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SyncScheduler @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val workManager = WorkManager.getInstance(context)

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .setRequiresBatteryNotLow(true)
            .build()

        val syncWorkRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            repeatInterval = 15, // Sync every 15 minutes
            repeatIntervalTimeUnit = TimeUnit.MINUTES,
            flexTimeInterval = 5, // 5 minutes flex time
            flexTimeIntervalUnit = TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .setBackoffCriteria(
                BackoffPolicy.EXPONENTIAL,
                WorkRequest.MIN_BACKOFF_MILLIS,
                TimeUnit.MILLISECONDS
            )
            .addTag("periodic_sync")
            .build()

        workManager.enqueueUniquePeriodicWork(
            "background_sync",
            ExistingPeriodicWorkPolicy.KEEP,
            syncWorkRequest
        )

        Timber.d("Periodic background sync scheduled")
    }

    fun scheduleSingleSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .addTag("manual_sync")
            .build()

        workManager.enqueue(syncWorkRequest)
        Timber.d("Manual sync scheduled")
    }

    fun cancelAllSync() {
        workManager.cancelAllWorkByTag("periodic_sync")
        workManager.cancelAllWorkByTag("manual_sync")
        Timber.d("All sync work cancelled")
    }
}
