package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tbl_sale_add_on",
    foreignKeys = [
        ForeignKey(entity = TblAddOn::class, parentColumns = ["add_on_id"], childColumns = ["item_add_on_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["item_add_on_id"]),
    ]
)
data class TblSaleAddOn(
    @PrimaryKey(autoGenerate = true) val sale_add_on_id: Int = 0,
    val order_master_id: String?,
    val item_add_on_id: Int?,
    val menu_item_id: Int?,
    val status: Boolean?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)