package com.warriortech.resb.data.local.dao

//import androidx.room.*
//import kotlinx.coroutines.flow.Flow
//import com.warriortech.resb.data.local.entity.*
//
//data class TblItemCategoryWithChildren(
//    @Embedded val parent: TblItemCategory,
//    @Relation(parentColumn = "item_cat_id", entityColumn = "item_cat_id")
//    val tbl_add_on_list: List<TblAddOn>,
//    @Relation(parentColumn = "item_cat_id", entityColumn = "item_cat_id")
//    val tbl_billing_list: List<TblBilling>,
//)
//
//@Dao
//interface TblItemCategoryRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_item_category")
//    fun getAllWithChildren(): Flow<List<TblItemCategoryWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_item_category WHERE item_cat_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblItemCategoryWithChildren?
//}
//
//data class TblCustomerWithChildren(
//    @Embedded val parent: TblCustomer,
//    @Relation(parentColumn = "customer_id", entityColumn = "customer_id")
//    val tbl_add_on_list: List<TblAddOn>,
//)
//
//@Dao
//interface TblCustomerRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_customer")
//    fun getAllWithChildren(): Flow<List<TblCustomerWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_customer WHERE customer_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblCustomerWithChildren?
//}
//
//data class TblOrderMasterWithChildren(
//    @Embedded val parent: TblOrderMaster,
//    @Relation(parentColumn = "order_master_id", entityColumn = "order_master_id")
//    val tbl_billing_list: List<TblBilling>,
//    @Relation(parentColumn = "order_master_id", entityColumn = "order_master_id")
//    val tbl_order_details_list: List<TblOrderDetails>,
//)
//
//@Dao
//interface TblOrderMasterRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_order_master")
//    fun getAllWithChildren(): Flow<List<TblOrderMasterWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_order_master WHERE order_master_id = :id")
//    suspend fun getByIdWithChildren(id: String): TblOrderMasterWithChildren?
//}
//
//data class TblStaffWithChildren(
//    @Embedded val parent: TblStaff,
//    @Relation(parentColumn = "staff_id", entityColumn = "staff_id")
//    val tbl_billing_list: List<TblBilling>,
//    @Relation(parentColumn = "staff_id", entityColumn = "staff_id")
//    val tbl_order_details_list: List<TblOrderDetails>,
//)
//
//@Dao
//interface TblStaffRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_staff")
//    fun getAllWithChildren(): Flow<List<TblStaffWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_staff WHERE staff_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblStaffWithChildren?
//}
//
//data class TblVoucherWithChildren(
//    @Embedded val parent: TblVoucher,
//    @Relation(parentColumn = "voucher_id", entityColumn = "voucher_id")
//    val tbl_billing_list: List<TblBilling>,
//)
//
//@Dao
//interface TblVoucherRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_voucher")
//    fun getAllWithChildren(): Flow<List<TblVoucherWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_voucher WHERE voucher_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblVoucherWithChildren?
//}
//
//data class TblKitchenCategoryWithChildren(
//    @Embedded val parent: TblKitchenCategory,
//    @Relation(parentColumn = "kitchen_cat_id", entityColumn = "kitchen_cat_id")
//    val tbl_menu_item_list: List<TblMenuItem>,
//    @Relation(parentColumn = "kitchen_cat_id", entityColumn = "kitchen_cat_id")
//    val tbl_order_master_list: List<TblOrderMaster>,
//)
//
//@Dao
//interface TblKitchenCategoryRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_kitchen_category")
//    fun getAllWithChildren(): Flow<List<TblKitchenCategoryWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_kitchen_category WHERE kitchen_cat_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblKitchenCategoryWithChildren?
//}
//
//data class TblMenuWithChildren(
//    @Embedded val parent: TblMenu,
//    @Relation(parentColumn = "menu_id", entityColumn = "menu_id")
//    val tbl_menu_item_list: List<TblMenuItem>,
//)
//
//@Dao
//interface TblMenuRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_menu")
//    fun getAllWithChildren(): Flow<List<TblMenuWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_menu WHERE menu_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblMenuWithChildren?
//}
//
//data class TblTaxWithChildren(
//    @Embedded val parent: TblTax,
//    @Relation(parentColumn = "tax_id", entityColumn = "tax_id")
//    val tbl_menu_item_list: List<TblMenuItem>,
//    @Relation(parentColumn = "tax_id", entityColumn = "tax_id")
//    val tbl_table_list: List<TblTable>,
//)
//
//@Dao
//interface TblTaxRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_tax")
//    fun getAllWithChildren(): Flow<List<TblTaxWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_tax WHERE tax_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblTaxWithChildren?
//}
//
//data class TblUnitWithChildren(
//    @Embedded val parent: TblUnit,
//    @Relation(parentColumn = "unit_id", entityColumn = "unit_id")
//    val tbl_menu_item_list: List<TblMenuItem>,
//)
//
//@Dao
//interface TblUnitRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_unit")
//    fun getAllWithChildren(): Flow<List<TblUnitWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_unit WHERE unit_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblUnitWithChildren?
//}
//
//data class TblMenuItemWithChildren(
//    @Embedded val parent: TblMenuItem,
//    @Relation(parentColumn = "menu_item_id", entityColumn = "menu_item_id")
//    val tbl_menu_item_list: List<TblMenuItem>,
//)
//
//@Dao
//interface TblMenuItemRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_menu_item")
//    fun getAllWithChildren(): Flow<List<TblMenuItemWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_menu_item WHERE menu_item_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblMenuItemWithChildren?
//}
//
//data class TblTableWithChildren(
//    @Embedded val parent: TblTable,
//    @Relation(parentColumn = "table_id", entityColumn = "table_id")
//    val tbl_order_master_list: List<TblOrderMaster>,
//)
//
//@Dao
//interface TblTableRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_table")
//    fun getAllWithChildren(): Flow<List<TblTableWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_table WHERE table_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblTableWithChildren?
//}
//
//data class TblAddOnWithChildren(
//    @Embedded val parent: TblAddOn,
//    @Relation(parentColumn = "add_on_id", entityColumn = "item_add_on_id")
//    val tbl_printer_list: List<TblPrinter>,
//)
//
//@Dao
//interface TblAddOnRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_add_on")
//    fun getAllWithChildren(): Flow<List<TblAddOnWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_add_on WHERE add_on_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblAddOnWithChildren?
//}
//
//data class TblAreaWithChildren(
//    @Embedded val parent: TblArea,
//    @Relation(parentColumn = "area_id", entityColumn = "area_id")
//    val tbl_sale_add_on_list: List<TblSaleAddOn>,
//    @Relation(parentColumn = "area_id", entityColumn = "area_id")
//    val tbl_staff_list: List<TblStaff>,
//)
//
//@Dao
//interface TblAreaRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_area")
//    fun getAllWithChildren(): Flow<List<TblAreaWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_area WHERE area_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblAreaWithChildren?
//}
//
//data class TblCounterWithChildren(
//    @Embedded val parent: TblCounter,
//    @Relation(parentColumn = "counter_id", entityColumn = "counter_id")
//    val tbl_staff_list: List<TblStaff>,
//    @Relation(parentColumn = "counter_id", entityColumn = "counter_id")
//    val tbl_tax_split_list: List<TblTaxSplit>,
//)
//
//@Dao
//interface TblCounterRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_counter")
//    fun getAllWithChildren(): Flow<List<TblCounterWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_counter WHERE counter_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblCounterWithChildren?
//}
//
//data class TblRoleWithChildren(
//    @Embedded val parent: TblRole,
//    @Relation(parentColumn = "role_id", entityColumn = "role_id")
//    val tbl_staff_list: List<TblStaff>,
//)
//
//@Dao
//interface TblRoleRelationDao {
//    @Transaction
//    @Query("SELECT * FROM tbl_role")
//    fun getAllWithChildren(): Flow<List<TblRoleWithChildren>>
//
//    @Transaction
//    @Query("SELECT * FROM tbl_role WHERE role_id = :id")
//    suspend fun getByIdWithChildren(id: Int): TblRoleWithChildren?
//}
