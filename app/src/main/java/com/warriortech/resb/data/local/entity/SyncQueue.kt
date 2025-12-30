package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.UUID

enum class SyncOperation {
    CREATE, UPDATE, DELETE
}

enum class SyncEntityType {
    BILL, ORDER, CUSTOMER, MENU_ITEM, TABLE, AREA, STAFF, PRINTER, COUNTER, ROLE
}

@Entity(tableName = "sync_queue")
data class SyncQueueItem(
    @PrimaryKey
    val id: String = UUID.randomUUID().toString(),
    val entityType: SyncEntityType,
    val entityId: Long,
    val operation: SyncOperation,
    val payload: String, // JSON payload
    val createdAt: Long = System.currentTimeMillis(),
    val attemptCount: Int = 0,
    val lastAttemptAt: Long? = null,
    val error: String? = null,
    val status: String = "pending" // pending, in_progress, completed, failed
)
