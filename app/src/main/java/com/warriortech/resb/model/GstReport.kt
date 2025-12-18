package com.warriortech.resb.model

data class HsnReport(
    val hsn_code: String,
    val item_cat_name: String,
    val unit_name: String,
    val qty: Int,
    val total: Double,
    val tax_percentage: Double,
    val taxable_value: Double,
    val igst: Double,
    val cgst: Double,
    val sgst: Double
)


data class GSTRDOCS(
    val description: String,
    val billFrom: String,
    val billTo: String,
    val nos: Int,
    val cancelled: Int,
)

data class ReportGSTResponse(
    val menu_item_id: Long,
    val menu_item_name: String,
    val qty: Int,
    val total: Double,
    val tax_percentage: Double,
    val tax_amount: Double,
    val sgst: Double,
    val cgst: Double,
    val igst: Double,
    val grand_total: Double,
)