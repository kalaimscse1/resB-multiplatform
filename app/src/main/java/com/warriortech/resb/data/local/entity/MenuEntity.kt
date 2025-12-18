package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tbl_menu")
data class TblMenu(
    @PrimaryKey(autoGenerate = true) val menu_id: Int = 0,
    val menu_name: String?,
    val order_by: Int?,
    val is_active: Boolean?,
    val start_time: Double?, // float -> Double
    val end_time: Double?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)