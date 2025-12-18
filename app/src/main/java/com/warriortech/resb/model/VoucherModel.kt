package com.warriortech.resb.model

data class TblVoucherRequest(
    var voucher_id: Long,
    var counter_id: Long,
    var voucher_type: Long,
    var voucher_name: String,
    var voucher_prefix: String,
    var voucher_suffix: String,
    var starting_no: String,
    var is_active: Boolean
)

data class TblVoucherResponse(
    var voucher_id: Long,
    var counter: TblCounter,
    var voucherType: TblVoucherType,
    var voucher_name: String,
    var voucher_prefix: String,
    var voucher_suffix: String,
    var starting_no: String,
    var is_active: Boolean
)

data class TblVoucherType(
    var voucher_Type_id: Long,
    var voucher_type_name: String,
    var is_active: Boolean,
)

data class TblVoucher(
    var voucher_id: Long,
    var counter: TblCounter,
    var voucherType: TblVoucherType,
    var voucher_name: String,
    var voucher_prefix: String,
    var voucher_suffix: String,
    var starting_no: String,
    var is_active: Boolean
)