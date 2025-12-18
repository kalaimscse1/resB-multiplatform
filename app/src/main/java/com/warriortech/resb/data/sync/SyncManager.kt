package com.warriortech.resb.data.sync

import android.content.Context
import androidx.work.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.ConnectionState
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit

class SyncManager(
    private val context: Context,
    private val networkMonitor: NetworkMonitor,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    private val workManager = WorkManager.getInstance(context)
    private val coroutineScope = CoroutineScope(Dispatchers.Main)

    init {
        observeNetworkConnectivity()
    }

    private fun observeNetworkConnectivity() {
        coroutineScope.launch {
            networkMonitor.isOnline
                .map { it == ConnectionState.Available }
                .distinctUntilChanged()
                .collect { isOnline ->
                    if (isOnline) {
                        // We're back online, schedule sync
                        scheduleSyncWork()
                    } else {
                        // We're offline, cancel any ongoing syncs
                        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
                    }
                }
        }
    }

    fun scheduleSyncWork() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val syncWorkerFactory = object : WorkerFactory() {
            override fun createWorker(
                appContext: Context,
                workerClassName: String,
                workerParameters: WorkerParameters
            ): ListenableWorker? {
                return if (workerClassName == SyncWorker::class.java.name) {
                    SyncWorker(
                        appContext,
                        workerParameters,
                        apiService,
                        sessionManager
                    )
                } else {
                    null
                }
            }
        }

//        WorkManager.initialize(
//            context,
//            Configuration.Builder()
//                .setWorkerFactory(syncWorkerFactory)
//                .build()
//        )

        // One-time sync work request
        val syncRequest = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        // Schedule unique work to ensure only one instance runs
        workManager.enqueueUniqueWork(
            SyncWorker.WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            syncRequest
        )
    }

    fun schedulePeriodicSync() {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        // Periodic sync work request (minimum 15 minutes)
        val periodicSyncRequest = PeriodicWorkRequestBuilder<SyncWorker>(
            15, TimeUnit.MINUTES
        )
            .setConstraints(constraints)
            .build()

        // Schedule unique periodic work
        workManager.enqueueUniquePeriodicWork(
            "${SyncWorker.WORK_NAME}_PERIODIC",
            ExistingPeriodicWorkPolicy.KEEP,
            periodicSyncRequest
        )
    }

    fun cancelSync() {
        workManager.cancelUniqueWork(SyncWorker.WORK_NAME)
    }
}