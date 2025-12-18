package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tbl_item_add_on")
data class TblItemAddOn(
    @PrimaryKey(autoGenerate = true) val item_add_on_id: Int = 0,
    val add_on_id: Int?,
    val menu_item_id: Int?,
    val is_required: Boolean?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)
