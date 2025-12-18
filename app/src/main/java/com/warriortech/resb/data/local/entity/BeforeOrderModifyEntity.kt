package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_before_order_details_modify")
data class TblBeforeOrderDetailsModify(
    @PrimaryKey(autoGenerate = true) val order_details_id: Int,
    val order_master_id: String?,
    val kot_number: Int?,
    val menu_item_id: Int?,
    val rate: Double?,
    val qty: Int?,
    val total: Double?,
    val tax_id: Int?,
    val tax_amount: Double?,
    val sgst_per: Double?,
    val sgst: Double?,
    val cgst_per: Double?,
    val cgst: Double?,
    val igst_per: Double?,
    val igst: Double?,
    val cess_per: Double?,
    val cess: Double?,
    val cess_specific: Double?,
    val grand_total: Double?,
    val prepare_status: Boolean?,
    val item_add_mode: Boolean?,
    val is_flag: Boolean?,
    val merge_order_nos: String?,
    val merge_order_tables: String?,
    val merge_pax: Int?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)