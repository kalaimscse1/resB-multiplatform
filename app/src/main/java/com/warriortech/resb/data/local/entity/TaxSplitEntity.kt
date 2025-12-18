package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_tax_split",
    foreignKeys = [
        ForeignKey(entity = TblTax::class, parentColumns = ["tax_id"], childColumns = ["tax_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["tax_id"]),
    ]
)
data class TblTaxSplit(
    @PrimaryKey(autoGenerate = true) val tax_split_id: Int = 0,
    val tax_id: Int?,
    val tax_split_name: String?,
    val tax_split_percentage: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)