package com.warriortech.resb.model

data class KitchenKOT(
    val kotNumber: Long,
    val tableNumber: String,
    val orderType: String, // DINE_IN, TAKEAWAY, DELIVERY
    val waiterName: String?,
    val orderTime: String,
    val status: KOTStatus,
    val items: List<KitchenKOTItem>,
    val orderId: String?
)

data class KitchenKOTItem(
    val itemName: String,
    val quantity: Int,
    val category: String,
    val addOns: List<String> = emptyList()
)

enum class KOTStatus {
    PENDING,
    IN_PROGRESS,
    COMPLETED,
    CANCELLED
}

data class KOTStatusUpdate(
    val kotId: Int,
    val status: KOTStatus
)

data class KitchenKOTResponse(
    val success: Boolean,
    val message: String,
    val data: List<KitchenKOT>?
)

data class KOTUpdateResponse(
    val success: Boolean,
    val message: String,
    val kotId: Int?
)

data class KitchenCategory(
    val kitchen_cat_id: Long,
    val kitchen_cat_name: String,
    val is_active: Long
)

data class KotResponse(
    val area_name: String,
    val table_name: String,
    val kot_number: Long,
    val order_master_id: String,
    val grand_total: Double,
    val order_status: String,
    val order_type: String,
    val staff_name: String,
    val order_date: String,
    val order_create_time: String
)