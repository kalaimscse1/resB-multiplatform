package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_general_settings")
data class TblGeneralSettings(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val company_name_font: Int?,
    val address_font: Int?,
    val is_tax: Boolean?,
    val is_tax_included: Boolean?,
    val is_round_off: Boolean?,
    val is_allowed_disc: Boolean?,
    val disc_by: Int?,
    val disc_amt: Double?,
    val is_tendered: Boolean?,
    val is_gst_summary: Boolean?,
    val is_receipt: Boolean?,
    val is_kot: Boolean?,
    val is_logo: Boolean?,
    val logo_path: String?,
    val is_cess: Boolean?,
    val is_delivery_charge: Boolean?,
    val is_table_allowed: Boolean?,
    val is_waiter_allowed: Boolean?,
    val menu_show_in_time: Boolean?,
    val tamil_receipt_print: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)