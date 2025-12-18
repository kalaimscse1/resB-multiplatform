package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tbl_voucher_type")
data class TblVoucherType(
    @PrimaryKey(autoGenerate = true) val voucher_type_id: Int = 0,
    val voucher_type_name: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)