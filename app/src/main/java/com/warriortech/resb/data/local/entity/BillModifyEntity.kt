package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey


@Entity(tableName = "tbl_bill_modify")
data class TblBillModify(
    @PrimaryKey(autoGenerate = true) val bill_modify_id: Int = 0,
    val bill_no: String?,
    val modify_date: String?, // use string for date
    val modify_time: String?,
    val staff_id: Int?,
    val modify_delete: String?,
    val note: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)
