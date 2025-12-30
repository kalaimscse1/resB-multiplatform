package com.warriortech.resb.data.local.dao

import androidx.room.*
import com.warriortech.resb.data.local.entity.SyncQueueItem
import com.warriortech.resb.data.local.entity.SyncEntityType
import com.warriortech.resb.data.local.entity.SyncOperation
import kotlinx.coroutines.flow.Flow

@Dao
interface SyncQueueDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: SyncQueueItem)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<SyncQueueItem>)

    @Update
    suspend fun update(item: SyncQueueItem)

    @Delete
    suspend fun delete(item: SyncQueueItem)

    @Query("DELETE FROM sync_queue WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' ORDER BY createdAt ASC")
    fun getPendingItems(): Flow<List<SyncQueueItem>>

    @Query("SELECT * FROM sync_queue WHERE status = 'pending' LIMIT :limit")
    suspend fun getPendingItemsLimit(limit: Int): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE status = 'in_progress'")
    suspend fun getInProgressItems(): List<SyncQueueItem>

    @Query("SELECT * FROM sync_queue WHERE status = 'failed' ORDER BY lastAttemptAt DESC LIMIT :limit")
    suspend fun getFailedItems(limit: Int = 100): List<SyncQueueItem>

    @Query("SELECT COUNT(*) FROM sync_queue WHERE status = 'pending'")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT * FROM sync_queue WHERE entityType = :entityType AND entityId = :entityId ORDER BY createdAt DESC LIMIT 1")
    suspend fun getLatestByEntity(entityType: SyncEntityType, entityId: Long): SyncQueueItem?

    @Query("DELETE FROM sync_queue WHERE status = 'completed'")
    suspend fun deleteCompletedItems()

    @Query("UPDATE sync_queue SET status = 'pending' WHERE status = 'in_progress' AND lastAttemptAt < :timestamp")
    suspend fun resetStaleInProgressItems(timestamp: Long)

    @Query("SELECT * FROM sync_queue ORDER BY createdAt DESC")
    fun getAllItems(): Flow<List<SyncQueueItem>>
}
