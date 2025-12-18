package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_customer")
data class TblCustomers(
    @PrimaryKey(autoGenerate = true) val customer_id: Int = 0,
    val customer_name: String?,
    val contact_no: String?,
    val address: String?,
    val email_address: String?,
    val gst_no: String?,
    val igst_status: Boolean?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)