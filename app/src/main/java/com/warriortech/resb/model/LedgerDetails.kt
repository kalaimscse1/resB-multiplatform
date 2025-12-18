package com.warriortech.resb.model

data class TblLedgerDetailsIdResponse(
    var ledger_details_id: Long,
    var ledger:TblLedgerDetailsResponse,
    var bill_no:String,
    var date:String,
    var time:String,
    var party_member:String,
    var party:TblLedgerDetailsResponse,
    var member:String,
    var member_id:String,
    var purpose:String,
    var amount_in:Double,
    var amount_out:Double,
    var is_active:Boolean
)
data class TblLedgerDetailsResponse(
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

data class TblLedgerDetailIdRequest(
    var ledger_details_id: Long=0,
    var id:Long,
    var bill_no:String,
    var date:String,
    var time:String,
    var party_member:String,
    var party_id:Long,
    var member:String,
    var member_id:String,
    var purpose: String,
    var amount_in:Double,
    var amount_out:Double,
    var is_active:Boolean=true
)