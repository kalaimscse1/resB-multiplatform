package com.warriortech.resb.model

data class Table(
    val table_id: Long,
    val area_id: Long,
    val area_name: String,
    val table_name: String,
    val seating_capacity: Int,
    val is_ac: String,  // e.g., "AC hall", "Non-AC"
    val table_status: String,
    val table_availability: String,
    val is_active: Boolean// e.g., "available", "occupied", "reserved"
)

data class TblTable(
    val table_id: Long,
    val area_id: Long,
    val table_name: String,
    val seating_capacity: Int,
    val is_ac: String,  // e.g., "AC hall", "Non-AC"
    val table_status: String,
    val table_availability: String,
    val is_active: Boolean
)


data class TableStatusResponse(
    var table_id: Long,
    var area_id: Long,
    var area_name: String,
    var table_name: String,
    var seating_capacity: Long,
    var is_ac: String,
    var table_status: String,
    var table_availability: String,
    var is_active: Boolean,
    var grandTotal: Double,
    var staff_name:String,
    var order_time:String
)