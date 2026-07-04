package com.warriortech.resb.ui.viewmodel.report

import android.Manifest
import android.content.Context
import android.util.Log
import androidx.annotation.RequiresPermission
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.LedgerDetailsRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.network.WhatsAppApi
import com.warriortech.resb.util.ReportExport
import com.warriortech.resb.util.getCurrentDateModern
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import okhttp3.RequestBody.Companion.toRequestBody
import timber.log.Timber
import javax.inject.Inject
import kotlin.random.Random

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
    private val ledgerDetailsRepository: LedgerDetailsRepository,
    private val apiService: ApiService,
    private val whatsappApi: WhatsAppApi
) : ViewModel() {

    private val _uiState = MutableStateFlow<PaidBillsUiState>(PaidBillsUiState.Idle)
    val uiState: StateFlow<PaidBillsUiState> = _uiState.asStateFlow()

    private val _selectedBill = MutableStateFlow<TblBillingResponse?>(null)
    val selectedBill: StateFlow<TblBillingResponse?> = _selectedBill.asStateFlow()

    private val _editableItems = MutableStateFlow<List<TblMenuItemResponse>>(emptyList())
    val editable: StateFlow<List<TblMenuItemResponse>> = _editableItems.asStateFlow()

    private val _billedItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    val billedItems: StateFlow<Map<TblMenuItemResponse, Int>> = _billedItems.asStateFlow()

    val orderId = MutableStateFlow<String?>(null)
    private val _orderDetails = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    // Editable totals and payment modes
    private val _orderAmt = MutableStateFlow(0.0)
    val orderAmt: StateFlow<Double> = _orderAmt.asStateFlow()

    private val _taxAmt = MutableStateFlow(0.0)
    val taxAmt: StateFlow<Double> = _taxAmt.asStateFlow()

    private val _grandTotal = MutableStateFlow(0.0)
    val grandTotal: StateFlow<Double> = _grandTotal.asStateFlow()

    private val _cash = MutableStateFlow(0.0)
    val cash: StateFlow<Double> = _cash.asStateFlow()

    private val _card = MutableStateFlow(0.0)
    val card: StateFlow<Double> = _card.asStateFlow()

    private val _upi = MutableStateFlow(0.0)
    val upi: StateFlow<Double> = _upi.asStateFlow()

    private val _due = MutableStateFlow(0.0)
    val due: StateFlow<Double> = _due.asStateFlow()

    private val _discount = MutableStateFlow(0.0)
    val discount: StateFlow<Double> = _discount.asStateFlow()

    // OTP States
    private val _generatedOtp = MutableStateFlow<String?>(null)
    private val _otpExpiryTime = MutableStateFlow<Long>(0)
    
    private val _otpState = MutableStateFlow<OtpState>(OtpState.Idle)
    val otpState: StateFlow<OtpState> = _otpState.asStateFlow()

    sealed class OtpState {
        object Idle : OtpState()
        object Sending : OtpState()
        object Sent : OtpState()
        object Verified : OtpState()
        data class Error(val message: String) : OtpState()
    }

    fun requestOtpForEdit(billNo: String) {
        viewModelScope.launch {
            try {
                _otpState.value = OtpState.Sending
                val otp = (100000..999999).random().toString()

                val ownerEmail = sessionManager.getGeneralSetting()?.recipient_no
                
                if (ownerEmail.isNullOrBlank()) {
                    _otpState.value = OtpState.Error("Owner Contact no not configured in profile")
                    return@launch
                }

                val response = whatsappApi.sendWhatsApp(
                    secret = sessionManager.getGeneralSetting()?.secret_key?.toRequestBody()?:"66a02ca4cbae00a9b996ba9d1f62a51c56cbccd1".toRequestBody(),
                    account = sessionManager.getGeneralSetting()?.account_key?.toRequestBody()?:"1783161865a87ff679a2f3e71d9181a67b7542122c6a48e409ad8ed".toRequestBody(),
                    recipient = sessionManager.getGeneralSetting()?.recipient_no?.toRequestBody()?:"".toRequestBody(),           // +919876543210
                    type = "text".toRequestBody(),
                    message = ("Your OTP is $otp\n" +
                            "For ${billNo}\n"
                            +"OTP Expiry in 3 mins").toRequestBody()
                )
                if (response.isSuccessful) {
                    _generatedOtp.value = otp
                    _otpExpiryTime.value = System.currentTimeMillis() + (3 * 60 * 1000)
                    _otpState.value = OtpState.Sent
                } else {
                    _otpState.value = OtpState.Error("Failed to send OTP: ${response.message()}")
                }
            } catch (e: Exception) {
                _otpState.value = OtpState.Error("Error: ${e.message}")
            }
        }
    }

    fun verifyOtp(enteredOtp: String): Boolean {
        val currentTime = System.currentTimeMillis()
        if (currentTime > _otpExpiryTime.value) {
            _otpState.value = OtpState.Error("OTP expired. Please request a new one.")
            return false
        }
        
        if (enteredOtp == _generatedOtp.value) {
            _otpState.value = OtpState.Verified
            return true
        } else {
            _otpState.value = OtpState.Error("Invalid OTP")
            return false
        }
    }

    fun clearOtpState() {
        _otpState.value = OtpState.Idle
        _generatedOtp.value = null
        _otpExpiryTime.value = 0
    }

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
                    }
                }
            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun selectBill(bill: String) {
        viewModelScope.launch {
            try {
                val res = billRepository.getPaymentByBillNo(bill)
                res?.let {
                    _selectedBill.value = it
                    _orderAmt.value = it.order_amt
                    _taxAmt.value = it.tax_amt
                    _grandTotal.value = it.grand_total
                    _cash.value = it.cash
                    _card.value = it.card
                    _upi.value = it.upi
                    _due.value = it.due
                    _discount.value = it.disc_amt

                    val orderDetail = orderRepository.getOrdersByOrderId(it.order_master.order_master_id)
                    orderId.value = orderDetail.body()?.firstOrNull()?.order_master_id
                    val orders = orderDetail.body()!!
                    _orderDetails.value = orders
                    val menuItems = orders.map { d ->
                        TblMenuItemResponse(
                            menu_item_id = d.menuItem.menu_item_id,
                            menu_item_code = d.menuItem.menu_item_code,
                            menu_item_name = d.menuItem.menu_item_name,
                            menu_item_name_tamil = d.menuItem.menu_item_name_tamil,
                            item_cat_id = d.menuItem.item_cat_id,
                            item_cat_name = d.menuItem.item_cat_name,
                            rate = d.rate,
                            ac_rate = d.rate,
                            parcel_rate = d.rate,
                            parcel_charge = d.rate,
                            tax_id = d.menuItem.tax_id,
                            tax_name = d.menuItem.tax_name,
                            tax_percentage = d.menuItem.tax_percentage,
                            kitchen_cat_id = d.menuItem.kitchen_cat_id,
                            kitchen_cat_name = d.menuItem.kitchen_cat_name,
                            stock_maintain = d.menuItem.stock_maintain,
                            rate_lock = d.menuItem.rate_lock,
                            unit_id = d.menuItem.unit_id,
                            unit_name = d.menuItem.unit_name,
                            min_stock = d.menuItem.min_stock,
                            hsn_code = d.menuItem.hsn_code,
                            order_by = d.menuItem.order_by,
                            is_inventory = d.menuItem.is_inventory,
                            is_raw = d.menuItem.is_raw,
                            is_available = d.menuItem.is_available,
                            image = d.menuItem.image,
                            qty = d.qty,
                            cess_specific = d.cess_specific,
                            cess_per = d.cess_per.toString(),
                            is_favourite = d.menuItem.is_favourite,
                            menu_id = d.menuItem.menu_id,
                            menu_name = d.menuItem.menu_name,
                            is_active = d.menuItem.is_active,
                            preparation_time = d.menuItem.preparation_time,
                            actual_rate = d.actual_rate
                        )
                    }
                    _billedItems.value = menuItems.associateWith { item -> item.qty }
                    _editableItems.value = menuItems
                }
            } catch (e: Exception) {
                Timber.e(e, "Error selecting bill")
            }
        }
    }

    fun updatePaymentAmounts(cash: Double, card: Double, upi: Double, discount: Double) {
        _cash.value = cash
        _card.value = card
        _upi.value = upi
        _discount.value = discount
        calculateTotals()
    }

    private fun calculateTotals() {
        val items = _billedItems.value
        val baseAmt = items.entries.sumOf { it.key.actual_rate * it.value }
        val tax = items.entries.sumOf { 
            (it.key.tax_percentage.toDoubleOrNull() ?: 0.0) / 100 * (it.key.actual_rate * it.value) 
        }
        
        _orderAmt.value = baseAmt
        _taxAmt.value = tax
        val total = baseAmt + tax - _discount.value
        _grandTotal.value = total
        
        // Auto-adjust due if payments don't match total
        val currentPayments = _cash.value + _card.value + _upi.value
        _due.value = if (total > currentPayments) total - currentPayments else 0.0
    }

    fun updateItemQuantity(menuItem: TblMenuItemResponse, newQuantity: Int) {
        val currentItems = _billedItems.value.toMutableMap()
        if (newQuantity > 0) {
            currentItems[menuItem] = newQuantity
        } else {
            currentItems.remove(menuItem)
        }
        _billedItems.value = currentItems.toMap()
        calculateTotals()
    }

    fun removeItem(menuItem: TblMenuItemResponse) {
        val currentItems = _billedItems.value.toMutableMap()
        currentItems.remove(menuItem)
        _billedItems.value = currentItems.toMap()
        calculateTotals()
    }

    fun updateBill(billNo: String) {
        viewModelScope.launch {
            try {
                val orderItems = _billedItems.value.entries.map { (menuItem, quantity) ->
                    val originalDetail = _orderDetails.value.find { it.menuItem.menu_item_id == menuItem.menu_item_id }
                    OrderItem(
                        quantity = quantity,
                        menuItem = menuItem,
                        orderDetailsId = originalDetail?.order_details_id ?: 0L,
                        kotNumber = originalDetail?.kot_number ?: 0
                    )
                }

                orderRepository.updateOrderDetails(
                    orderId = orderId.value,
                    items = orderItems,
                    tableStatus = ""
                ).collect { result ->
                    result.fold(
                        onSuccess = {
                            val request = TblBillingRequest(
                                bill_no = billNo,
                                cash = _cash.value,
                                card = _card.value,
                                upi = _upi.value,
                                due = _due.value,
                                disc_amt = _discount.value,
                                order_amt = _orderAmt.value,
                                tax_amt = _taxAmt.value,
                                grand_total = _grandTotal.value,
                                received_amt = _cash.value + _card.value + _upi.value,
                                pending_amt = _due.value,
                                is_active = 1L,
                                bill_date ="",
                                bill_create_time = "",
                                order_master_id = "" ,
                                voucher_id = 1,
                                staff_id = 1,
                                customer_id = 1,
                                cust_contact_no = "",
                                cust_address = "",
                                cess = 0.0,
                                cess_specific = 0.0,
                                delivery_amt =0.0,
                                round_off = 0.0,
                                rounded_amt = 0.0,
                                others = 0.0,
                                change = 0.0,
                                note = "" ,
                                tendered_amt = 0.0,
                                upi_type_id = 1L
                            )
                            billRepository.updateBill(billNo, it.first().order_master_id, request)
                            _uiState.value = PaidBillsUiState.Success(emptyList()) // Trigger success
                        },
                        onFailure = {
                            _uiState.value = PaidBillsUiState.Error("Update failed: ${it.message}")
                        }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error("Update exception: ${e.message}")
            }
        }
    }

    fun deleteBill(billNo: String) {
        viewModelScope.launch {
            try {
                billRepository.deleteBill(billNo)
                ledgerDetailsRepository.deleteByEntryNo(billNo)
                val currentState = _uiState.value
                if (currentState is PaidBillsUiState.Success) {
                    val updatedBills = currentState.bills.filter { it.bill_no != billNo }
                    _uiState.value = PaidBillsUiState.Success(updatedBills)
                }
            } catch (e: Exception) {
                _uiState.value = PaidBillsUiState.Error("Failed to delete bill: ${e.message}")
            }
        }
    }
    fun sendBillViaWhatsApp(billNo: TblBillingResponse,context: Context) {
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
                    paperWidth = sessionManager.getPaperWidth(),
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

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBill(billNo: String,context: Context) {
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
                    paperWidth = sessionManager.getPaperWidth(),
                    received_amt = bill?.received_amt ?: 0.0,
                    pending_amt = bill?.pending_amt?:0.0
                )

                val ip = orderRepository.getIpAddress("COUNTER")
                val printResponse = billRepository.printBill(billDetails, ip,context)
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


    fun clearSelection() {
        _selectedBill.value = null
    }

    fun clearError() {
        if (_uiState.value is PaidBillsUiState.Error) {
            _uiState.value = PaidBillsUiState.Idle
        }
    }
}
