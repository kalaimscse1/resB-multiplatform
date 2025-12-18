package com.warriortech.resb.model

data class TblGroupNature(
    val g_nature_id: Int,
    val g_nature_name: String,
    val is_active: Boolean
)

data class TblBankDetails(
    val ledger_name: String,
    val bank_name: String,
    val account_no: String,
    val ifsc_code: String,
    val upi_id: String
)

data class TblLedgerDetails(
    val ledger_id: Int,
    val ledger_name: String,
    val ledger_fullname: String,
    val order_by: Int,
    val group: TblGroupDetails,
    val address: String,
    val address1: String,
    val place: String,
    val pincode: Int,
    val country: String,
    val contact_no: String,
    val email: String,
    val gst_no: String,
    val pan_no: String,
    val state_code: String,
    val state_name: String,
    val sac_code: String,
    val igst_status: String,
    val opening_balance: String,
    val due_date: String,
    val bank_details: String,
    val tamil_text: String,
    val distance: Double,
    val is_default: Boolean,
    val is_active: Boolean
)

data class TblLedgerRequest(
    val ledger_id:Int =0,
    val ledger_name: String,
    val ledger_fullname: String,
    val order_by: Int,
    val group_id: Int,
    val address: String,
    val address1: String,
    val place: String,
    val pincode: Int,
    val country: String,
    val contact_no: String,
    val email: String,
    val gst_no: String,
    val pan_no: String,
    val state_code: String,
    val state_name: String,
    val sac_code: String,
    val igst_status: String,
    val opening_balance: String,
    val due_date: String,
    val bank_details: String,
    val tamil_text: String,
    val distance: Double,
    val is_default: Boolean,
    val is_active: Boolean = true
)

data class TblGroupDetails(
    val group_id: Int,
    val group_name: String,
    val group_fullname: String,
    val group_order: Int,
    val sub_group: String,
    val group_nature: TblGroupNature,
    val gross_profit: String,
    val tamil_text: String,
    val is_default: Boolean,
    val is_active: Boolean,
    val group_by: Int
)

data class TblGroupRequest(
    val group_id: Int,
    val group_name: String,
    val group_fullname: String,
    val group_order: Int,
    val sub_group: String,
    val group_nature: Int,
    val gross_profit: String,
    val tamil_text: String,
    val is_default: Boolean,
    val is_active: Boolean,
    val group_by: Int
)