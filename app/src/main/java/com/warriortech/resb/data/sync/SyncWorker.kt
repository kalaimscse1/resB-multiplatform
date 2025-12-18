package com.warriortech.resb.data.sync

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.warriortech.resb.data.local.RestaurantDatabase
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblArea
import com.warriortech.resb.data.local.entity.TblCustomers
import com.warriortech.resb.data.local.entity.TblMenu
import com.warriortech.resb.data.local.entity.TblMenuItem
import com.warriortech.resb.data.local.entity.TblTableEntity
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.Menu
import com.warriortech.resb.model.Table
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.model.TblMenuItemRequest
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblTable
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import dagger.assisted.AssistedInject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import timber.log.Timber

@HiltWorker
class SyncWorker @AssistedInject constructor(
    appContext: Context,
    workerParams: WorkerParameters,
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) : CoroutineWorker(appContext, workerParams) {

    private val database: RestaurantDatabase = RestaurantDatabase.getDatabase(appContext)

    // Initialize all DAOs for comprehensive sync
    private val tableDao = database.tableDao()
    private val menuItemDao = database.menuItemDao()
    private val areaDao = database.tblAreaDao()
    private val customerDao = database.tblCustomerDao()
    private val menuDao = database.tblMenuDao()
    private val counterDao = database.tblCounterDao()
    private val staffDao = database.tblStaffDao()
    private val printerDao = database.tblPrinterDao()
    private val taxDao = database.tblTaxDao()
    private val taxSplitDao = database.tblTaxSplitDao()
    private val voucherDao = database.tblVoucherDao()
    private val voucherTypeDao = database.tblVoucherTypeDao()
    private val unitDao = database.tblUnitDao()
    private val roleDao = database.tblRoleDao()
    private val kitchenCategoryDao = database.tblKitchenCategoryDao()
    private val itemCategoryDao = database.tblItemCategoryDao()
    private val modifierDao = database.tblAddOnDao()
    private val generalSettingsDao = database.tblGeneralSettingsDao()

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        try {
            Timber.d("Starting comprehensive synchronization...")
            val companyCode = sessionManager.getCompanyCode() ?: ""

            if (companyCode.isEmpty()) {
                Timber.w("No company code available, skipping sync")
                return@withContext Result.failure()
            }

            // Phase 1: Sync local changes to server (Priority entities first)
            syncPendingTablesToServer()
            syncPendingMenuItemsToServer()
            syncPendingAreasToServer()
            syncPendingCustomersToServer()
            syncPendingMenusToServer()
            syncPendingCountersToServer()
            syncPendingStaffToServer()
            syncPendingPrintersToServer()
            syncPendingTaxesToServer()
            syncPendingTaxSplitsToServer()
            syncPendingVouchersToServer()
            syncPendingVoucherTypesToServer()
            syncPendingUnitsToServer()
            syncPendingRolesToServer()
            syncPendingKitchenCategoriesToServer()
            syncPendingItemCategoriesToServer()
            syncPendingModifiersToServer()
            syncPendingGeneralSettingsToServer()

            // Phase 2: Sync server changes to local (Master data first)
            syncAreasFromServer()
            syncRolesFromServer()
            syncUnitsFromServer()
            syncTaxesFromServer()
            syncTaxSplitsFromServer()
            syncKitchenCategoriesFromServer()
            syncItemCategoriesFromServer()
            syncVoucherTypesFromServer()
            syncCountersFromServer()
            syncStaffFromServer()
            syncPrintersFromServer()
            syncCustomersFromServer()
            syncMenusFromServer()
            syncTablesFromServer()
            syncMenuItemsFromServer()
            syncVouchersFromServer()
            syncModifiersFromServer()
            syncGeneralSettingsFromServer()

            Timber.d("Comprehensive synchronization completed successfully")
            Result.success()
        } catch (e: Exception) {
            Timber.e(e, "Error during comprehensive synchronization")
            Result.retry()
        }
    }

    private suspend fun syncPendingTablesToServer() {
        try {
            val pendingTables = tableDao.getTablesBySyncStatus(SyncStatus.PENDING_SYNC)
            Timber.d("Found ${pendingTables.size} pending tables to sync")

            for (table in pendingTables) {
                try {
                    val tableRequest = table.toApiModel()
                    val response =
                        apiService.createTable(tableRequest, sessionManager.getCompanyCode() ?: "")

                    if (response.isSuccessful) {
                        val updatedTable = table.copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        tableDao.updateTable(updatedTable)
                        Timber.d("Table synced to server: ${table.table_name}")
                    } else {
                        Timber.w("Failed to sync table ${table.table_name}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing table ${table.table_name} to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncPendingTablesToServer")
        }
    }

    private suspend fun syncPendingMenuItemsToServer() {
        try {
            val pendingMenuItems = menuItemDao.getMenuItemsBySyncStatus(SyncStatus.PENDING_SYNC)
            Timber.d("Found ${pendingMenuItems.size} pending menu items to sync")

            for (menuItem in pendingMenuItems) {
                try {
                    val menuItemRequest = menuItem.toApiModel()
                    val response = apiService.createMenuItem(
                        menuItemRequest,
                        sessionManager.getCompanyCode() ?: ""
                    )

                    if (response.isSuccessful) {
                        val updatedMenuItem = menuItem.copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        menuItemDao.updateMenuItem(updatedMenuItem)
                        Timber.d("Menu item synced to server: ${menuItem.menu_item_name}")
                    } else {
                        Timber.w("Failed to sync menu item ${menuItem.menu_item_name}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing menu item ${menuItem.menu_item_name} to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncPendingMenuItemsToServer")
        }
    }

    private suspend fun syncTablesFromServer() {
        try {
            val response = apiService.getAllTables(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                val remoteTables = response.body() ?: emptyList()
                Timber.d("Fetched ${remoteTables.size} tables from server")

                val localTables = remoteTables.map { remote ->
                    // Check if table exists locally
                    val existingTable = tableDao.getTableById(remote.table_id.toLong())

                    if (existingTable != null && existingTable.is_synced == SyncStatus.PENDING_SYNC) {
                        // Keep local version if it has pending changes
                        existingTable
                    } else {
                        // Use server version
                        remote.toEntity().copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                }

                tableDao.insertTables(localTables)
                Timber.d("Updated local tables from server")
            } else {
                Timber.w("Failed to fetch tables from server: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncTablesFromServer")
        }
    }

    private suspend fun syncMenuItemsFromServer() {
        try {
            val response = apiService.getAllMenuItems(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                val remoteMenuItems = response.body() ?: emptyList()
                Timber.d("Fetched ${remoteMenuItems.size} menu items from server")

                val localMenuItems = remoteMenuItems.map { remote ->
                    // Check if menu item exists locally
                    val existingMenuItem = menuItemDao.getMenuItemById(remote.menu_item_id)

                    if (existingMenuItem != null && existingMenuItem.is_synced == SyncStatus.PENDING_SYNC) {
                        // Keep local version if it has pending changes
                        existingMenuItem
                    } else {
                        // Use server version
                        remote.toEntity().copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                }

                menuItemDao.insertMenuItems(localMenuItems)
                Timber.d("Updated local menu items from server")
            } else {
                Timber.w("Failed to fetch menu items from server: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncMenuItemsFromServer")
        }
    }

    /** Convert remote MenuItemResponse to local entity */
    private fun TblMenuItemResponse.toEntity() = TblMenuItem(
        menu_item_id = menu_item_id.toInt(),
        menu_item_code = menu_item_code,
        menu_item_name = menu_item_name,
        menu_item_name_tamil = menu_item_name_tamil,
        menu_id = menu_id.toInt(),
        rate = rate,
        image = image,
        ac_rate = ac_rate,
        parcel_rate = parcel_rate,
        is_available = is_available,
        item_cat_id = item_cat_id.toInt(),
        parcel_charge = parcel_charge,
        tax_id = tax_id.toInt(),
        kitchen_cat_id = kitchen_cat_id.toInt(),
        stock_maintain = stock_maintain,
        rate_lock = rate_lock,
        unit_id = unit_id.toInt(),
        min_stock = min_stock.toInt(),
        hsn_code = hsn_code,
        order_by = order_by.toInt(),
        is_inventory = is_inventory.toInt(),
        is_raw = is_raw,
        cess_specific = cess_specific,
        is_favourite = is_favourite,
        is_active = is_active == 1L,
        preparation_time = preparation_time.toInt(),
        is_synced = SyncStatus.SYNCED,
        last_synced_at = System.currentTimeMillis()
    )

    /** Convert local Table entity to API model */
    private fun TblTableEntity.toApiModel() = TblTable(
        table_id = table_id.toLong(),
        area_id = area_id?.toLong() ?: 0L,
        table_name = table_name.toString(),
        seating_capacity = seating_capacity?.toInt() ?: 0,
        is_ac = is_ac.toString(),
        table_status = table_status.toString(),
        table_availability = table_availability.toString(),
        is_active = is_active == true
    )

    /** Convert remote Table model to local entity */
    private fun Table.toEntity() = TblTableEntity(
        table_id = table_id.toInt(),
        area_id = area_id.toInt(),
        table_name = table_name,
        seating_capacity = seating_capacity,
        is_ac = is_ac,
        table_status = table_status,
        table_availability = table_availability,
        is_active = is_active,
        is_synced = SyncStatus.SYNCED,
        last_synced_at = System.currentTimeMillis()
    )

    /** Convert local MenuItem entity to API model */
    private fun TblMenuItem.toApiModel() = TblMenuItemRequest(
        menu_item_id = menu_item_id.toLong(),
        menu_item_code = menu_item_code.toString(),
        menu_item_name = menu_item_name.toString(),
        menu_item_name_tamil = menu_item_name_tamil.toString(),
        menu_id = menu_id?.toLong() ?: 0L,
        rate = rate?.toDouble() ?: 0.0,
        image = image.toString(),
        ac_rate = ac_rate?.toDouble() ?: 0.0,
        parcel_rate = parcel_rate?.toDouble() ?: 0.0,
        is_available = is_available.toString(),
        item_cat_id = item_cat_id?.toLong() ?: 0L,
        parcel_charge = parcel_charge?.toDouble() ?: 0.0,
        tax_id = tax_id?.toLong() ?: 0L,
        kitchen_cat_id = kitchen_cat_id?.toLong() ?: 0L,
        stock_maintain = stock_maintain.toString(),
        rate_lock = rate_lock.toString(),
        unit_id = unit_id?.toLong() ?: 0L,
        min_stock = min_stock?.toLong() ?: 0L,
        hsn_code = hsn_code.toString(),
        order_by = order_by?.toLong() ?: 0L,
        is_inventory = is_inventory?.toLong() ?: 0L,
        is_raw = is_raw.toString(),
        cess_specific = cess_specific?.toDouble() ?: 0.0,
        is_favourite = is_favourite == true,
        is_active = if (is_active == true) 1L else 0L,
        preparation_time = preparation_time?.toLong() ?: 0L
    )

    // ==================== AREA SYNCHRONIZATION ====================

    private suspend fun syncPendingAreasToServer() {
        try {
            val pendingAreas = areaDao.getUnsynced()
            Timber.d("Found ${pendingAreas.size} pending areas to sync")

            for (area in pendingAreas) {
                try {
                    val areaRequest = Area(
                        area_id = area.area_id.toLong(),
                        area_name = area.area_name ?: "",
                        isActvice = area.is_active ?: true,
                    )

                    val response = if (area.is_synced == SyncStatus.PENDING_UPDATE) {
                        apiService.updateArea(
                            area.area_id.toLong(),
                            areaRequest,
                            sessionManager.getCompanyCode() ?: ""
                        )
                    } else {
                        apiService.createArea(areaRequest, sessionManager.getCompanyCode() ?: "")
                    }

                    if (response.isSuccessful) {
                        val updatedArea = area.copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        areaDao.update(updatedArea)
                        Timber.d("Area synced to server: ${area.area_name}")
                    } else {
                        Timber.w("Failed to sync area ${area.area_name}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing area ${area.area_name} to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncPendingAreasToServer")
        }
    }

    private suspend fun syncAreasFromServer() {
        try {
            val response = apiService.getAllAreas(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                val remoteAreas = response.body() ?: emptyList()
                Timber.d("Fetched ${remoteAreas.size} areas from server")

                val localAreas = remoteAreas.map { remote ->
                    val existingArea = areaDao.getById(remote.area_id.toInt())

                    if (existingArea != null && existingArea.is_synced == SyncStatus.PENDING_SYNC) {
                        existingArea
                    } else {
                        TblArea(
                            area_id = remote.area_id.toInt(),
                            area_name = remote.area_name,
                            is_active = remote.isActvice,
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                }

                areaDao.insertAll(localAreas)
                Timber.d("Updated local areas from server")
            } else {
                Timber.w("Failed to fetch areas from server: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncAreasFromServer")
        }
    }

    // ==================== CUSTOMER SYNCHRONIZATION ====================

    private suspend fun syncPendingCustomersToServer() {
        try {
            val pendingCustomers = customerDao.getUnsynced()
            Timber.d("Found ${pendingCustomers.size} pending customers to sync")

            for (customer in pendingCustomers) {
                try {
                    val customerRequest = TblCustomer(
                        customer_id = customer.customer_id.toLong(),
                        customer_name = customer.customer_name ?: "",
                        address = customer.address ?: "",
                        contact_no = customer.contact_no ?: "",
                        email_address = customer.email_address ?: "",
                        is_active = if (customer.is_active == true) 1L else 0L,
                        gst_no = customer.gst_no ?: "",
                        igst_status = customer.igst_status ?: false,
                    )

                    val response = if (customer.is_synced == SyncStatus.PENDING_UPDATE) {
                        apiService.updateCustomer(
                            customer.customer_id.toLong(),
                            customerRequest,
                            sessionManager.getCompanyCode() ?: ""
                        )
                    } else {
                        apiService.createCustomer(
                            customerRequest,
                            sessionManager.getCompanyCode() ?: ""
                        )
                    }

                    if (response.isSuccessful) {
                        val updatedCustomer = customer.copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        customerDao.update(updatedCustomer)
                        Timber.d("Customer synced to server: ${customer.customer_name}")
                    } else {
                        Timber.w("Failed to sync customer ${customer.customer_name}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing customer ${customer.customer_name} to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncPendingCustomersToServer")
        }
    }

    private suspend fun syncCustomersFromServer() {
        try {
            val response = apiService.getAllCustomers(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                val remoteCustomers = response.body() ?: emptyList()
                Timber.d("Fetched ${remoteCustomers.size} customers from server")

                val localCustomers = remoteCustomers.map { remote ->
                    val existingCustomer = customerDao.getById(remote.customer_id.toInt())

                    if (existingCustomer != null && existingCustomer.is_synced == SyncStatus.PENDING_SYNC) {
                        existingCustomer
                    } else {
                        TblCustomers(
                            customer_id = remote.customer_id.toInt(),
                            customer_name = remote.customer_name,
                            address = remote.address,
                            contact_no = remote.contact_no,
                            email_address = remote.email_address,
                            is_active = if (remote.is_active == 1L) true else false,
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis(),
                            gst_no = remote.gst_no,
                            igst_status = remote.igst_status
                        )
                    }
                }

                customerDao.insertAll(localCustomers)
                Timber.d("Updated local customers from server")
            } else {
                Timber.w("Failed to fetch customers from server: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncCustomersFromServer")
        }
    }

    // ==================== MENU SYNCHRONIZATION ====================

    private suspend fun syncPendingMenusToServer() {
        try {
            val pendingMenus = menuDao.getUnsynced()
            Timber.d("Found ${pendingMenus.size} pending menus to sync")

            for (menu in pendingMenus) {
                try {
                    val menuRequest = Menu(
                        menu_id = menu.menu_id.toLong(),
                        menu_name = menu.menu_name ?: "",
                        is_active = menu.is_active ?: true,
                        order_by = (menu.order_by ?: 0).toString(),
                        start_time = menu.start_time?.toFloat() ?: 0.0f,
                        end_time = menu.end_time?.toFloat() ?: 0.0f,
                    )

                    val response = if (menu.is_synced == SyncStatus.PENDING_UPDATE) {
                        apiService.updateMenu(
                            menu.menu_id.toLong(),
                            menuRequest,
                            sessionManager.getCompanyCode() ?: ""
                        )
                    } else {
                        apiService.createMenu(menuRequest, sessionManager.getCompanyCode() ?: "")
                    }

                    if (response.isSuccessful) {
                        val updatedMenu = menu.copy(
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                        menuDao.update(updatedMenu)
                        Timber.d("Menu synced to server: ${menu.menu_name}")
                    } else {
                        Timber.w("Failed to sync menu ${menu.menu_name}: ${response.code()}")
                    }
                } catch (e: Exception) {
                    Timber.e(e, "Error syncing menu ${menu.menu_name} to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncPendingMenusToServer")
        }
    }

    private suspend fun syncMenusFromServer() {
        try {
            val response = apiService.getAllMenus(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                val remoteMenus = response.body() ?: emptyList()
                Timber.d("Fetched ${remoteMenus.size} menus from server")

                val localMenus = remoteMenus.map { remote ->
                    val existingMenu = menuDao.getById(remote.menu_id.toInt())

                    if (existingMenu != null && existingMenu.is_synced == SyncStatus.PENDING_SYNC) {
                        existingMenu
                    } else {
                        TblMenu(
                            menu_id = remote.menu_id.toInt(),
                            menu_name = remote.menu_name,
                            is_active = remote.is_active,
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis(),
                            order_by = remote.order_by.toInt(),
                            start_time = remote.start_time.toDouble(),
                            end_time = remote.end_time.toDouble()
                        )
                    }
                }

                menuDao.insertAll(localMenus)
                Timber.d("Updated local menus from server")
            } else {
                Timber.w("Failed to fetch menus from server: ${response.code()}")
            }
        } catch (e: Exception) {
            Timber.e(e, "Error in syncMenusFromServer")
        }
    }

    // ==================== PLACEHOLDER SYNC METHODS ====================
    // These are placeholder implementations for the remaining entities
    // They follow the same pattern and should be implemented based on specific entity requirements

    private suspend fun syncPendingCountersToServer() =
        handlePendingSync("Counters") { /* TODO: Implement counter sync */ }

    private suspend fun syncCountersFromServer() =
        handleServerSync("Counters") { /* TODO: Implement counter sync */ }

    private suspend fun syncPendingStaffToServer() =
        handlePendingSync("Staff") { /* TODO: Implement staff sync */ }

    private suspend fun syncStaffFromServer() =
        handleServerSync("Staff") { /* TODO: Implement staff sync */ }

    private suspend fun syncPendingPrintersToServer() =
        handlePendingSync("Printers") { /* TODO: Implement printer sync */ }

    private suspend fun syncPrintersFromServer() =
        handleServerSync("Printers") { /* TODO: Implement printer sync */ }

    private suspend fun syncPendingTaxesToServer() =
        handlePendingSync("Taxes") { /* TODO: Implement tax sync */ }

    private suspend fun syncTaxesFromServer() =
        handleServerSync("Taxes") { /* TODO: Implement tax sync */ }

    private suspend fun syncPendingTaxSplitsToServer() =
        handlePendingSync("TaxSplits") { /* TODO: Implement taxsplit sync */ }

    private suspend fun syncTaxSplitsFromServer() =
        handleServerSync("TaxSplits") { /* TODO: Implement taxsplit sync */ }

    private suspend fun syncPendingVouchersToServer() =
        handlePendingSync("Vouchers") { /* TODO: Implement voucher sync */ }

    private suspend fun syncVouchersFromServer() =
        handleServerSync("Vouchers") { /* TODO: Implement voucher sync */ }

    private suspend fun syncPendingVoucherTypesToServer() =
        handlePendingSync("VoucherTypes") { /* TODO: Implement vouchertype sync */ }

    private suspend fun syncVoucherTypesFromServer() =
        handleServerSync("VoucherTypes") { /* TODO: Implement vouchertype sync */ }

    private suspend fun syncPendingUnitsToServer() =
        handlePendingSync("Units") { /* TODO: Implement unit sync */ }

    private suspend fun syncUnitsFromServer() =
        handleServerSync("Units") { /* TODO: Implement unit sync */ }

    private suspend fun syncPendingRolesToServer() =
        handlePendingSync("Roles") { /* TODO: Implement role sync */ }

    private suspend fun syncRolesFromServer() =
        handleServerSync("Roles") { /* TODO: Implement role sync */ }

    private suspend fun syncPendingKitchenCategoriesToServer() =
        handlePendingSync("KitchenCategories") { /* TODO: Implement kitchen category sync */ }

    private suspend fun syncKitchenCategoriesFromServer() =
        handleServerSync("KitchenCategories") { /* TODO: Implement kitchen category sync */ }

    private suspend fun syncPendingItemCategoriesToServer() =
        handlePendingSync("ItemCategories") { /* TODO: Implement item category sync */ }

    private suspend fun syncItemCategoriesFromServer() =
        handleServerSync("ItemCategories") { /* TODO: Implement item category sync */ }

    private suspend fun syncPendingModifiersToServer() =
        handlePendingSync("AddOn") { /* TODO: Implement modifier sync */ }

    private suspend fun syncModifiersFromServer() =
        handleServerSync("AddOn") { /* TODO: Implement modifier sync */ }

    private suspend fun syncPendingGeneralSettingsToServer() =
        handlePendingSync("GeneralSettings") { /* TODO: Implement general settings sync */ }

    private suspend fun syncGeneralSettingsFromServer() =
        handleServerSync("GeneralSettings") { /* TODO: Implement general settings sync */ }

    // Helper methods for placeholder implementations
    private suspend fun handlePendingSync(entityName: String, syncAction: suspend () -> Unit) {
        try {
            Timber.d("Syncing pending $entityName to server...")
            syncAction()
            Timber.d("Completed syncing pending $entityName to server")
        } catch (e: Exception) {
            Timber.e(e, "Error syncing pending $entityName to server")
        }
    }

    private suspend fun handleServerSync(entityName: String, syncAction: suspend () -> Unit) {
        try {
            Timber.d("Syncing $entityName from server...")
            syncAction()
            Timber.d("Completed syncing $entityName from server")
        } catch (e: Exception) {
            Timber.e(e, "Error syncing $entityName from server")
        }
    }

    companion object {
        const val WORK_NAME = "SyncWorker"
    }
}