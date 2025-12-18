package com.warriortech.resb.model

data class Modifiers(
    val add_on_id: Long = 0,
    val item_cat_id: Long,
    val add_on_name: String,
    val add_on_price: Double,
    val is_active: Boolean = true // Specific items this modifier applies to
)

data class SaleModifiers(
    val sale_add_on_id: Long = 0,
    val order_master_id: Long,
    val item_add_on_id: Long,
    val menu_item_id: Long,
    val status: Boolean,
    val is_active: Boolean = true // Specific items this modifier applies to
)


enum class ModifierType {
    ADDITION,    // Add something (extra cheese)
    REMOVAL,     // Remove something (no onions)
    SUBSTITUTION // Replace something (almond milk instead of regular)
}

data class OrderItemModifier(
    val id: Long = 0,
    val order_detail_id: Long,
    val modifier_id: Long,
    val modifier_name: String,
    val price_adjustment: Double
)

data class MenuItemWithModifiers(
    val menuItem: MenuItem,
    val availableModifiers: List<Modifiers>
)

data class OrderItemWithModifiers(
    val orderItem: TblOrderDetailsResponse,
    val modifiers: List<OrderItemModifier>
)
