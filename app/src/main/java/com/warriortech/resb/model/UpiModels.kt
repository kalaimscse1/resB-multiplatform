package com.warriortech.resb.model

data class TblUpiType(
    var upi_type_id: Long = 0,
    var upi_type_name: String,
    var is_active: Boolean = true
)
