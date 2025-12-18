package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_counter")
data class TblCounter(
    @PrimaryKey(autoGenerate = true) val counter_id: Int = 0,
    val counter_name: String?,
    val ip_address: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)