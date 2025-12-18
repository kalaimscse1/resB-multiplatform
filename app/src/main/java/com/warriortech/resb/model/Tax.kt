package com.warriortech.resb.model

data class Tax(
    val tax_id: Long,
    val tax_name: String,
    val tax_percentage: Double,
    val cess_percentage: Double,
    val is_active: Boolean,
)
