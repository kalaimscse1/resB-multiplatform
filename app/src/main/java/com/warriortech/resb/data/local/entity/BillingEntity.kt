package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_billing",
    foreignKeys = [
        ForeignKey(entity = TblOrderMaster::class, parentColumns = ["order_master_id"], childColumns = ["order_master_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblStaff::class, parentColumns = ["staff_id"], childColumns = ["staff_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblVoucher::class, parentColumns = ["voucher_id"], childColumns = ["voucher_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["order_master_id"]),
        Index(value = ["staff_id"]),
        Index(value = ["voucher_id"]),
    ]
)
data class TblBilling(
    @PrimaryKey val bill_no: String,
    val bill_date: String?,
    val bill_create_time: String?,
    val order_master_id: String?,
    val voucher_id: Int?,
    val staff_id: Int?,
    val customer_id: Int?,
    val order_amt: Double?,
    val disc_amt: Double?,
    val tax_amt: Double?,
    val cess: Double?,
    val cess_specific: Double?,
    val delivery_amt: Double?,
    val grand_total: Double?,
    val round_off: Double?,
    val rounded_amt: Double?,
    val cash: Double?,
    val card: Double?,
    val upi: Double?,
    val due: Double?,
    val others: Double?,
    val received_amt: Double?,
    val pending_amt: Double?,
    val change: Double?,
    val note: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)