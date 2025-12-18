package com.warriortech.resb.model

data class OrderItem(
    val id: Long? = null,
    val menuItem: TblMenuItemResponse,
    val quantity: Int,
    val notes: String? = null,
    val orderDetailsId: Long? = null,
    val kotNumber: Int? = null
)

/**
 * Order model that matches the server response format
 * Updated to use Int for ids and Long for timestamp to match backend
 */

data class Order(
    val id: Long? = null,
    val tableId: Int,
    val items: List<OrderItem>,
    val totalAmount: Double,
    val status: String,
    val createdAt: Long? = null,
    val isPrinted: Boolean = false
)

enum class OrderStatus {
    PENDING,
    PREPARING,
    READY,
    DELIVERED,
    COMPLETED,
    CANCELLED ,
    RUNNING,
    HOLD
}

/**
 * PrintResponse model for KOT print response
 */


data class OrderMaster(
    var order_master_id: String,
    var order_date: String,
    var order_create_time: String,
    var order_completed_time: String,
    var staff_id: Long,
    var is_dine_in: Boolean,
    var is_take_away: Boolean,
    var is_delivery: Boolean,
    var table_id: Long,
    var no_of_person: Int,
    var waiter_request_status: Boolean,
    var kitchen_response_status: Boolean,
    var order_status: String,
    var is_delivered: Boolean,
    var is_merge: Boolean,
    var is_active: Long
)

data class OrderDetails(
    var order_details_id: Long,
    var order_master_id: String,
    var kot_number: Int?,
    var menu_item_id: Long,
    var rate: Double,
    var actual_rate:Double,
    var qty: Int,
    var total:Double,
    var tax_id:Long,
    var tax_name:String,
    var tax_amount:Double,
    var sgst_per:Double,
    var sgst:Double,
    var cgst_per:Double,
    var cgst:Double,
    var igst_per:Double,
    var igst:Double,
    var cess_per:Double,
    var cess:Double,
    var cess_specific:Double,
    var grand_total:Double,
    var prepare_status:Boolean,
    var item_add_mode:Boolean,
    var is_flag:Boolean,
    var merge_order_nos:String,
    var merge_order_tables:String,
    var merge_pax:Int,
    var is_active:Long
)

data class TblOrderResponse(
    var order_master_id: String,
    var order_date: String,
    var order_create_time:String,
    var order_completed_time:String,
    var staff_id:Long,
    var staff_name:String,
    var is_dine_in:Boolean,
    var is_take_away:Boolean,
    var is_delivery:Boolean,
    var table_id:Long,
    var table_name:String,
    var area_id:Long,
    var area_name:String,
    var no_of_person:Int,
    var waiter_request_status:Boolean,
    var kitchen_response_status:Boolean,
    var order_status:String,
    var is_delivered:Boolean,
    var is_merge:Boolean,
    var is_active:Long,
    var kot_number:Int?=null
)

data class TblOrderDetailsResponse(
    var order_details_id:Long,
    var order_master_id: String,
    var kot_number:Int,
    var menuItem: TblMenuItemResponse,
    var rate: Double,
    var actual_rate:Double,
    var qty:Int,
    var total:Double,
    var tax_id:Long,
    var tax_name:String,
    var tax_amount:Double,
    var sgst_per:Double,
    var sgst:Double,
    var cgst_per:Double,
    var cgst:Double,
    var igst_per:Double,
    var igst:Double,
    var cess_per:Double,
    var cess:Double,
    var cess_specific:Double,
    var grand_total:Double,
    var prepare_status:Boolean,
    var item_add_mode:Boolean,
    var is_flag:Boolean,
    var merge_order_nos:String,
    var merge_order_tables:String,
    var merge_pax:Int,
    var is_active:Long
)