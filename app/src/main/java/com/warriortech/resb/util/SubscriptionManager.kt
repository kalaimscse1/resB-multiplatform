package com.warriortech.resb.util

import com.warriortech.resb.network.SessionManager
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SubscriptionManager @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "SubscriptionManager"
        private const val KEY_SUBSCRIPTION_END_DATE = "subscription_end_date"
        private const val KEY_LAST_NOTIFICATION_DATE = "last_notification_date"
    }

    fun saveSubscriptionEndDate(endDate: LocalDate) {
        val dateString = endDate.format(DateTimeFormatter.ISO_LOCAL_DATE)
        sessionManager.saveSubscriptionEndDate(dateString)
    }

    fun getSubscriptionEndDate(): LocalDate? {
        return sessionManager.getSubscriptionEndDate()?.let { dateString ->
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }
    }

    fun getDaysUntilExpiration(): Long? {
        val endDate = getSubscriptionEndDate() ?: return null
        val today = LocalDate.now()
        return ChronoUnit.DAYS.between(today, endDate)
    }

    fun isSubscriptionExpired(): Boolean {
        val daysUntilExpiration = getDaysUntilExpiration() ?: return false
        return daysUntilExpiration < 0
    }

    fun shouldShowExpirationWarning(): Boolean {
        val daysUntilExpiration = getDaysUntilExpiration() ?: return false
        return daysUntilExpiration in 1..7
    }

    fun shouldShowNotificationToday(): Boolean {
        if (!shouldShowExpirationWarning()) return false

        val today = LocalDate.now()
        val lastNotificationDate = getLastNotificationDate()

        return lastNotificationDate == null || lastNotificationDate.isBefore(today)
    }

    fun markNotificationShownToday() {
        val today = LocalDate.now()
        val dateString = today.format(DateTimeFormatter.ISO_LOCAL_DATE)
        sessionManager.saveLastNotificationDate(dateString)
    }

    private fun getLastNotificationDate(): LocalDate? {
        return sessionManager.getLastNotificationDate()?.let { dateString ->
            try {
                LocalDate.parse(dateString, DateTimeFormatter.ISO_LOCAL_DATE)
            } catch (e: Exception) {
                null
            }
        }
    }
}
