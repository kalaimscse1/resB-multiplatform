package com.warriortech.resb.data.sync

import android.content.Context
import com.warriortech.resb.data.local.RestaurantDatabase
import com.warriortech.resb.data.local.entity.SyncQueueItem
import com.warriortech.resb.data.local.entity.SyncEntityType
import com.warriortech.resb.data.local.entity.SyncOperation
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

class OfflineQueueManager(context: Context) {
    private val database = RestaurantDatabase.getDatabase(context)
    private val syncQueueDao = database.syncQueueDao()
    private val gson = Gson()

    /**
     * Add an operation to the sync queue
     */
    suspend fun queueOperation(
        entityType: SyncEntityType,
        entityId: Long,
        operation: SyncOperation,
        payload: Any
    ) {
        try {
            val jsonPayload = gson.toJson(payload)
            val item = SyncQueueItem(
                entityType = entityType,
                entityId = entityId,
                operation = operation,
                payload = jsonPayload,
                status = "pending"
            )
            syncQueueDao.insert(item)
            Timber.d("Queued $operation for $entityType (ID: $entityId)")
        } catch (e: Exception) {
            Timber.e(e, "Error queuing operation for $entityType")
        }
    }

    /**
     * Get pending sync items
     */
    fun getPendingItems(): Flow<List<SyncQueueItem>> {
        return syncQueueDao.getPendingItems()
    }

    /**
     * Get pending items with limit
     */
    suspend fun getPendingItemsLimit(limit: Int = 50): List<SyncQueueItem> {
        return syncQueueDao.getPendingItemsLimit(limit)
    }

    /**
     * Mark item as in progress
     */
    suspend fun markInProgress(item: SyncQueueItem) {
        syncQueueDao.update(item.copy(status = "in_progress", lastAttemptAt = System.currentTimeMillis()))
    }

    /**
     * Mark item as completed
     */
    suspend fun markCompleted(item: SyncQueueItem) {
        syncQueueDao.update(item.copy(status = "completed"))
    }

    /**
     * Mark item as failed
     */
    suspend fun markFailed(item: SyncQueueItem, error: String) {
        val updatedItem = item.copy(
            status = "failed",
            error = error,
            attemptCount = item.attemptCount + 1,
            lastAttemptAt = System.currentTimeMillis()
        )
        syncQueueDao.update(updatedItem)
    }

    /**
     * Retry failed items
     */
    suspend fun retryFailedItems(maxAttempts: Int = 3) {
        val failedItems = syncQueueDao.getFailedItems()
        failedItems.forEach { item ->
            if (item.attemptCount < maxAttempts) {
                syncQueueDao.update(item.copy(status = "pending", attemptCount = item.attemptCount + 1))
            }
        }
    }

    /**
     * Get pending count
     */
    fun getPendingCount(): Flow<Int> {
        return syncQueueDao.getPendingCount()
    }

    /**
     * Clean up completed items
     */
    suspend fun cleanupCompleted() {
        syncQueueDao.deleteCompletedItems()
    }

    /**
     * Reset stale in-progress items (not updated in last hour)
     */
    suspend fun resetStaleInProgress() {
        val oneHourAgo = System.currentTimeMillis() - (60 * 60 * 1000)
        syncQueueDao.resetStaleInProgressItems(oneHourAgo)
    }

    /**
     * Get payload as specific type
     */
    private inline fun <reified T> getPayload(item: SyncQueueItem): T? {
        return try {
            gson.fromJson(item.payload, T::class.java)
        } catch (e: Exception) {
            Timber.e(e, "Error parsing payload for item ${item.id}")
            null
        }
    }
}
