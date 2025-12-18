package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tbl_item_category")
data class TblItemCategory(
    @PrimaryKey(autoGenerate = true) val item_cat_id: Int = 0,
    val item_cat_name: String?,
    val order_by: Int?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)