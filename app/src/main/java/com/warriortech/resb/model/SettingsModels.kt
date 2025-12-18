package com.warriortech.resb.model

data class Menu(
    val menu_id: Long,
    val menu_name: String,
    val order_by: String,
    val start_time: Float,
    val end_time: Float,
    val is_active: Boolean,
)

data class Printer(
    val printer_id: Long,
    val printer_name: String,
    val kitchen_cat_id: Long,
    val ip_address: String,
    val is_active: Long
)

data class TblPrinterResponse(
    var printer_id: Long,
    var kitchen_cat: TblKitchenCategory,
    var printer_name: String,
    var ip_address: String,
    var is_active: Long
)

data class TblKitchenCategory(
    var kitchen_cat_id: Long,
    var kitchen_cat_name: String,
    var is_active: Long
)



