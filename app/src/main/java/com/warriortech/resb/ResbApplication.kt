package com.warriortech.resb

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import androidx.work.ListenableWorker
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import com.warriortech.resb.data.sync.SyncManager
import com.warriortech.resb.data.sync.SyncWorker
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.LocaleHelper
import com.warriortech.resb.util.NetworkMonitor
import com.warriortech.resb.util.SubscriptionScheduler
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.BuildConfig
import timber.log.Timber
import javax.inject.Inject

@HiltAndroidApp
class ResbApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var subscriptionScheduler: SubscriptionScheduler

    @Inject
    lateinit var workerFactory: HiltWorkerFactory

    @Inject
    lateinit var sessionManager: SessionManager

    @Inject
    lateinit var apiService: ApiService

    override fun onCreate() {
        super.onCreate()

        // Schedule subscription checks
        subscriptionScheduler.scheduleSubscriptionChecks()

        // Enable logging in debug
        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }

        // Apply locale in background
        CoroutineScope(Dispatchers.Default).launch {
            LocaleHelper.applyLocale(this@ResbApplication)
        }
    }

    override fun getWorkManagerConfiguration(): Configuration {
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
        return Configuration.Builder()
            .setMinimumLoggingLevel(Log.INFO)
            .setWorkerFactory(syncWorkerFactory)
            .build()
    }

    private fun isDebugBuild(): Boolean {
        return applicationInfo.flags and android.content.pm.ApplicationInfo.FLAG_DEBUGGABLE != 0
    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(LocaleHelper.onAttach(base ?: this))
    }

    override fun onConfigurationChanged(newConfig: android.content.res.Configuration) {
        super.onConfigurationChanged(newConfig)
        LocaleHelper.onAttach(this)
    }

    companion object {
        lateinit var sharedPreferences: SharedPreferences
            private set
    }
}
