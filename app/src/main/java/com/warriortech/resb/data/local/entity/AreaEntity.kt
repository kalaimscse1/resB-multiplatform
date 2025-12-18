package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_area")
data class TblArea(
    @PrimaryKey(autoGenerate = true) val area_id: Int,
    val area_name: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)