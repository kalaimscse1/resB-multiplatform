package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.warriortech.resb.model.MenuItem
import com.warriortech.resb.model.TblMenuItemResponse

@Entity(
    tableName = "tbl_menu_item",
    foreignKeys = [
        ForeignKey(entity = TblKitchenCategory::class, parentColumns = ["kitchen_cat_id"], childColumns = ["kitchen_cat_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblMenu::class, parentColumns = ["menu_id"], childColumns = ["menu_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblTax::class, parentColumns = ["tax_id"], childColumns = ["tax_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblUnit::class, parentColumns = ["unit_id"], childColumns = ["unit_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblMenuItem::class, parentColumns = ["menu_item_id"], childColumns = ["menu_item_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["kitchen_cat_id"]),
        Index(value = ["menu_id"]),
        Index(value = ["tax_id"]),
        Index(value = ["unit_id"]),
        Index(value = ["menu_item_id"]),
    ]
)
data class TblMenuItem(
    @PrimaryKey(autoGenerate = true) val menu_item_id: Int = 0,
    val menu_item_code: String?,
    val menu_item_name: String?,
    val menu_item_name_tamil: String?,
    val menu_id: Int?,
    val item_cat_id: Int?,
    val image: String?,
    val rate: Double?,
    val ac_rate: Double?,
    val parcel_rate: Double?,
    val parcel_charge: Double?,
    val tax_id: Int?,
    val kitchen_cat_id: Int?,
    val is_available: String?,
    val is_favourite: Boolean?,
    val stock_maintain: String?,
    val preparation_time: Int?,
    val rate_lock: String?,
    val unit_id: Int?,
    val min_stock: Int?,
    val hsn_code: String?,
    val order_by: Int?,
    val is_inventory: Int?,
    val is_raw: String?,
    val cess_specific: Double?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = System.currentTimeMillis()
) {
    fun toModel(): TblMenuItemResponse {
       return TblMenuItemResponse(
            menu_item_id = this.menu_item_id.toLong(),
            menu_item_code = this.menu_item_code ?: "",
            menu_item_name = this.menu_item_name.toString(),
            menu_item_name_tamil = this.menu_item_name_tamil.toString(),
            menu_id = this.menu_id?.toLong() ?: 0L,
            menu_name = "",
            rate = this.rate!!,
            item_cat_id = this.item_cat_id?.toLong() ?: 0L,
            item_cat_name = "",
            image = this.image.toString(),
            ac_rate = this.ac_rate ?: 0.0,
            is_available = this.is_available ?: "YES",
            parcel_rate = this.parcel_rate ?: 0.0,
            parcel_charge = this.parcel_charge ?: 0.0,
            tax_id = this.tax_id?.toLong() ?: 0L,
            tax_name = "",
            tax_percentage = "",
            kitchen_cat_id = this.kitchen_cat_id?.toLong() ?: 0L,
            kitchen_cat_name = "",
            stock_maintain = this.stock_maintain ?: "NO",
            rate_lock = this.rate_lock ?: "NO",
            unit_id = this.unit_id?.toLong() ?: 0L,
            unit_name = "",
            min_stock = this.min_stock?.toLong() ?: 0L,
            hsn_code = this.hsn_code ?: "",
            order_by = this.order_by?.toLong() ?: 0L,
            is_inventory = this.is_inventory?.toLong() ?: 0L,
            is_raw = this.is_raw ?: "NO",
            cess_per = "",
            cess_specific = this.cess_specific!!,
            is_favourite = this.is_favourite!!,
            is_active = if (this.is_active == true) 1L else 0L,
            preparation_time = this.preparation_time?.toLong() ?: 0L
        )
    }
}