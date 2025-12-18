package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_kitchen_category")
data class TblKitchenCategory(
    @PrimaryKey(autoGenerate = true) val kitchen_cat_id: Int = 0,
    val kitchen_cat_name: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)