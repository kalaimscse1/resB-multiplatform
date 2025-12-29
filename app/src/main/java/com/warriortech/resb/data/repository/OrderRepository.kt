package com.warriortech.resb.data.repository

import android.annotation.SuppressLint
import com.warriortech.resb.data.local.dao.OrderDao
import com.warriortech.resb.data.local.dao.TableDao
import com.warriortech.resb.data.local.dao.TblOrderDetailsDao
import com.warriortech.resb.data.local.dao.TblOrderMasterDao
import com.warriortech.resb.data.local.dao.TblVoucherDao
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblOrderDetails
import com.warriortech.resb.data.local.entity.TblOrderMaster
import com.warriortech.resb.model.KOTRequest
import com.warriortech.resb.model.OrderDetails
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.OrderMaster
import com.warriortech.resb.model.OrderStatus
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.model.TblOrderResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.PrinterHelper
import com.warriortech.resb.util.generateNextBillNo
import com.warriortech.resb.util.getCurrentDateModern
import com.warriortech.resb.util.getCurrentTimeModern
import kotlinx.coroutines.flow.*
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.collections.map

/**
 * Repository for Order-related API operations
 * Updated to work with the Kotlin Mini App backend
 */
@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val orderDao: TblOrderMasterDao,
    private val orderDetailsDao: TblOrderDetailsDao,
    private val voucherDao: TblVoucherDao,
    private val tableDao: TableDao,
    private val sessionManager: SessionManager,
    private val printerHelper: PrinterHelper
) {
    /**
     * Create a new order
     * @param tableId The table ID
     * @param items List of order items
     */
    @SuppressLint("SuspiciousIndentation")
    suspend fun placeOrUpdateOrder(
        tableId: Long,
        itemsToPlace: List<OrderItem>,
        tableStatus: String,
        existingOpenOrderMasterId: String? = null // Allow passing it if already known
    ): Flow<Result<TblOrderResponse>> = flow {
        if (itemsToPlace.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("Cannot place an order with no items.")))
            return@flow
        }

        try {
            var currentOrderMasterId = existingOpenOrderMasterId
            var orderMasterResponse: TblOrderResponse? = null

            // 1. Check for/Determine existing open OrderMaster ID for the table
            if (currentOrderMasterId == null && tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY") {
                // Try to find an open order for this table
                // Assuming getOpenOrderMasterForTable returns a TblOrderResponse or similar with order_master_id
                // If it returns null, no open order exists.
                val openOrderResponse = apiService.getOpenOrderMasterForTable(
                    tableId,
                    sessionManager.getCompanyCode() ?: ""
                ) // YOU NEED TO IMPLEMENT/DEFINE THIS
                if (openOrderResponse.isSuccessful && openOrderResponse.body() != null) {
                    currentOrderMasterId = openOrderResponse.body()!!.order_master_id
                    orderMasterResponse =
                        openOrderResponse.body() // Store the existing order master response
                }
            }

            val tableInfo = apiService.getTablesByStatus(
                tableId,
                sessionManager.getCompanyCode() ?: ""
            ) // Assuming this gets details like seating_capacity, table_name

            // 2. If no existing open OrderMaster, create a new one
            if (currentOrderMasterId == null) {
                val newOrderMasterApiId = apiService.getOrderNo(
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                ) // Get new Order Master ID from API
                val orderRequest = OrderMaster(
                    order_date = getCurrentDateModern(),
                    order_create_time = getCurrentTimeModern(),
                    order_completed_time = "", // Will be empty for new/running orders
                    staff_id = sessionManager.getUser()?.staff_id ?: 1,
                    is_dine_in = tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY",
                    is_take_away = tableStatus == "TAKEAWAY",
                    is_delivery = tableStatus == "DELIVERY",
                    table_id = tableId,
                    no_of_person = tableInfo.seating_capacity,
                    waiter_request_status = true,
                    kitchen_response_status = true, // Assuming KOT is being sent
                    order_status = "RUNNING",
                    is_merge = false,
                    is_active = 1,
                    order_master_id = newOrderMasterApiId["order_master_id"]
                        ?: "", // Use ID from getOrderNo
                    is_delivered = false
                )
                val response = apiService.createOrder(
                    orderRequest,
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                )
                if (response.isSuccessful && response.body() != null) {
                    orderMasterResponse = response.body()
                    currentOrderMasterId = orderMasterResponse!!.order_master_id
                    // Update table availability only if a new order is created for a dine-in table
                    if (orderRequest.is_dine_in) {
                        apiService.updateTableAvailability(
                            tableId,
                            "OCCUPIED",
                            sessionManager.getCompanyCode() ?: ""
                        )
                    }
                } else {
                    emit(
                        Result.failure(
                            Exception(
                                "Error creating new OrderMaster: ${response.code()}, ${
                                    response.errorBody()?.string()
                                }"
                            )
                        )
                    )
                    return@flow
                }
            } else {
                // If currentOrderMasterId was passed or found, but we don't have the TblOrderResponse object yet
                // You might need an API endpoint to fetch OrderMaster details by its ID if not already available
                // For now, let's assume if currentOrderMasterId is not null, it's valid.
                // The TblOrderResponse is mainly used to emit success, so we might need to construct a minimal one or fetch it.
                // This part depends on what TblOrderResponse should contain when updating.
                // For simplicity, let's assume we proceed and the success emission will primarily focus on the KOT.
                // Fetch the order master details if we only have the ID
                val masterResponse = apiService.getOrderMasterById(
                    currentOrderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                ) // YOU MIGHT NEED THIS ENDPOINT
                if (masterResponse.isSuccessful && masterResponse.body() != null) {
                    orderMasterResponse = masterResponse.body()
                } else {
                    emit(Result.failure(Exception("Could not retrieve details for existing OrderMaster ID: $currentOrderMasterId")))
                    return@flow
                }
            }


            // 3. Create OrderDetails for the items being placed (new or added)
            val newKotNumberMap = apiService.getKotNo(
                sessionManager.getCompanyCode() ?: ""
            ) // Get a KOT number for this batch of items
            val newKotNumber = newKotNumberMap["kot_number"]

            if (currentOrderMasterId.isEmpty() || newKotNumber == null) {
                emit(Result.failure(Exception("Failed to obtain OrderMaster ID or KOT number.")))
                return@flow
            }


            val orderDetailsList = itemsToPlace.map { item ->
                val pricePerUnit = when (tableStatus) {
                    "AC" -> item.menuItem.ac_rate
                    "TAKEAWAY", "DELIVERY" -> item.menuItem.parcel_rate
                    else -> item.menuItem.rate
                }
                val tax = apiService.getTaxSplit(
                    item.menuItem.tax_id,
                    sessionManager.getCompanyCode() ?: ""
                )
                val cgst = tax[0].tax_split_percentage
                val sgst = tax[1].tax_split_percentage
                val totalAmountForTaxCalc = pricePerUnit
                val taxAmount = calculateGst(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    true,
                    sgst.toDouble(),
                    cgst.toDouble()
                )
                val cess = calculateGstAndCess(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    item.menuItem.cess_per.toDouble(),
                    true,
                    item.menuItem.cess_specific,
                    sgst.toDouble(),
                    cgst.toDouble()
                )
                OrderDetails(
                    order_master_id = currentOrderMasterId,
                    order_details_id = 0,
                    kot_number = newKotNumber,
                    menu_item_id = item.menuItem.menu_item_id,
                    rate = if (item.menuItem.is_inventory != 1L) taxAmount.basePrice.roundTo2() else cess.basePrice.roundTo2(),
                    actual_rate = pricePerUnit,
                    qty = item.quantity,
                    total = if (item.menuItem.is_inventory != 1L) (taxAmount.basePrice * item.quantity).roundTo2() else (cess.basePrice * item.quantity).roundTo2(), // Total base price for this item line
                    tax_id = item.menuItem.tax_id,
                    tax_name = item.menuItem.tax_name,
                    tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmount.gstAmount * item.quantity).roundTo2() else (cess.gstAmount * item.quantity).roundTo2(),
                    sgst_per = if (tableStatus != "DELIVERY") sgst.toDouble() else 0.0,
                    sgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.sgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.sgst * item.quantity).roundTo2() else 0.0
                    },
                    cgst_per = if (tableStatus != "DELIVERY") cgst.toDouble() else 0.0,
                    cgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.cgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.cgst * item.quantity).roundTo2() else 0.0
                    }, // Adjust if your backend calculates differently
                    igst_per = item.menuItem.tax_percentage.toDouble(),
                    igst = taxAmount.gstAmount.roundTo2(),
                    cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                    cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.cessAmount * item.quantity).roundTo2() else 0.0,
                    cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                    grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.totalPrice * item.quantity).roundTo2() else (taxAmount.totalPrice * item.quantity).roundTo2(),
                    prepare_status = true,
                    item_add_mode = existingOpenOrderMasterId != null,
                    is_flag = false,
                    merge_order_nos = "",
                    merge_order_tables = "",
                    merge_pax = 0,
                    is_active = 1
                )
            }
            val detailsResponse = apiService.createOrderDetails(
                orderDetailsList,
                sessionManager.getCompanyCode() ?: ""
            )

            if (detailsResponse.isSuccessful) {
                if (orderMasterResponse != null) {
                    orderMasterResponse.kot_number = newKotNumber
                    emit(Result.success(orderMasterResponse))
                } else {
                    emit(Result.failure(Exception("Order details created, but failed to package final response.")))
                }
            } else {
                emit(
                    Result.failure(
                        Exception(
                            "Error creating OrderDetails: ${detailsResponse.code()}, ${
                                detailsResponse.errorBody()?.string()
                            }"
                        )
                    )
                )
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get open order items for a specific table to display in UI.
     * This needs to be robust to fetch all items belonging to any open OrderMaster for the table.
     */

    @SuppressLint("SuspiciousIndentation")
    suspend fun placeOrUpdateOrders(
        tableId: Long,
        itemsToPlace: List<OrderItem>,
        tableStatus: String,
        existingOpenOrderMasterId: String? = null // Allow passing it if already known
    ): Flow<Result<List<TblOrderDetailsResponse>>> = flow {
        if (itemsToPlace.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("Cannot place an order with no items.")))
            return@flow
        }

        try {
            var currentOrderMasterId = existingOpenOrderMasterId
            var orderMasterResponse: TblOrderResponse? = null

            if (currentOrderMasterId == null && tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY") {
                val openOrderResponse = apiService.getOpenOrderMasterForTable(
                    tableId,
                    sessionManager.getCompanyCode() ?: ""
                ) // YOU NEED TO IMPLEMENT/DEFINE THIS
                if (openOrderResponse.isSuccessful && openOrderResponse.body() != null) {
                    currentOrderMasterId = openOrderResponse.body()!!.order_master_id
                    orderMasterResponse = openOrderResponse.body()
                }
            }

            val tableInfo = apiService.getTablesByStatus(
                tableId,
                sessionManager.getCompanyCode() ?: ""
            ) // Assuming this gets details like seating_capacity, table_name

            if (currentOrderMasterId == null) {
                val newOrderMasterApiId = apiService.getOrderNo(
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                ) // Get new Order Master ID from API
                val orderRequest = OrderMaster(
                    order_date = getCurrentDateModern(),
                    order_create_time = getCurrentTimeModern(),
                    order_completed_time = "",
                    staff_id = sessionManager.getUser()?.staff_id ?: 1,
                    is_dine_in = tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY",
                    is_take_away = tableStatus == "TAKEAWAY",
                    is_delivery = tableStatus == "DELIVERY",
                    table_id = tableId,
                    no_of_person = tableInfo.seating_capacity,
                    waiter_request_status = true,
                    kitchen_response_status = true,
                    order_status = "RUNNING",
                    is_merge = false,
                    is_active = 1,
                    order_master_id = newOrderMasterApiId["order_master_id"] ?: "",
                    is_delivered = false
                )
                val response = apiService.createOrder(
                    orderRequest,
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                )
                if (response.isSuccessful && response.body() != null) {
                    orderMasterResponse = response.body()
                    currentOrderMasterId = orderMasterResponse!!.order_master_id
                    if (orderRequest.is_dine_in) {
                        apiService.updateTableAvailability(
                            tableId,
                            "OCCUPIED",
                            sessionManager.getCompanyCode() ?: ""
                        )
                    }
                } else {
                    emit(
                        Result.failure(
                            Exception(
                                "Error creating new OrderMaster: ${response.code()}, ${
                                    response.errorBody()?.string()
                                }"
                            )
                        )
                    )
                    return@flow
                }
            } else {
                val masterResponse = apiService.getOrderMasterById(
                    currentOrderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                ) // YOU MIGHT NEED THIS ENDPOINT
                if (masterResponse.isSuccessful && masterResponse.body() != null) {
                    orderMasterResponse = masterResponse.body()
                } else {
                    emit(Result.failure(Exception("Could not retrieve details for existing OrderMaster ID: $currentOrderMasterId")))
                    return@flow
                }
            }

            val newKotNumberMap = apiService.getKotNo(sessionManager.getCompanyCode() ?: "")
            val newKotNumber = newKotNumberMap["kot_number"]

            if (currentOrderMasterId.isEmpty() || newKotNumber == null) {
                emit(Result.failure(Exception("Failed to obtain OrderMaster ID or KOT number.")))
                return@flow
            }

            val orderDetailsList = itemsToPlace.map { item ->
                val pricePerUnit = when (tableStatus) {
                    "AC" -> item.menuItem.ac_rate
                    "PARCEL", "DELIVERY" -> item.menuItem.parcel_rate
                    else -> item.menuItem.rate
                }
                val tax = apiService.getTaxSplit(
                    item.menuItem.tax_id,
                    sessionManager.getCompanyCode() ?: ""
                )
                val cgst = tax[0].tax_split_percentage
                val sgst = tax[1].tax_split_percentage
                val totalAmountForTaxCalc = pricePerUnit
                val taxAmount = calculateGst(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    true,
                    sgst.toDouble(),
                    cgst.toDouble()
                )
                val cess = calculateGstAndCess(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    item.menuItem.cess_per.toDouble(),
                    true,
                    item.menuItem.cess_specific,
                    sgst.toDouble(),
                    cgst.toDouble()
                )
                OrderDetails(
                    order_master_id = currentOrderMasterId,
                    order_details_id = 0,
                    kot_number = newKotNumber,
                    menu_item_id = item.menuItem.menu_item_id,
                    rate = if (item.menuItem.is_inventory != 1L) taxAmount.basePrice.roundTo2() else cess.basePrice.roundTo2(),
                    actual_rate = pricePerUnit,
                    qty = item.quantity,
                    total = if (item.menuItem.is_inventory != 1L) (taxAmount.basePrice * item.quantity).roundTo2() else (cess.basePrice * item.quantity).roundTo2(), // Total base price for this item line
                    tax_id = item.menuItem.tax_id,
                    tax_name = item.menuItem.tax_name,
                    tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmount.gstAmount * item.quantity).roundTo2() else (cess.gstAmount * item.quantity).roundTo2(),
                    sgst_per = if (tableStatus != "DELIVERY") sgst.toDouble() else 0.0,
                    sgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.sgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.sgst * item.quantity).roundTo2() else 0.0
                    },
                    cgst_per = if (tableStatus != "DELIVERY") cgst.toDouble() else 0.0,
                    cgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.cgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.cgst * item.quantity).roundTo2() else 0.0
                    }, // Adjust if your backend calculates differently
                    igst_per = item.menuItem.tax_percentage.toDouble(),
                    igst = taxAmount.gstAmount.roundTo2(),
                    cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                    cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.cessAmount * item.quantity).roundTo2() else 0.0,
                    cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                    grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.totalPrice * item.quantity).roundTo2() else (taxAmount.totalPrice * item.quantity).roundTo2(),
                    prepare_status = true,
                    item_add_mode = existingOpenOrderMasterId != null,
                    is_flag = false,
                    merge_order_nos = "",
                    merge_order_tables = "",
                    merge_pax = 0,
                    is_active = 1
                )
            }
            val detailsResponse = apiService.createOrderDetails(
                orderDetailsList,
                sessionManager.getCompanyCode() ?: ""
            )

            if (detailsResponse.isSuccessful) {
                val response = detailsResponse.body()
                if (response != null) {
                    orderMasterResponse?.kot_number = newKotNumber
                    emit(Result.success(response))
                } else {
                    emit(Result.failure(Exception("Order details created, but failed to package final response.")))
                }
            } else {
                emit(
                    Result.failure(
                        Exception(
                            "Error creating OrderDetails: ${detailsResponse.code()}, ${
                                detailsResponse.errorBody()?.string()
                            }"
                        )
                    )
                )
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun placeOrderLocalDb(
        tableId: Long,
        itemsToPlace: List<OrderItem>,
        tableStatus: String,
        existingOpenOrderMasterId: String? = null
    ): Flow<Result<List<TblOrderDetailsResponse>>> = flow {
        try {
            var currentOrderMasterId = existingOpenOrderMasterId
            var orderMasterResponse: TblOrderResponse? = null

            // Step 1: Create Order Master if needed
            if (existingOpenOrderMasterId == null) {
                val voucher = voucherDao.getActiveVoucherByType(
                    sessionManager.getUser()?.counter_id?.toInt() ?: 0, "ORDER"
                )
                val newOrderMasterId = generateNextBillNo(
                    voucher?.starting_no ?: "ORD0001",
                    voucher?.starting_no ?: "ORD0001"
                )
                val orderMaster = TblOrderMaster(
                    order_master_id = newOrderMasterId,
                    order_date = getCurrentDateModern(),
                    order_create_time = getCurrentTimeModern(),
                    order_completed_time = "",
                    staff_id = sessionManager.getUser()?.staff_id?.toInt() ?: 1,
                    is_dine_in = tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY",
                    is_take_away = tableStatus == "TAKEAWAY",
                    is_delivery = tableStatus == "DELIVERY",
                    table_id = tableId.toInt(),
                    no_of_person = 0,
                    waiter_request_status = true,
                    kitchen_response_status = true,
                    order_status = "RUNNING",
                    is_merge = false,
                    is_active = true,
                    is_delivered = false,
                    is_online = false,
                    online_order_id = 1,
                    online_ref_no = "",
                    is_online_paid = false,
                    is_synced = SyncStatus.PENDING_SYNC
                )
                orderDao.insert(orderMaster)
                tableDao.updateTableAvailability(tableId, "OCCUPIED")
                currentOrderMasterId = newOrderMasterId
            } else {
                val orderMaster = orderDao.getById(existingOpenOrderMasterId)
                if (orderMaster != null) {
                    currentOrderMasterId = orderMaster.order_master_id
                }
            }

            // Step 2: Create Order Details
            val newKotNumber = orderDetailsDao.getMaxKOTNumber(getCurrentDateModern())
            val orderDetailsList = itemsToPlace.map { item ->
                val pricePerUnit = when (tableStatus) {
                    "AC" -> item.menuItem.ac_rate
                    "PARCEL", "DELIVERY" -> item.menuItem.parcel_rate
                    else -> item.menuItem.rate
                }
                val tax =
                    apiService.getTaxSplit(
                        item.menuItem.tax_id,
                        sessionManager.getCompanyCode() ?: ""
                    )
                val cgst = tax[0].tax_split_percentage
                val sgst = tax[1].tax_split_percentage
                val totalAmountForTaxCalc = pricePerUnit
                val taxAmount = calculateGst(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    true,
                    sgst.toDouble(),
                    cgst.toDouble()
                )
                val cess = calculateGstAndCess(
                    totalAmountForTaxCalc,
                    item.menuItem.tax_percentage.toDouble(),
                    item.menuItem.cess_per.toDouble(),
                    true,
                    item.menuItem.cess_specific,
                    sgst.toDouble(),
                    cgst.toDouble()
                )

                TblOrderDetails(
                    order_master_id = currentOrderMasterId.toString(),
                    order_details_id = 0,
                    kot_number = newKotNumber,
                    menu_item_id = item.menuItem.menu_item_id.toInt(),
                    rate = if (item.menuItem.is_inventory != 1L) taxAmount.basePrice.roundTo2() else cess.basePrice.roundTo2(),
                    actual_rate = pricePerUnit,
                    qty = item.quantity,
                    total = if (item.menuItem.is_inventory != 1L) (taxAmount.basePrice * item.quantity).roundTo2() else (cess.basePrice * item.quantity).roundTo2(),
                    tax_id = item.menuItem.tax_id.toInt(),
                    tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmount.gstAmount * item.quantity).roundTo2() else (cess.gstAmount * item.quantity).roundTo2(),
                    sgst_per = if (tableStatus != "DELIVERY") sgst.toDouble() else 0.0,
                    sgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.sgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.sgst * item.quantity).roundTo2() else 0.0
                    },
                    cgst_per = if (tableStatus != "DELIVERY") cgst.toDouble() else 0.0,
                    cgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmount.cgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cess.cgst * item.quantity).roundTo2() else 0.0
                    },
                    igst_per = item.menuItem.tax_percentage.toDouble(),
                    igst = taxAmount.gstAmount.roundTo2(),
                    cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                    cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.cessAmount * item.quantity).roundTo2() else 0.0,
                    cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                    grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.totalPrice * item.quantity).roundTo2() else (taxAmount.totalPrice * item.quantity).roundTo2(),
                    prepare_status = true,
                    item_add_mode = existingOpenOrderMasterId != null,
                    is_flag = false,
                    merge_order_nos = "",
                    merge_order_tables = "",
                    merge_pax = 0,
                    is_active = true
                )
            }

            orderDetailsDao.insertAll(orderDetailsList)

            // Step 3: Fetch back inserted details as response
            val savedDetails = orderDetailsDao.getByOrderMasterId(currentOrderMasterId.toString())
            val responseList = savedDetails.map { it.toTblOrderDetailsResponse() }

            emit(Result.success(responseList))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    private fun TblOrderDetails.toTblOrderDetailsResponse(): TblOrderDetailsResponse {
        return TblOrderDetailsResponse(
            order_master_id = this.order_master_id.toString(),
            order_details_id = this.order_details_id.toLong(),
            kot_number = this.kot_number?:0,
            menuItem = TblMenuItemResponse(
                menu_item_id = this.menu_item_id?.toLong() ?: 0L,
                menu_item_name = "",
                rate = 0.0,
                ac_rate = 0.0,
                parcel_rate = 0.0,
                tax_id = 0,
                tax_name = "",
                tax_percentage = 0.0.toString(),
                is_inventory = 0,
                cess_per = 0.0.toString(),
                cess_specific = 0.0,
                menu_item_code = TODO(),
                menu_item_name_tamil = TODO(),
                menu_id = TODO(),
                menu_name = TODO(),
                item_cat_id = TODO(),
                item_cat_name = TODO(),
                image = TODO(),
                parcel_charge = TODO(),
                kitchen_cat_id = TODO(),
                kitchen_cat_name = TODO(),
                is_available = TODO(),
                preparation_time = TODO(),
                is_favourite = TODO(),
                stock_maintain = TODO(),
                rate_lock = TODO(),
                unit_id = TODO(),
                unit_name = TODO(),
                min_stock = TODO(),
                hsn_code = TODO(),
                order_by = TODO(),
                is_raw = TODO(),
                is_active = TODO(),
                qty = TODO(),
                actual_rate = TODO()
            ),
            rate = this.rate ?: 0.0,
            actual_rate = this.actual_rate ?: 0.0,
            qty = this.qty ?: 0,
            total = this.total ?: 0.0,
            tax_id = this.tax_id?.toLong() ?: 0L,
            tax_amount = this.tax_amount ?: 0.0,
            sgst_per = this.sgst_per ?: 0.0,
            sgst = this.sgst ?: 0.0,
            cgst_per = this.cgst_per ?: 0.0,
            cgst = this.cgst ?: 0.0,
            igst_per = this.igst_per ?: 0.0,
            igst = this.igst ?: 0.0,
            cess_per = this.cess_per ?: 0.0,
            cess = this.cess ?: 0.0,
            cess_specific = this.cess_specific ?: 0.0,
            grand_total = this.grand_total ?: 0.0,
            prepare_status = this.prepare_status == true,
            item_add_mode = this.item_add_mode == true,
            is_flag = this.is_flag == true,
            merge_order_nos = this.merge_order_nos.toString(),
            merge_order_tables = this.merge_order_tables.toString(),
            merge_pax = this.merge_pax ?: 0,
            is_active = if (this.is_active == true) 1 else 0,
            tax_name = ""
        )
    }

    suspend fun updateOrderDetails(
        orderId: String?,
        items: List<OrderItem>,
        kotNumber: Int? = null,
        tableStatus: String
    ): Flow<Result<List<TblOrderDetailsResponse>>> = flow {
        val orderDetails = items.map { item ->

            val tax =
                apiService.getTaxSplit(item.menuItem.tax_id, sessionManager.getCompanyCode() ?: "")
            val cgst = tax[0].tax_split_percentage
            val sgst = tax[1].tax_split_percentage
            val totalAmountForTaxCalc = item.menuItem.actual_rate
            val taxAmount = calculateGst(
                totalAmountForTaxCalc,
                item.menuItem.tax_percentage.toDouble(),
                true,
                sgst.toDouble(),
                cgst.toDouble()
            )
            val cess = calculateGstAndCess(
                totalAmountForTaxCalc,
                item.menuItem.tax_percentage.toDouble(),
                item.menuItem.cess_per.toDouble(),
                true,
                item.menuItem.cess_specific,
                sgst.toDouble(),
                cgst.toDouble()
            )

            OrderDetails(
                order_master_id = orderId ?: "",
                order_details_id = item.orderDetailsId ?: 0L,
                kot_number = kotNumber ?: item.kotNumber,
                menu_item_id = item.menuItem.menu_item_id,
                rate = if (item.menuItem.is_inventory != 1L) taxAmount.basePrice.roundTo2() else cess.basePrice.roundTo2(),
                actual_rate = item.menuItem.actual_rate,
                qty = item.quantity,
                total = if (item.menuItem.is_inventory != 1L) (taxAmount.basePrice * item.quantity).roundTo2() else (cess.basePrice * item.quantity).roundTo2(), // Total base price for this item line
                tax_id = item.menuItem.tax_id,
                tax_name = item.menuItem.tax_name,
                tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmount.gstAmount * item.quantity).roundTo2() else (cess.gstAmount * item.quantity).roundTo2(),
                sgst_per = if (tableStatus != "DELIVERY") sgst.toDouble() else 0.0,
                sgst = if (item.menuItem.is_inventory != 1L) {
                    if (tableStatus != "DELIVERY") (taxAmount.sgst * item.quantity).roundTo2() else 0.0
                } else {
                    if (tableStatus != "DELIVERY") (cess.sgst * item.quantity).roundTo2() else 0.0
                },
                cgst_per = if (tableStatus != "DELIVERY") cgst.toDouble() else 0.0,
                cgst = if (item.menuItem.is_inventory != 1L) {
                    if (tableStatus != "DELIVERY") (taxAmount.cgst * item.quantity).roundTo2() else 0.0
                } else {
                    if (tableStatus != "DELIVERY") (cess.cgst * item.quantity).roundTo2() else 0.0
                }, // Adjust if your backend calculates differently
                igst_per = item.menuItem.tax_percentage.toDouble(),
                igst = taxAmount.gstAmount.roundTo2(),
                cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.cessAmount * item.quantity).roundTo2() else 0.0,
                cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cess.totalPrice * item.quantity).roundTo2() else (taxAmount.totalPrice * item.quantity).roundTo2(),
                prepare_status = true,
                item_add_mode = orderId != null,
                is_flag = false,
                merge_order_nos = "",
                merge_order_tables = "",
                merge_pax = 0,
                is_active = 1
            )
        }
        val response =
            apiService.updateOrderDetails(orderDetails, sessionManager.getCompanyCode() ?: "")
        if (response.isSuccessful) {
            val response = response.body()
            if (response != null) {
                emit(Result.success(response))
            } else {
                emit(Result.failure(Exception("Order details created, but failed to package final response.")))
            }
        } else {
            emit(
                Result.failure(
                    Exception(
                        "Error creating OrderDetails: ${response.code()}, ${
                            response.errorBody()?.string()
                        }"
                    )
                )
            )
        }
    }

    suspend fun getOpenOrderItemsForTable(tableId: Long): List<TblOrderDetailsResponse> {
        val openOrderMasterResponse = apiService.getOpenOrderMasterForTable(
            tableId,
            sessionManager.getCompanyCode() ?: ""
        ) // Assuming this is defined

        if (openOrderMasterResponse.isSuccessful) {
            val orderMaster = openOrderMasterResponse.body()
            if (orderMaster != null && orderMaster.order_status == "RUNNING") {
                val orderMasterId = orderMaster.order_master_id
                val orderDetailsResponse = apiService.getOpenOrderDetailsForTable(
                    orderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                ) // YOU MAY NEED TO RENAME/CREATE THIS
                if (orderDetailsResponse.isSuccessful && orderDetailsResponse.body() != null) {
                    return orderDetailsResponse.body()!!
                }
            }
        }
        return emptyList()
    }


    @SuppressLint("DefaultLocale")
    fun Double.roundTo2(): Double {
        val dec = sessionManager.getDecimalPlaces()
        return if (dec == 2L)
            BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
        else if (dec == 3L)
            BigDecimal.valueOf(this).setScale(3, RoundingMode.HALF_UP).toDouble()
        else
            BigDecimal.valueOf(this).setScale(4, RoundingMode.HALF_UP).toDouble()
    }

    /**
     * Get all orders
     * Our backend doesn't have a filter by table yet, so we get all orders and filter client-side
     */
    suspend fun getAllOrders(): List<TblOrderResponse> {
        try {
            val response = apiService.getAllOrders(sessionManager.getCompanyCode() ?: "")

            if (response.isSuccessful) {
                val orders = response.body()
                if (orders != null) {

                    return orders
                } else {
                    return emptyList()
                }
            } else {
                return emptyList()
            }
        } catch (e: Exception) {
            return emptyList()
        }
        return emptyList()
    }

    suspend fun getRunningOrderAmount(orderId: String): Map<String, Double> {
        val response =
            apiService.getRunningOrderAmount(orderId, sessionManager.getCompanyCode() ?: "")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        return emptyMap()
    }

    @SuppressLint("SuspiciousIndentation")
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    suspend fun printKOT(orderId: KOTRequest, ipAddress: String): Flow<Result<String>> =
        flow  { // Changed Flow type to Flow<Result<PrintResponse>>
            try {
                val response = apiService.printKOT(orderId, sessionManager.getCompanyCode() ?: "")
                val result = orderId.items
                if (response.isSuccessful) {
                    val printResponse = response.body()

                    var mess = ""
                    if (printResponse != null) {
                        if (sessionManager.getBluetoothPrinter()!=null)
                            printerHelper.printViaBluetoothMac(
                                data = printResponse.bytes(),
                                macAddress =  sessionManager.getBluetoothPrinter().toString()
                            ) { _, m -> mess = m }
                        else
                        printerHelper.printViaTcp(
                            ipAddress,
                            data = printResponse.bytes()
                        ) { success, message ->
                            mess = if (success) {
                                message
                            } else {
                                message
                            }
                        }
                        emit(Result.success(mess))
                    } else {
                        emit(Result.failure(Exception("KOT print successful but response body was empty.")))
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    emit(Result.failure(Exception("Failed to print KOT. Code: ${response.code()}, Error: $errorBody")))
                }
            } catch (e: Exception) {
                emit(Result.failure(Exception("Error printing KOT: ${e.message}", e)))
            }
        }

    suspend fun getIpAddress(category: String): String {
        val response = apiService.getIpAddresss(category, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful && response.body() != null)
            response.body()?.printerIpAddress ?: ""
        else
            ""
    }

    suspend fun getOrdersByOrderId(lng: String): Response<List<TblOrderDetailsResponse>> {
        return apiService.getOpenOrderDetailsForTable(lng, sessionManager.getCompanyCode() ?: "")
    }

    suspend fun deleteByid(orderDeatailId: Long): Int {
        val response =
            apiService.deleteOrderDetails(orderDeatailId, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful) response.body()!! else 0
    }
}

data class GstResult(
    val basePrice: Double,
    val gstAmount: Double,
    val totalPrice: Double,
    val cgst: Double,
    val sgst: Double
)

fun calculateGst(
    amount: Double,
    gstRate: Double,
    isInclusive: Boolean,
    sgst: Double,
    cgst: Double
): GstResult {
    return if (isInclusive) {
        val basePrice = amount / (1 + gstRate / 100)
        val gstAmount = basePrice * gstRate / 100
        val cgstAmount = basePrice * cgst / 100
        val sgstAmount = basePrice * sgst / 100
        val totalPrice = basePrice + gstAmount
        GstResult(basePrice, gstAmount, totalPrice, cgstAmount, sgstAmount)
    } else {
        val gstAmount = amount * gstRate / 100
        val cgstAmount = amount * cgst / 100
        val sgstAmount = amount * sgst / 100
        val totalPrice = amount + gstAmount
        GstResult(amount, gstAmount, totalPrice, cgstAmount, sgstAmount)
    }
}

data class GstCessResult(
    val basePrice: Double,
    val gstAmount: Double,
    val cessAmount: Double,
    val totalPrice: Double,
    val cgst: Double,
    val sgst: Double
)

fun calculateGstAndCess(
    amount: Double,
    gstRate: Double,
    cessRate: Double,
    isInclusive: Boolean,
    cessSpecific: Double,
    sgst: Double,
    cgst: Double
): GstCessResult {
    return if (isInclusive) {
        val amount = amount - cessSpecific
        val totalRate = gstRate + cessRate
        val basePrice = amount / (1 + totalRate / 100)
        val gstAmount = basePrice * gstRate / 100
        val cgstAmount = basePrice * cgst / 100
        val sgstAmount = basePrice * sgst / 100
        val cessAmount = basePrice * cessRate / 100
        val totalPrice = basePrice + gstAmount + cessAmount + cessSpecific
        GstCessResult(basePrice, gstAmount, cessAmount, totalPrice, cgstAmount, sgstAmount)
    } else {
        val gstAmount = amount * gstRate / 100
        val cgstAmount = amount * cgst / 100
        val sgstAmount = amount * sgst / 100
        val cessAmount = amount * cessRate / 100
        val totalPrice = amount + gstAmount + cessAmount + cessSpecific
        GstCessResult(amount, gstAmount, cessAmount, totalPrice, cgstAmount, sgstAmount)
    }
}