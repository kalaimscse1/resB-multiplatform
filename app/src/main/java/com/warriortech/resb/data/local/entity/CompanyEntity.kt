package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_company")
data class TblCompany(
    @PrimaryKey val company_code: String,
    val company_name: String?,
    val owner_name: String?,
    val address1: String?,
    val address2: String?,
    val place: String?,
    val pincode: String?,
    val contact_no: String?,
    val mail_id: String?,
    val country: String?,
    val state: String?,
    val currency: String?,
    val tax_no: String?,
    val decimal_point: Int?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)