package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_table",
    indices = [
        Index(value = ["area_id"]),
    ]
)
data class TblTableEntity(
    @PrimaryKey(autoGenerate = true) val table_id: Int = 0,
    val area_id: Int?,
    val table_name: String?,
    val seating_capacity: Int?,
    val is_ac: String?,
    val table_status: String?,
    val table_availability: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null,

    val created_at: Long = System.currentTimeMillis(),
    val updated_at: Long = System.currentTimeMillis()
)

enum class SyncStatus {
    SYNCED,           // Data is synchronized with the server
    PENDING_SYNC,     // Local changes need to be synced to server
    SYNC_FAILED,
    PENDING_DELETE,
    PENDING_UPDATE,// Failed to sync with server
}