package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tbl_add_on",
    foreignKeys = [
        ForeignKey(entity = TblItemCategory::class, parentColumns = ["item_cat_id"], childColumns = ["item_cat_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["item_cat_id"]),
    ]
)
data class TblAddOn(
    @PrimaryKey(autoGenerate = true) val add_on_id: Int,
    val item_cat_id: Int?,
    val add_on_name: String?,
    val add_on_price: Double?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)