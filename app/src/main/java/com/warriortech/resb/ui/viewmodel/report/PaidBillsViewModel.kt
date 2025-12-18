package com.warriortech.resb.ui.viewmodel.report

import android.util.Log
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.LedgerDetailsRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.model.Bill
import com.warriortech.resb.model.BillItem
import com.warriortech.resb.model.Modifiers
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.TblBillingRequest
import com.warriortech.resb.model.TblBillingResponse
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.viewmodel.report.KotViewModel.KotActionState
import com.warriortech.resb.util.ReportExport
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

sealed class PaidBillsUiState {
    object Loading : PaidBillsUiState()
    data class Success(val bills: List<TblBillingResponse>) : PaidBillsUiState()
    data class Error(val message: String) : PaidBillsUiState()
    object Idle : PaidBillsUiState()
}

@HiltViewModel
class PaidBillsViewModel @Inject constructor(
    private val billRepository: BillRepository,
    private val sessionManager: SessionManager,
    private val orderRepository: OrderRepository,
    private val ledgerDetailsRepository: LedgerDetailsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaidBillsUiState>(PaidBillsUiState.Idle)
    val uiState: StateFlow<PaidBillsUiState> = _uiState.asStateFlow()

    private val _selectedBill = MutableStateFlow<TblBillingResponse?>(null)
    val selectedBill: StateFlow<TblBillingResponse?> = _selectedBill.asStateFlow()

    private val _editableItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val editable: StateFlow<List<TblMenuItemResponse>> = _editableItems.asStateFlow()

    private val _billedItems = MutableStateFlow<Map<TblMenuItemResponse,Int>>(emptyMap())
    val billedItems: StateFlow<Map<TblMenuItemResponse,Int>> = _billedItems.asStateFlow()


    val orderId = MutableStateFlow<String?>(null)

    val _orderDetails = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())
    fun loadPaidBills(fromDate: String, toDate: String) {
        viewModelScope.launch {
            try {
                _uiState.value = PaidBillsUiState.Loading
                val tenantId = sessionManager.getCompanyCode() ?: ""
                val response = billRepository.getPaidBills(tenantId, fromDate, toDate)

                response.collect { result ->
                    result.onSuccess { bills ->
                        _uiState.value = PaidBillsUiState.Success(bills)
                    }.onFailure { error ->
                        _uiState.value = PaidBillsUiState.Error(error.message ?: "Unknown error")
                        Log.e("PaidBillsViewModel", "Error loading paid bills", error)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error(e.message ?: "Unknown error")
                Log.e("PaidBillsViewModel", "Exception loading paid bills", e)
            }
        }
    }

    fun selectBill(bill: String) {
        viewModelScope.launch {
            try {
                val res = billRepository.getPaymentByBillNo(bill)
                val orderDetail =
                    orderRepository.getOrdersByOrderId(res?.order_master?.order_master_id ?: "")
                orderId.value = orderDetail.body()?.firstOrNull()?.order_master_id
                val orders = orderDetail.body()!!
                _orderDetails.value = orders
                val menutItems = orders.map {
                    TblMenuItemResponse(
                        menu_item_id = it.menuItem.menu_item_id,
                        menu_item_name = it.menuItem.menu_item_name,
                        menu_item_name_tamil = it.menuItem.menu_item_name_tamil,
                        item_cat_id = it.menuItem.item_cat_id,
                        item_cat_name = it.menuItem.item_cat_name,
                        rate = it.rate,
                        ac_rate = it.rate,
                        parcel_rate = it.rate,
                        parcel_charge = it.rate,
                        tax_id = it.menuItem.tax_id,
                        tax_name = it.menuItem.tax_name,
                        tax_percentage = it.menuItem.tax_percentage,
                        kitchen_cat_id = it.menuItem.kitchen_cat_id,
                        kitchen_cat_name = it.menuItem.kitchen_cat_name,
                        stock_maintain = it.menuItem.stock_maintain,
                        rate_lock = it.menuItem.rate_lock,
                        unit_id = it.menuItem.unit_id,
                        unit_name = it.menuItem.unit_name,
                        min_stock = it.menuItem.min_stock,
                        hsn_code = it.menuItem.hsn_code,
                        order_by = it.menuItem.order_by,
                        is_inventory = it.menuItem.is_inventory,
                        is_raw = it.menuItem.is_raw,
                        is_available = it.menuItem.is_available,
                        image = it.menuItem.image,
                        qty = it.qty,
                        cess_specific = it.cess_specific,
                        cess_per = it.cess_per.toString(),
                        is_favourite = it.menuItem.is_favourite,
                        menu_item_code = it.menuItem.menu_item_code,
                        menu_id = it.menuItem.menu_id,
                        menu_name = it.menuItem.menu_name,
                        is_active = it.menuItem.is_active,
                        preparation_time = it.menuItem.preparation_time,
                        actual_rate = it.actual_rate
                    )
                }
                _billedItems.value = menutItems.associateWith { it.qty }.toMutableMap()
                _editableItems.value = menutItems
                _selectedBill.value = res
            } catch (e: Exception) {

            }
        }

    }

    fun deleteBill(billNo: String) {
        viewModelScope.launch {
            try {
                // Implement delete logic here
                // You would call billRepository.deleteBill(billNo)
                // For now, just refresh the list
                billRepository.deleteBill(billNo)
                ledgerDetailsRepository.deleteByEntryNo(billNo)
                val currentState = _uiState.value
                if (currentState is PaidBillsUiState.Success) {
                    val updatedBills = currentState.bills.filter { it.bill_no != billNo }
                    _uiState.value = PaidBillsUiState.Success(updatedBills)
                }
            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error("Failed to delete bill: ${e.message}")
                Log.e("PaidBillsViewModel", "Error deleting bill", e)
            }
        }
    }
    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun printBill(billNo: String) {
        viewModelScope.launch {
            try {
                // Implement print logic here
                // You would call billRepository.printBill(billNo)
                val tamil = sessionManager.getGeneralSetting()?.tamil_receipt_print ?: false
                val bill = billRepository.getPaymentByBillNo(billNo)
                var sn = 1
                val orderDetails =
                    orderRepository.getOrdersByOrderId(bill?.order_master?.order_master_id ?: "")
                        .body()!!
                val counter =
                    sessionManager.getUser()?.counter_name ?: "Counter1"
                val billItems = orderDetails.map { detail ->
                    val menuItem = detail.menuItem
                    val qty = detail.qty
                    BillItem(
                        sn = sn++,
                        itemName = if (tamil) menuItem.menu_item_name_tamil else menuItem.menu_item_name,
                        qty = qty,
                        price = detail.actual_rate,
                        basePrice = detail.rate,
                        amount = qty * detail.actual_rate,
                        sgstPercent = menuItem.tax_percentage.toDouble() / 2,
                        cgstPercent = menuItem.tax_percentage.toDouble() / 2,
                        igstPercent = if (detail.igst > 0) menuItem.tax_percentage.toDouble() else 0.0,
                        cessPercent = if (detail.cess > 0) menuItem.cess_per.toDouble() else 0.0,
                        sgst = detail.sgst,
                        cgst = detail.cgst,
                        igst = if (detail.igst > 0) detail.igst else 0.0,
                        cess = if (detail.cess > 0) detail.cess else 0.0,
                        cess_specific = if (detail.cess_specific > 0) detail.cess_specific else 0.0,
                        taxPercent = menuItem.tax_percentage.toDouble(),
                        taxAmount = detail.tax_amount
                    )
                }
                val billDetails = Bill(
                    company_code = sessionManager.getCompanyCode() ?: "",
                    billNo = bill?.bill_no ?: "",
                    date = bill?.bill_date.toString(),
                    time = bill?.bill_create_time.toString(),
                    orderNo = bill?.order_master?.order_master_id ?: "",
                    counter = counter,
                    tableNo = bill?.order_master?.table_name ?: "",
                    custName = bill?.customer?.customer_name ?: "",
                    custNo = bill?.customer?.contact_no ?: "",
                    custAddress = bill?.customer?.address ?: "",
                    custGstin = bill?.customer?.gst_no ?: "",
                    items = billItems,
                    subtotal = bill?.order_amt ?: 0.0,
                    deliveryCharge = 0.0, // Assuming no delivery charge
                    discount = bill?.disc_amt ?: 0.0,
                    roundOff = bill?.round_off ?: 0.0,
                    total = bill?.grand_total ?: 0.0,
                    paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 58 else 80,
                    received_amt = bill?.received_amt ?: 0.0,
                    pending_amt = bill?.pending_amt?:0.0
                )

                val ip = orderRepository.getIpAddress("COUNTER")
                val printResponse = billRepository.printBill(billDetails, ip)
                printResponse.collect { result ->
                    result.fold(
                        onSuccess = { message ->
                            Timber.e(message)
                        },
                        onFailure = { error ->
                            Timber.e(error, "Failed to print bill")
                        }
                    )
                }

            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error("Failed to print bill: ${e.message}")
                Log.e("PaidBillsViewModel", "Error printing bill", e)
            }
        }
    }

    fun updateBill(billNo: String) {
        viewModelScope.launch {
            val orderItem = _billedItems.value.entries.map { (menuItem, quantity) ->
                val id =
                    _orderDetails.value.filter { it.menuItem.menu_item_id == menuItem.menu_item_id }
                OrderItem(
                    quantity = quantity,
                    menuItem = menuItem,
                    orderDetailsId = id.first().order_details_id,
                    kotNumber = id.first().kot_number
                )
            }
            orderRepository.updateOrderDetails(
                orderId = orderId.value,
                items = orderItem,
                tableStatus = ""
            ).collect { result ->
                result.fold(
                    onSuccess = {
                        billRepository.updateBill(billNo, it.first().order_master_id)
                        loadPaidBills(getCurrentDateModern(),getCurrentDateModern())
                    },
                    onFailure = {
                        _uiState.value =
                            PaidBillsUiState.Error("Failed to print bill: ${it.message}")
                    }
                )
            }
        }
    }
    fun updateItemQuantity(menuItem: TblMenuItemResponse, newQuantity: Int) {
        val currentItems = _billedItems.value.toMutableMap()
        if (newQuantity > 0) {
            currentItems[menuItem] = newQuantity
        } else {
            currentItems.remove(menuItem)
        }
        _billedItems.value = currentItems.toMap()
    }

    fun removeItem(menuItem: TblMenuItemResponse) {
        val orderId =
            _orderDetails.value.filter { it.menuItem.menu_item_id == menuItem.menu_item_id }
        viewModelScope.launch {
            val currentItems = _billedItems.value.toMutableMap()
            _billedItems.value = currentItems.toMap()
            orderRepository.deleteByid(orderDeatailId = orderId.first().order_details_id)
        }

    }

    fun sendBillViaWhatsApp(billNo: TblBillingResponse,context: android.content.Context) {
        viewModelScope.launch {
            try {
                // Implement print logic here
                // You would call billRepository.printBill(billNo)
                val tamil = sessionManager.getGeneralSetting()?.tamil_receipt_print ?: false
                val bill = billNo
                var sn = 1
                val orderDetails =
                    orderRepository.getOrdersByOrderId(bill.order_master.order_master_id)
                        .body()!!
                val counter =
                    sessionManager.getUser()?.counter_name ?: "Counter1"
                val billItems = orderDetails.map { detail ->
                    val menuItem = detail.menuItem
                    val qty = detail.qty
                    BillItem(
                        sn = sn++,
                        itemName = if (tamil) menuItem.menu_item_name_tamil else menuItem.menu_item_name,
                        qty = qty,
                        price = menuItem.rate,
                        basePrice = detail.rate,
                        amount = qty * menuItem.rate,
                        sgstPercent = menuItem.tax_percentage.toDouble() / 2,
                        cgstPercent = menuItem.tax_percentage.toDouble() / 2,
                        igstPercent = if (detail.igst > 0) menuItem.tax_percentage.toDouble() else 0.0,
                        cessPercent = if (detail.cess > 0) menuItem.cess_per.toDouble() else 0.0,
                        sgst = detail.sgst,
                        cgst = detail.cgst,
                        igst = if (detail.igst > 0) detail.igst else 0.0,
                        cess = if (detail.cess > 0) detail.cess else 0.0,
                        cess_specific = if (detail.cess_specific > 0) detail.cess_specific else 0.0,
                        taxPercent = menuItem.tax_percentage.toDouble(),
                        taxAmount = detail.tax_amount
                    )
                }
                val billDetails = Bill(
                    company_code = sessionManager.getCompanyCode() ?: "",
                    billNo = bill.bill_no,
                    date = bill.bill_date.toString(),
                    time = bill.bill_create_time.toString(),
                    orderNo = bill.order_master.order_master_id,
                    counter = counter,
                    tableNo = bill.order_master.table_name,
                    custName = bill.customer.customer_name,
                    custNo = bill.customer.contact_no,
                    custAddress = bill.customer.address,
                    custGstin = bill.customer.gst_no,
                    items = billItems,
                    subtotal = bill.order_amt,
                    deliveryCharge = 0.0, // Assuming no delivery charge
                    discount = bill.disc_amt,
                    roundOff = bill.round_off,
                    total = bill.grand_total,
                    paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 58 else 80,
                    received_amt = bill.received_amt,
                    pending_amt = bill.pending_amt
                )
                ReportExport.generateBillPdf(billDetails, context,sessionManager)

            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error("Failed to print bill: ${e.message}")
                Log.e("PaidBillsViewModel", "Error printing bill", e)
            }
        }
    }

    fun clearSelection() {
        _selectedBill.value = null
    }

    fun clearError() {
        if (_uiState.value is PaidBillsUiState.Error) {
            _uiState.value = PaidBillsUiState.Idle
        }
    }
}

