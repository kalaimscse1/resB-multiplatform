package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_printer",
    foreignKeys = [
        ForeignKey(entity = TblAddOn::class, parentColumns = ["add_on_id"], childColumns = ["kitchen_cat_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["kitchen_cat_id"]),
    ]
)
data class TblPrinter(
    @PrimaryKey(autoGenerate = true) val printer_id: Int = 0,
    val kitchen_cat_id: Int?,
    val printer_name: String?,
    val ip_address: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)