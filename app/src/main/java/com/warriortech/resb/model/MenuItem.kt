package com.warriortech.resb.model

data class MenuItem(
    val menu_item_id: Long,
    val menu_item_name: String,
    val menu_item_name_tamil: String,
    val item_cat_id: Long,
    val item_cat_name: String,
    val rate: Double,
    val ac_rate: Double,
    val parcel_rate: Double,
    val parcel_charge: Double,
    val tax_id: Long,
    val tax_name: String,
    val tax_percentage: String,
    val cess_per: String,
    val cess_specific: Double,
    val kitchen_cat_id: Long,
    val kitchen_cat_name: String,
    val stock_maintain: String,
    val rate_lock: String,
    val unit_id: Long,
    val unit_name: String,
    val min_stock: Long,
    val hsn_code: String,
    val order_by: Long,
    val is_inventory: Long,
    val is_raw: String,
    val is_available: String,
    val is_favourite: Boolean,
    val image: String = "",
    var qty: Int = 0
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as MenuItem

        if (menu_item_id != other.menu_item_id) return false
        if (menu_item_name != other.menu_item_name) return false
        if (menu_item_name_tamil != other.menu_item_name_tamil) return false
        if (item_cat_id != other.item_cat_id) return false
        if (item_cat_name != other.item_cat_name) return false
        if (rate != other.rate) return false
        if (ac_rate != other.ac_rate) return false
        if (parcel_rate != other.parcel_rate) return false
        if (parcel_charge != other.parcel_charge) return false
        if (tax_id != other.tax_id) return false
        if (tax_name != other.tax_name) return false
        if (tax_percentage != other.tax_percentage) return false
        if (cess_per != other.cess_per) return false
        if (cess_specific != other.cess_specific) return false
        if (kitchen_cat_id != other.kitchen_cat_id) return false
        if (kitchen_cat_name != other.kitchen_cat_name) return false
        if (stock_maintain != other.stock_maintain) return false
        if (rate_lock != other.rate_lock) return false
        if (unit_id != other.unit_id) return false
        if (unit_name != other.unit_name) return false
        if (min_stock != other.min_stock) return false
        if (hsn_code != other.hsn_code) return false
        if (order_by != other.order_by) return false
        if (is_inventory != other.is_inventory) return false
        if (is_raw != other.is_raw) return false
        if (is_available != other.is_available) return false
        if (is_favourite != other.is_favourite) return false
        if (image != other.image) return false
        // qty is not part of default equals/hashCode for data class primary constructor

        return true
    }

    override fun hashCode(): Int {
        var result = menu_item_id.hashCode()
        // For every String property, defensively use ?.hashCode() ?: 0
        // This assumes any of them could be null due to Java interop issues
        result = 31 * result + (menu_item_name?.hashCode() ?: 0)
        result = 31 * result + (menu_item_name_tamil?.hashCode() ?: 0)
        result = 31 * result + item_cat_id.hashCode()
        result = 31 * result + (item_cat_name?.hashCode() ?: 0)
        result = 31 * result + rate.hashCode()
        result = 31 * result + ac_rate.hashCode()
        result = 31 * result + parcel_rate.hashCode()
        result = 31 * result + parcel_charge.hashCode()
        result = 31 * result + tax_id.hashCode()
        result = 31 * result + (tax_name?.hashCode() ?: 0)
        result = 31 * result + (tax_percentage?.hashCode() ?: 0)
        result = 31 * result + (cess_per?.hashCode() ?: 0)
        result = 31 * result + cess_specific.hashCode()
        result = 31 * result + kitchen_cat_id.hashCode()
        result = 31 * result + (kitchen_cat_name?.hashCode() ?: 0)
        result = 31 * result + (stock_maintain?.hashCode() ?: 0)
        result = 31 * result + (rate_lock?.hashCode() ?: 0)
        result = 31 * result + unit_id.hashCode()
        result = 31 * result + (unit_name?.hashCode() ?: 0)
        result = 31 * result + min_stock.hashCode()
        result = 31 * result + (hsn_code?.hashCode() ?: 0)
        result = 31 * result + order_by.hashCode()
        result = 31 * result + is_inventory.hashCode()
        result = 31 * result + (is_raw?.hashCode() ?: 0)
        result = 31 * result + (is_available?.hashCode() ?: 0)
        result = 31 * result + is_favourite.hashCode()
        result = 31 * result + (image?.hashCode() ?: 0)
        // qty is not part of default equals/hashCode for data class primary constructor
        return result
    }
}


/**
 * MenuCategory model for grouping menu items
 * Updated to use Int for id to match backend
 */
data class MenuCategory(
    val item_cat_id: Long,
    val item_cat_name: String,
    val order_by: String,
    val is_active: Boolean
)

data class TblMenuItemRequest(
    var menu_item_id: Long,
    var menu_item_code: String,
    var menu_item_name: String,
    var menu_item_name_tamil: String,
    var menu_id: Long,
    var item_cat_id: Long,
    var image: String,
    var rate: Double,
    var ac_rate: Double,
    var parcel_rate: Double,
    var parcel_charge: Double,
    var tax_id: Long,
    var kitchen_cat_id: Long,
    var is_available: String,
    var is_favourite: Boolean,
    var stock_maintain: String,
    var preparation_time: Long,
    var rate_lock: String,
    var unit_id: Long,
    var min_stock: Long,
    var hsn_code: String,
    var order_by: Long,
    var is_inventory: Long,
    var is_raw: String,
    var cess_specific: Double,
    var is_active: Long
)

data class TblMenuItemResponse(
    var menu_item_id: Long,
    var menu_item_code: String,
    var menu_item_name: String,
    var menu_item_name_tamil: String,
    var menu_id: Long,
    var menu_name: String,
    var item_cat_id: Long,
    var item_cat_name: String,
    var image: String,
    var rate: Double,
    var ac_rate: Double,
    var parcel_rate: Double,
    var parcel_charge: Double,
    var tax_id: Long,
    var tax_name: String,
    var tax_percentage: String,
    var kitchen_cat_id: Long,
    var kitchen_cat_name: String,
    var is_available: String,
    var preparation_time: Long,
    var is_favourite: Boolean,
    var stock_maintain: String,
    var rate_lock: String,
    var unit_id: Long,
    var unit_name: String,
    var min_stock: Long,
    var hsn_code: String,
    var order_by: Long,
    var is_inventory: Long,
    var is_raw: String,
    val cess_per: String,
    val cess_specific: Double,
    var is_active: Long,
    var qty: Int = 0,
    var actual_rate: Double = 0.0
)

data class TblUnit(
    var unit_id: Long,
    var unit_name: String,
    var is_active: Long
)