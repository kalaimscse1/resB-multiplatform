package com.warriortech.resb.util

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.warriortech.resb.worker.SubscriptionCheckWorker
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionScheduler @Inject constructor(
    private val context: Context
) {
    companion object {
        private const val SUBSCRIPTION_CHECK_WORK = "subscription_check_work"
    }

    fun scheduleSubscriptionChecks() {
        val subscriptionCheckRequest = PeriodicWorkRequestBuilder<SubscriptionCheckWorker>(
            24, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            SUBSCRIPTION_CHECK_WORK,
            ExistingPeriodicWorkPolicy.KEEP,
            subscriptionCheckRequest
        )
    }

    fun cancelSubscriptionChecks() {
        WorkManager.getInstance(context).cancelUniqueWork(SUBSCRIPTION_CHECK_WORK)
    }
}
