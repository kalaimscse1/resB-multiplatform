package com.warriortech.resb.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import com.senraise.printer.SrPrinter
import com.warriortech.resb.model.KOTRequest
import com.warriortech.resb.model.OrderDetails
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.OrderMaster
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.model.TblOrderResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.PrinterHelper
import com.warriortech.resb.util.getCurrentDateModern
import com.warriortech.resb.util.getCurrentTimeModern
import kotlinx.coroutines.flow.*
import retrofit2.Response
import java.math.BigDecimal
import java.math.RoundingMode
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Order-related API operations
 * Updated to work with the Kotlin Mini App backend
 */
@Singleton
class OrderRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager,
    private val printerHelper: PrinterHelper
) {
    /**
     * Create a new order
     * @param tableId The table ID
     * @param itemsToPlace List of order items
     */
    @SuppressLint("SuspiciousIndentation")
    fun placeOrUpdateOrder(
        tableId: Long,
        itemsToPlace: List<OrderItem>,
        tableStatus: String,
        existingOpenOrderMasterId: String? = null,
        deliveryBoyId: Long? = null,// Allow passing it if already known
        isOnline: Boolean = false,
        onlineRefNo: String = "",
        onlineOrderId: Int = 1
    ): Flow<Result<TblOrderResponse>> = flow {
        if (itemsToPlace.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("Cannot place an order with no items.")))
            return@flow
        }

        try {
            var currentOrderMasterId = existingOpenOrderMasterId
            var orderMasterResponse: TblOrderResponse?

            // 1. Check for/Determine existing open OrderMaster ID for the table
            if (currentOrderMasterId == null && tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY" && !isOnline) {
                // Try to find an open order for this table
                val openOrderResponse = apiService.getOpenOrderMasterForTable(
                    tableId,
                    sessionManager.getCompanyCode() ?: ""
                ) 
                if (openOrderResponse.isSuccessful && openOrderResponse.body() != null) {
                    currentOrderMasterId = openOrderResponse.body()!!.order_master_id
                    orderMasterResponse = openOrderResponse.body() 
                } else {
                    orderMasterResponse = null
                }
            } else {
                orderMasterResponse = null
                if (!isOnline) {
                    apiService.updateTableOpenStatus(tableId, false, sessionManager.getCompanyCode() ?: "")
                }
            }

            val tableInfo = apiService.getTablesByStatus(
                tableId,
                sessionManager.getCompanyCode() ?: ""
            ) 

            // 2. If no existing open OrderMaster, create a new one
            if (currentOrderMasterId == null) {
                val newOrderMasterApiId = apiService.getOrderNo(
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                ) 
                val orderRequest = OrderMaster(
                    order_date = getCurrentDateModern(),
                    order_create_time = getCurrentTimeModern(),
                    order_completed_time = "", 
                    staff_id = sessionManager.getUser()?.staff_id ?: 1,
                    is_dine_in = tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY" && !isOnline,
                    is_take_away = tableStatus == "TAKEAWAY",
                    is_delivery = tableStatus == "DELIVERY" ,
                    table_id = tableId,
                    no_of_person = tableInfo.seating_capacity,
                    waiter_request_status = true,
                    kitchen_response_status = true, 
                    order_status = "RUNNING",
                    is_merge = false,
                    is_active = 1,
                    order_master_id = newOrderMasterApiId["order_master_id"]
                        ?: "",
                    is_delivered = false,
                    note = if (isOnline) "Online Order" else "",
                    delivery_time = "",
                    delivery_boy_id = deliveryBoyId ?: 5,
                    is_online = isOnline,
                    online_ref_no = onlineRefNo,
                    online_order_id = onlineOrderId,
                    is_online_paid = isOnline,
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
            } else if (orderMasterResponse == null) {
                val masterResponse = apiService.getOrderMasterById(
                    currentOrderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                )
                if (masterResponse.isSuccessful && masterResponse.body() != null) {
                    orderMasterResponse = masterResponse.body()
                } else {
                    emit(Result.failure(Exception("Could not retrieve details for existing OrderMaster ID: $currentOrderMasterId")))
                    return@flow
                }
            }


            // 3. Create OrderDetails
            val isTaxEnabled = sessionManager.getGeneralSetting()?.is_tax ?: false
            val isTaxIncluded = sessionManager.getGeneralSetting()?.is_tax_included ?: false
            
            val newKotNumberMap = apiService.getKotNo(
                sessionManager.getCompanyCode() ?: ""
            ) 
            val newKotNumber = newKotNumberMap["kot_number"]

            if (currentOrderMasterId!!.isEmpty() || newKotNumber == null) {
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
                val cgstPer = tax.getOrNull(0)?.tax_split_percentage?.toDouble() ?: 0.0
                val sgstPer = tax.getOrNull(1)?.tax_split_percentage?.toDouble() ?: 0.0
                val totalTaxPer = item.menuItem.tax_percentage.toDouble()
                
                val taxAmountResult = if (isTaxEnabled) {
                    calculateGst(pricePerUnit, totalTaxPer, isTaxIncluded, sgstPer, cgstPer)
                } else {
                    GstResult(pricePerUnit, 0.0, pricePerUnit, 0.0, 0.0)
                }
                
                val cessAmountResult = if (isTaxEnabled && item.menuItem.is_inventory == 1L) {
                    calculateGstAndCess(
                        pricePerUnit,
                        totalTaxPer,
                        item.menuItem.cess_per.toDouble(),
                        isTaxIncluded,
                        item.menuItem.cess_specific,
                        sgstPer,
                        cgstPer
                    )
                } else {
                    GstCessResult(pricePerUnit, 0.0, 0.0, pricePerUnit, 0.0, 0.0)
                }

                OrderDetails(
                    order_master_id = currentOrderMasterId!!,
                    order_details_id = 0,
                    kot_number = newKotNumber,
                    menu_item_id = item.menuItem.menu_item_id,
                    rate = if (item.menuItem.is_inventory != 1L) taxAmountResult.basePrice.roundTo2() else cessAmountResult.basePrice.roundTo2(),
                    actual_rate = pricePerUnit,
                    qty = item.quantity,
                    total = if (item.menuItem.is_inventory != 1L) (taxAmountResult.basePrice * item.quantity).roundTo2() else (cessAmountResult.basePrice * item.quantity).roundTo2(), 
                    tax_id = item.menuItem.tax_id,
                    tax_name = item.menuItem.tax_name,
                    tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmountResult.gstAmount * item.quantity).roundTo2() else (cessAmountResult.gstAmount * item.quantity).roundTo2(),
                    sgst_per = if (tableStatus != "DELIVERY") sgstPer else 0.0,
                    sgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmountResult.sgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cessAmountResult.sgst * item.quantity).roundTo2() else 0.0
                    },
                    cgst_per = if (tableStatus != "DELIVERY") cgstPer else 0.0,
                    cgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmountResult.cgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cessAmountResult.cgst * item.quantity).roundTo2() else 0.0
                    }, 
                    igst_per = totalTaxPer,
                    igst = taxAmountResult.gstAmount.roundTo2(),
                    cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                    cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.cessAmount * item.quantity).roundTo2() else 0.0,
                    cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                    grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.totalPrice * item.quantity).roundTo2() else (taxAmountResult.totalPrice * item.quantity).roundTo2(),
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
                emit(Result.failure(Exception("Error creating OrderDetails: ${detailsResponse.code()}")))
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    /**
     * Get open order items for a specific table to display in UI.
     */

    @SuppressLint("SuspiciousIndentation")
    fun placeOrUpdateOrders(
        tableId: Long,
        itemsToPlace: List<OrderItem>,
        tableStatus: String,
        existingOpenOrderMasterId: String? = null ,
        deliveryBoyId: Long? = null,
    ): Flow<Result<List<TblOrderDetailsResponse>>> = flow {
        if (itemsToPlace.isEmpty()) {
            emit(Result.failure(IllegalArgumentException("No items.")))
            return@flow
        }

        try {
            var currentOrderMasterId = existingOpenOrderMasterId
            var orderMasterResponse: TblOrderResponse?

            if (currentOrderMasterId == null && tableStatus != "TAKEAWAY" && tableStatus != "DELIVERY") {
                val openOrderResponse = apiService.getOpenOrderMasterForTable(
                    tableId,
                    sessionManager.getCompanyCode() ?: ""
                ) 
                if (openOrderResponse.isSuccessful && openOrderResponse.body() != null) {
                    currentOrderMasterId = openOrderResponse.body()!!.order_master_id
                    orderMasterResponse = openOrderResponse.body()
                } else {
                    orderMasterResponse = null
                }
            } else {
                orderMasterResponse = null
            }

            val tableInfo = apiService.getTablesByStatus(
                tableId,
                sessionManager.getCompanyCode() ?: ""
            ) 

            if (currentOrderMasterId == null) {
                val newOrderMasterApiId = apiService.getOrderNo(
                    sessionManager.getCompanyCode() ?: "",
                    sessionManager.getUser()?.counter_id ?: 0L,
                    "ORDER"
                ) 
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
                    is_delivered = false,
                    note = "",
                    delivery_time = "",
                    delivery_boy_id = deliveryBoyId ?: 5
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
                    emit(Result.failure(Exception("Error creating OrderMaster")))
                    return@flow
                }
            } else if (orderMasterResponse == null) {
                val masterResponse = apiService.getOrderMasterById(
                    currentOrderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                ) 
                if (masterResponse.isSuccessful && masterResponse.body() != null) {
                    orderMasterResponse = masterResponse.body()
                } else {
                    emit(Result.failure(Exception("OrderMaster not found")))
                    return@flow
                }
            }

            val isTaxEnabled = sessionManager.getGeneralSetting()?.is_tax ?: false
            val isTaxIncluded = sessionManager.getGeneralSetting()?.is_tax_included ?: false

            val newKotNumberMap = apiService.getKotNo(sessionManager.getCompanyCode() ?: "")
            val newKotNumber = newKotNumberMap["kot_number"]

            if (currentOrderMasterId!!.isEmpty() || newKotNumber == null) {
                emit(Result.failure(Exception("Obtain failed")))
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
                val cgstPer = tax.getOrNull(0)?.tax_split_percentage?.toDouble() ?: 0.0
                val sgstPer = tax.getOrNull(1)?.tax_split_percentage?.toDouble() ?: 0.0
                val totalTaxPer = item.menuItem.tax_percentage.toDouble()
                Log.d("taxEnabled", isTaxEnabled.toString())

                val taxAmountResult = if (isTaxEnabled) {
                    calculateGst(pricePerUnit, totalTaxPer, isTaxIncluded, sgstPer, cgstPer)
                } else {
                    GstResult(pricePerUnit, 0.0, pricePerUnit, 0.0, 0.0)
                }

                val cessAmountResult = if (isTaxEnabled && item.menuItem.is_inventory == 1L) {
                    calculateGstAndCess(
                        pricePerUnit,
                        totalTaxPer,
                        item.menuItem.cess_per.toDouble(),
                        isTaxIncluded,
                        item.menuItem.cess_specific,
                        sgstPer,
                        cgstPer
                    )
                } else {
                    GstCessResult(pricePerUnit, 0.0, 0.0, pricePerUnit, 0.0, 0.0)
                }

                OrderDetails(
                    order_master_id = currentOrderMasterId!!,
                    order_details_id = 0,
                    kot_number = newKotNumber,
                    menu_item_id = item.menuItem.menu_item_id,
                    rate = if (item.menuItem.is_inventory != 1L) taxAmountResult.basePrice.roundTo2() else cessAmountResult.basePrice.roundTo2(),
                    actual_rate = pricePerUnit,
                    qty = item.quantity,
                    total = if (item.menuItem.is_inventory != 1L) (taxAmountResult.basePrice * item.quantity).roundTo2() else (cessAmountResult.basePrice * item.quantity).roundTo2(), 
                    tax_id = item.menuItem.tax_id,
                    tax_name = item.menuItem.tax_name,
                    tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmountResult.gstAmount * item.quantity).roundTo2() else (cessAmountResult.gstAmount * item.quantity).roundTo2(),
                    sgst_per = if (tableStatus != "DELIVERY") sgstPer else 0.0,
                    sgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmountResult.sgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cessAmountResult.sgst * item.quantity).roundTo2() else 0.0
                    },
                    cgst_per = if (tableStatus != "DELIVERY") cgstPer else 0.0,
                    cgst = if (item.menuItem.is_inventory != 1L) {
                        if (tableStatus != "DELIVERY") (taxAmountResult.cgst * item.quantity).roundTo2() else 0.0
                    } else {
                        if (tableStatus != "DELIVERY") (cessAmountResult.cgst * item.quantity).roundTo2() else 0.0
                    }, 
                    igst_per = totalTaxPer,
                    igst = taxAmountResult.gstAmount.roundTo2(),
                    cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                    cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.cessAmount * item.quantity).roundTo2() else 0.0,
                    cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                    grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.totalPrice * item.quantity).roundTo2() else (taxAmountResult.totalPrice * item.quantity).roundTo2(),
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
                    emit(Result.failure(Exception("Failed response.")))
                }
            } else {
                emit(Result.failure(Exception("Error")))
            }

        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getOrderMasterById(orderId: String): TblOrderResponse{
        return apiService.getOrderMasterById(orderId, sessionManager.getCompanyCode() ?: "").body()!!
    }

    @SuppressLint("DefaultLocale")
    fun Double.roundTo2(): Double {
        val dec = sessionManager.getDecimalPlaces()
        return when (dec) {
            2L -> BigDecimal.valueOf(this).setScale(2, RoundingMode.HALF_UP).toDouble()
            3L -> BigDecimal.valueOf(this).setScale(3, RoundingMode.HALF_UP).toDouble()
            else -> BigDecimal.valueOf(this).setScale(4, RoundingMode.HALF_UP).toDouble()
        }
    }

    fun updateOrderDetails(
        orderId: String?,
        items: List<OrderItem>,
        kotNumber: Int? = null,
        tableStatus: String
    ): Flow<Result<List<TblOrderDetailsResponse>>> = flow {
        
        val isTaxEnabled = sessionManager.getGeneralSetting()?.is_tax ?: false
        val isTaxIncluded = sessionManager.getGeneralSetting()?.is_tax_included ?: false

        val orderDetails = items.map { item ->

            val tax =
                apiService.getTaxSplit(item.menuItem.tax_id, sessionManager.getCompanyCode() ?: "")
            val cgstPer = tax.getOrNull(0)?.tax_split_percentage?.toDouble() ?: 0.0
            val sgstPer = tax.getOrNull(1)?.tax_split_percentage?.toDouble() ?: 0.0
            val totalTaxPer = item.menuItem.tax_percentage.toDouble()
            
            val totalAmountForTaxCalc = item.menuItem.actual_rate
            
            val taxAmountResult = if (isTaxEnabled) {
                calculateGst(totalAmountForTaxCalc, totalTaxPer, isTaxIncluded, sgstPer, cgstPer)
            } else {
                GstResult(totalAmountForTaxCalc, 0.0, totalAmountForTaxCalc, 0.0, 0.0)
            }
            
            val cessAmountResult = if (isTaxEnabled && item.menuItem.is_inventory == 1L) {
                calculateGstAndCess(
                    totalAmountForTaxCalc,
                    totalTaxPer,
                    item.menuItem.cess_per.toDouble(),
                    isTaxIncluded,
                    item.menuItem.cess_specific,
                    sgstPer,
                    cgstPer
                )
            } else {
                GstCessResult(totalAmountForTaxCalc, 0.0, 0.0, totalAmountForTaxCalc, 0.0, 0.0)
            }

            OrderDetails(
                order_master_id = orderId ?: "",
                order_details_id = item.orderDetailsId ?: 0L,
                kot_number = kotNumber ?: item.kotNumber,
                menu_item_id = item.menuItem.menu_item_id,
                rate = if (item.menuItem.is_inventory != 1L) taxAmountResult.basePrice.roundTo2() else cessAmountResult.basePrice.roundTo2(),
                actual_rate = item.menuItem.actual_rate,
                qty = item.quantity,
                total = if (item.menuItem.is_inventory != 1L) (taxAmountResult.basePrice * item.quantity).roundTo2() else (cessAmountResult.basePrice * item.quantity).roundTo2(), 
                tax_id = item.menuItem.tax_id,
                tax_name = item.menuItem.tax_name,
                tax_amount = if (item.menuItem.is_inventory != 1L) (taxAmountResult.gstAmount * item.quantity).roundTo2() else (cessAmountResult.gstAmount * item.quantity).roundTo2(),
                sgst_per = if (tableStatus != "DELIVERY") sgstPer else 0.0,
                sgst = if (item.menuItem.is_inventory != 1L) {
                    if (tableStatus != "DELIVERY") (taxAmountResult.sgst * item.quantity).roundTo2() else 0.0
                } else {
                    if (tableStatus != "DELIVERY") (cessAmountResult.sgst * item.quantity).roundTo2() else 0.0
                },
                cgst_per = if (tableStatus != "DELIVERY") cgstPer else 0.0,
                cgst = if (item.menuItem.is_inventory != 1L) {
                    if (tableStatus != "DELIVERY") (taxAmountResult.cgst * item.quantity).roundTo2() else 0.0
                } else {
                    if (tableStatus != "DELIVERY") (cessAmountResult.cgst * item.quantity).roundTo2() else 0.0
                }, 
                igst_per = totalTaxPer,
                igst = taxAmountResult.gstAmount.roundTo2(),
                cess_per = if (item.menuItem.is_inventory == 1L) item.menuItem.cess_per.toDouble() else 0.0,
                cess = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.cessAmount * item.quantity).roundTo2() else 0.0,
                cess_specific = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (item.menuItem.cess_specific * item.quantity).roundTo2() else 0.0,
                grand_total = if (item.menuItem.is_inventory == 1L && item.menuItem.cess_specific != 0.00) (cessAmountResult.totalPrice * item.quantity).roundTo2() else (taxAmountResult.totalPrice * item.quantity).roundTo2(),
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
            val body = response.body()
            if (body != null) {
                emit(Result.success(body))
            } else {
                emit(Result.failure(Exception("Failed")))
            }
        } else {
            emit(Result.failure(Exception("Error")))
        }
    }

    suspend fun getOpenOrderItemsForTable(tableId: Long): List<TblOrderDetailsResponse> {
        val openOrderMasterResponse = apiService.getOpenOrderMasterForTable(
            tableId,
            sessionManager.getCompanyCode() ?: ""
        ) 

        if (openOrderMasterResponse.isSuccessful) {
            val orderMaster = openOrderMasterResponse.body()
            if (orderMaster != null && orderMaster.order_status == "RUNNING") {
                val orderMasterId = orderMaster.order_master_id
                val orderDetailsResponse = apiService.getOpenOrderDetailsForTable(
                    orderMasterId,
                    sessionManager.getCompanyCode() ?: ""
                ) 
                if (orderDetailsResponse.isSuccessful && orderDetailsResponse.body() != null) {
                    return orderDetailsResponse.body()!!
                }
            }
        }
        return emptyList()
    }

    /**
     * Get all orders
     */
    suspend fun getAllOrders(): List<TblOrderResponse> {
        return try {
            val response = apiService.getAllOrders(sessionManager.getCompanyCode() ?: "")

            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            emptyList()
        }
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
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printKOT(orderId: KOTRequest, ipAddress: String,applicationContext: Context): Flow<Result<String>> =
        flow  { 
            try {
                val target = sessionManager.getPrinterType()
                
                val localSuccess = printerHelper.printKotWithTemplate(orderId, target, ipAddress)
                
                if (localSuccess) {
                    emit(Result.success("✅ KOT printed using local template"))
                } else {
                    val response = apiService.printKOT(orderId, sessionManager.getCompanyCode() ?: "")
                    if (response.isSuccessful) {
                        val printResponse = response.body()

                        var mess = ""
                        if (printResponse != null) {
                            val printerType = sessionManager.getPrinterType()
                            if (printerType == "BLUETOOTH" && sessionManager.getBluetoothPrinter() != null)
                                printerHelper.printViaBluetoothMac(
                                    data = printResponse.bytes(),
                                    macAddress =  sessionManager.getBluetoothPrinter().toString()
                                ) { _, m -> mess = m }
                            else if (printerType == "TCP")
                                printerHelper.printViaTcp(
                                    ipAddress,
                                    data = printResponse.bytes()
                                ) { _, message ->
                                    mess = message
                                }
                            else if (printerType =="InBuilt"){
                                SrPrinter.getInstance(applicationContext).printEpson(printResponse.bytes())
                            }
                            else
                                return@flow emit(Result.failure(Exception("Printer not configured")))
                            emit(Result.success(mess))
                        } else {
                            emit(Result.failure(Exception("KOT print empty body.")))
                        }
                    } else {
                        emit(Result.failure(Exception("Failed to print KOT")))
                    }
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
        val amnt = amount - cessSpecific
        val totalRate = gstRate + cessRate
        val basePrice = amnt / (1 + totalRate / 100)
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
