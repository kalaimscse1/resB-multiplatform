package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Ignore
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_voucher",
    foreignKeys = [
        ForeignKey(
            entity = TblVoucherType::class,
            parentColumns = ["voucher_type_id"],
            childColumns = ["voucher_type"],
            onDelete = ForeignKey.CASCADE
        ),
    ],
    indices = [
        Index(value = ["voucher_type"]),
    ]
)

data class TblVoucher(
    @PrimaryKey(autoGenerate = true) val voucher_id: Int = 0,
    val counter_id: Int?,
    val voucher_name: String?,
    val voucher_type: Int?,
    val voucher_prefix: String?,
    val voucher_suffix: String?,
    val starting_no: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)
