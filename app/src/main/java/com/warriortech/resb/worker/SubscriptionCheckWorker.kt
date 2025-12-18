
package com.warriortech.resb.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.warriortech.resb.notification.NotificationHelper
import com.warriortech.resb.util.SubscriptionManager
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SubscriptionCheckWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    @EntryPoint
    @InstallIn(SingletonComponent::class)
    interface SubscriptionCheckWorkerEntryPoint {
        fun subscriptionManager(): SubscriptionManager
        fun notificationHelper(): NotificationHelper
    }

    override suspend fun doWork(): Result = withContext(Dispatchers.IO)  {
        try {
            val entryPoint = EntryPointAccessors.fromApplication(
                applicationContext,
                SubscriptionCheckWorkerEntryPoint::class.java
            )
            
            val subscriptionManager = entryPoint.subscriptionManager()
            val notificationHelper = entryPoint.notificationHelper()

            if (subscriptionManager.shouldShowNotificationToday()) {
                val daysUntilExpiration = subscriptionManager.getDaysUntilExpiration()
                if (daysUntilExpiration != null && daysUntilExpiration > 0) {
                    notificationHelper.showSubscriptionExpirationWarning(daysUntilExpiration)
                    subscriptionManager.markNotificationShownToday()
                }
            }

            Result.success()
        } catch (e: Exception) {
            Result.failure()
        }
    }
}
