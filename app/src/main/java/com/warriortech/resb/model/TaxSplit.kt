package com.warriortech.resb.model

data class TblTaxSplit(
    var tax_split_id: Long,
    var tax_id: Long,
    var tax_name: String,
    var tax_split_name: String,
    var tax_split_percentage: String,
    var is_active: Boolean
)

data class TaxSplit(
    var tax_split_id: Long,
    var tax_id: Long,
    var tax_split_name: String,
    var tax_split_percentage: String,
    var is_active: Boolean
)