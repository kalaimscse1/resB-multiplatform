package com.warriortech.resb.ui.viewmodel.payment

import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.BillRepository
import com.warriortech.resb.data.repository.CustomerRepository
import com.warriortech.resb.data.repository.OrderRepository
import com.warriortech.resb.data.repository.calculateGst
import com.warriortech.resb.model.Bill
import com.warriortech.resb.model.BillItem
import com.warriortech.resb.model.OrderItem
import com.warriortech.resb.model.TblCustomer
import com.warriortech.resb.model.TblMenuItemResponse
import com.warriortech.resb.model.TblOrderDetailsResponse
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.service.PrintService
import com.warriortech.resb.ui.viewmodel.setting.CustomerSettingsViewModel.UiState
import com.warriortech.resb.util.CurrencySettings
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2

data class PaymentMethod(
    val id: String,
    val name: String,
    val iconResId: Int? = null
)

sealed interface PaymentProcessingState {
    object Idle : PaymentProcessingState
    object Processing : PaymentProcessingState
    data class Success(val order: PaidOrder, val transactionId: String) : PaymentProcessingState
    data class Error(val message: String) : PaymentProcessingState
}

data class BillingPaymentUiState(
    val billedItems: Map<TblMenuItemResponse, Int> = emptyMap(),
    val tableStatus: String = "TABLE",
    val discountFlat: Double = 0.0,
    val otherChrages: Double = 0.0,
    val cessSpecific: Double = 0.0,
    val cessAmount: Double = 0.0,
    val subtotal: Double = 0.0,
    val taxAmount: Double = 0.0,
    val totalAmount: Double = 0.0,
    val orderDetails: List<TblOrderDetailsResponse> = emptyList(),
    val orderMasterId: String? = null,
    val discount: Double = 0.0,
    val selectedKotNumber: Int? = null,
    val availablePaymentMethods: List<PaymentMethod> = emptyList(),
    val selectedPaymentMethod: PaymentMethod? = null,
    val amountToPay: Double = 0.0,
    val paymentProcessingState: PaymentProcessingState = PaymentProcessingState.Idle,
    val amountReceived: Double = 0.0,
    val changeAmount: Double = 0.0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val cashAmount: Double = 0.0,
    val cardAmount: Double = 0.0,
    val upiAmount: Double = 0.0,
    val customer: TblCustomer? = null
)

@HiltViewModel
class BillingViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val billRepository: BillRepository,
    private val orderRepository: OrderRepository,
    private val printService: PrintService,
    private val sessionManager: SessionManager,
    private val customerRepository: CustomerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BillingPaymentUiState())
    val uiState: StateFlow<BillingPaymentUiState> = _uiState.asStateFlow()

    private val _customer = MutableStateFlow<TblCustomer?>(null)
    val customer: StateFlow<TblCustomer?> = _customer.asStateFlow()

    private val _customers = MutableStateFlow<List<TblCustomer>>(emptyList())
    val customers: StateFlow<List<TblCustomer>> = _customers

    private val _selectedItems = MutableStateFlow<Map<TblMenuItemResponse, Int>>(emptyMap())
    val selectedItems: StateFlow<Map<TblMenuItemResponse, Int>> = _selectedItems.asStateFlow()

    val _originalOrderDetails = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())
    private val _filteredOrderDetails = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    private val _totalAmount = MutableStateFlow(0.0)
    val totalAmount: StateFlow<Double> = _totalAmount.asStateFlow()

    private val _orderId = MutableStateFlow("")
    val orderId: StateFlow<String> = _orderId.asStateFlow()

    private val _billNo = MutableStateFlow("")
    val billNo: StateFlow<String> = _billNo.asStateFlow()

    private val _customerId = MutableStateFlow(0L)
    val customerId: StateFlow<Long> = _customerId.asStateFlow()

    private val _preview = MutableStateFlow<Bitmap?>(null)
    val preview: StateFlow<Bitmap?> = _preview
    var res = false
    val orderDetailsResponse1 = MutableStateFlow<List<TblOrderDetailsResponse>>(emptyList())

    init {
        viewModelScope.launch {
            val customerList = customerRepository.getAllCustomers()
            _customers.value = customerList
            loadAvailablePaymentMethods()
            CurrencySettings.update(
                symbol = sessionManager.getRestaurantProfile()?.currency ?: "",
                decimals = sessionManager.getRestaurantProfile()?.decimal_point?.toInt() ?: 2
            )
        }
    }

    fun updateCustomerId(id: Long) {
        _customerId.value = id
    }

    fun loadBillPreview(bill: Bill) {
        viewModelScope.launch {
            val bmp = billRepository.fetchBillPreview(bill)
            _preview.value = bmp
        }
    }

    fun addCustomer(customer: TblCustomer) {
        viewModelScope.launch {
            try {
                customerRepository.insertCustomer(customer)
                loadCustomers()
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to add customer") }
            }
        }
    }

    fun updateSelectedCustomer(customer: TblCustomer) {
        _uiState.value = _uiState.value.copy(customer = customer)
    }

    fun updateBillNo(billNo: String) {
        _billNo.value = billNo
    }

    fun loadCustomers() {
        viewModelScope.launch {
            try {
                val customerList = customerRepository.getAllCustomers()
                _customers.value = customerList
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = e.message ?: "Failed to load customers") }
            }
        }
    }

    /**
     * Central function to recalc totals based on billed items
     */

    private fun recalcTotals(items: Map<TblMenuItemResponse, Int>): BillingPaymentUiState {

        val subtotal = items.entries.sumOf { (menuItem, qty) -> menuItem.rate * qty }
        val taxAmount = items.entries.sumOf { (menuItem, qty) ->
            val gst = calculateGst(
                menuItem.actual_rate,
                menuItem.tax_percentage.toDouble(),
                true,
                menuItem.tax_percentage.toDouble() / 2,
                menuItem.tax_percentage.toDouble() / 2
            )
            Log.d("GSTCALC", "recalcTotals: $gst ${menuItem.actual_rate}")
            gst.gstAmount * qty
        }

        val cessAmount = items.entries.sumOf { (menuItem, qty) ->
            if (menuItem.is_inventory == 1L && menuItem.cess_specific != 0.00) {
                menuItem.cess_specific * qty
            } else 0.0
        }

        val cessSpecific = items.entries.sumOf { (menuItem, qty) ->
            if (menuItem.is_inventory == 1L) {
                (menuItem.actual_rate * qty) * (menuItem.cess_per.toDoubleOrNull() ?: 0.0) / 100.0
            } else 0.0
        }

        val discountFlat = _uiState.value.discountFlat
        val totalAmount = subtotal + taxAmount + cessAmount + cessSpecific - discountFlat

        return _uiState.value.copy(
            billedItems = items,
            subtotal = subtotal,
            taxAmount = taxAmount,
            cessAmount = cessAmount,
            cessSpecific = cessSpecific,
            totalAmount = totalAmount,
            amountToPay = totalAmount
        )
    }

    fun setMenuDetails(menu: Map<TblMenuItemResponse, Int>) {
        _selectedItems.value = menu
    }

    fun setOrderId(orderId: String) {
        _orderId.value = orderId
    }

    fun setBillingDetailsFromOrderResponse(
        orderDetails: List<TblOrderDetailsResponse>,
        orderMasterId: String
    ) {
        viewModelScope.launch {
            val order = orderRepository.getOrdersByOrderId(orderMasterId)
            if (order.body() != null) {
                val orderDetailsResponse = order.body()!!
                _orderId.value = orderMasterId
                _originalOrderDetails.value = orderDetailsResponse
                _filteredOrderDetails.value = orderDetailsResponse
                val menuItems = orderDetailsResponse.map {

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
                val itemsMap = menuItems.associateWith { it.qty }.toMutableMap()
                var tableStatus = "TABLE" // Default
                val subtotal = orderDetailsResponse.sumOf { it.total }
                val taxAmount = orderDetailsResponse.sumOf { it.tax_amount }
                val cessAmount = orderDetailsResponse.sumOf { if (it.cess > 0) it.cess else 0.0 }
                val cessSpecific =
                    orderDetailsResponse.sumOf { if (it.cess_specific > 0) it.cess_specific else 0.0 }
                val totalAmount =
                    subtotal + taxAmount + cessAmount + cessSpecific + _uiState.value.otherChrages
                if (cessAmount > 0.0) {
                    _uiState.update { currentState ->
                        currentState.copy(
                            billedItems = itemsMap,
                            tableStatus = tableStatus,
                            subtotal = subtotal,
                            taxAmount = taxAmount,
                            totalAmount = totalAmount,
                            amountToPay = totalAmount,
                            orderDetails = orderDetails,
                            orderMasterId = orderMasterId,
                            cessAmount = cessAmount,
                            cessSpecific = cessSpecific
                        )
                    }
                } else {
                    _uiState.update { currentState ->
                        currentState.copy(
                            billedItems = itemsMap,
                            tableStatus = tableStatus,
                            subtotal = subtotal,
                            taxAmount = taxAmount,
                            totalAmount = totalAmount,
                            amountToPay = totalAmount,
                            orderDetails = orderDetails,
                            orderMasterId = orderMasterId
                        )
                    }
                }
            } else {
                _uiState.update { it.copy(errorMessage = "Order not found") }
            }
        }
    }

    fun placeOrder(item: Map<TblMenuItemResponse, Int>) {
        // Implementation for placing order if needed
        viewModelScope.launch {
            val orderItems = item.map { (menuItem, quantity) ->
                OrderItem(
                    quantity = quantity,
                    menuItem = menuItem,
                )
            }
            orderRepository.placeOrUpdateOrders(
                2, orderItems,
                ""
            ).collect { result ->
                result.fold(
                    onSuccess = { order ->
                        _orderId.value = order.firstOrNull()?.order_master_id ?: ""
                        orderDetailsResponse1.value = order
                        _selectedItems.value =
                            emptyMap() // Clear selected items after placing order
                    },
                    onFailure = { error ->

                    }
                )
            }
        }
    }

    fun setCustomer(customer: TblCustomer?) {
        _customer.value = customer
    }

    fun filterByKotNumber(kotNumber: Int) {
        val filtered =
            _originalOrderDetails.value
        _filteredOrderDetails.value = filtered
        val itemsMap = mutableMapOf<TblMenuItemResponse, Int>()
        filtered.forEach { detail ->
            val existingQty = itemsMap[detail.menuItem] ?: 0
            itemsMap[detail.menuItem] = existingQty + detail.qty
        }
        _uiState.value = recalcTotals(itemsMap).copy(
            selectedKotNumber = if (kotNumber == -1) null else kotNumber
        )
    }

    fun updateKotItem(
        orderDetailId: Long,
        newQuantity: Int,
        newRate: Double
    ) {
        val updatedDetails = _originalOrderDetails.value.map { detail ->
            if (detail.order_details_id == orderDetailId) {
                detail.copy(
                    qty = newQuantity,
                    rate = newRate,
                    total = newQuantity * newRate
                )
            } else detail
        }
        _originalOrderDetails.value = updatedDetails
        val itemsMap = mutableMapOf<TblMenuItemResponse, Int>()
        updatedDetails.forEach { detail ->
            val menuItem = detail.menuItem.copy(rate = detail.rate, qty = detail.qty)
            itemsMap[menuItem] = detail.qty
        }
        _uiState.value = recalcTotals(itemsMap)
    }

    fun updateItemQuantity(menuItem: TblMenuItemResponse, newQuantity: Int) {
        val currentItems = _uiState.value.billedItems.toMutableMap()
        if (newQuantity > 0) {
            currentItems[menuItem] = newQuantity
        } else {
            currentItems.remove(menuItem)
        }
        _uiState.value = recalcTotals(currentItems)
    }

    fun updateTotal() {
        _totalAmount.value = selectedItems.value.entries.sumOf { it.key.rate * it.value }
    }

    fun removeItem(menuItem: TblMenuItemResponse) {
        val currentItems = _uiState.value.billedItems.toMutableMap()
        currentItems.remove(menuItem)
        _uiState.value = recalcTotals(currentItems)
    }

    fun updateTaxPercentage(tax: Double) {
        _uiState.update { currentState ->
            val newTaxAmount = calculateTaxAmount(currentState.subtotal, tax)
            val newTotalAmount =
                calculateTotal(currentState.subtotal, newTaxAmount, currentState.discountFlat)
            currentState.copy(
                taxAmount = newTaxAmount,
                totalAmount = newTotalAmount,
                amountToPay = newTotalAmount
            )
        }
    }

    fun updateDiscountFlat(discount: Double) {
        _uiState.update { currentState ->
            val newTotalAmount =
                calculateTotal(currentState.subtotal, currentState.taxAmount, discount) + currentState.otherChrages
            currentState.copy(
                discountFlat = discount,
                totalAmount = newTotalAmount,
                amountToPay = newTotalAmount
            )
        }
    }

    fun updateOtherCharges(charges: Double) {
        _uiState.update { currentState ->
            val newTotalAmount =
                calculateTotal(currentState.subtotal, currentState.taxAmount, currentState.discountFlat) + charges
            currentState.copy(
                otherChrages = charges,
                totalAmount = newTotalAmount,
                amountToPay = newTotalAmount
            )
        }
    }

    private fun calculateTaxAmount(subtotal: Double, taxPercentage: Double): Double {
        return subtotal * (taxPercentage / 100)
    }

    private fun calculateTotal(subtotal: Double, taxAmount: Double, discountFlat: Double): Double {
        return subtotal + taxAmount - discountFlat
    }

    private fun loadAvailablePaymentMethods() {
        _uiState.update {
            it.copy(
                availablePaymentMethods = listOf(
                    PaymentMethod("cash", "CASH"),
                    PaymentMethod("card", "CARD"),
                    PaymentMethod("upi", "UPI"),
                    PaymentMethod("due", "DUE"),
                    PaymentMethod("others", "OTHERS")
                )
            )
        }
    }

    fun selectPaymentMethod(method: PaymentMethod) {
        _uiState.update { it.copy(selectedPaymentMethod = method) }
    }

    fun updateAmountToPay(amount: Double) {
        _uiState.update { it.copy(amountToPay = amount) }
    }

    fun updateOrderMasterId(orderId: String) {
        _uiState.update { it.copy(orderMasterId = orderId) }
    }

    fun updateCashAmount(amount: Double) {
        val currentState = _uiState.value
        if (currentState.cashAmount != amount) {
            _uiState.update { it.copy(cashAmount = amount) }
        }
    }

    fun updateCardAmount(amount: Double) {
        val currentState = _uiState.value
        if (currentState.cardAmount != amount) {
            _uiState.update { it.copy(cardAmount = amount) }
        }
    }

    fun updateUpiAmount(amount: Double) {
        val currentState = _uiState.value
        if (currentState.upiAmount != amount) {
            _uiState.update { it.copy(upiAmount = amount) }
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun processPayment(voucherType: String) {
        val currentState = _uiState.value
        val paymentMethod = currentState.selectedPaymentMethod
        val amount = when (paymentMethod?.name) {
            "CASH" -> if (currentState.cashAmount == 0.0) currentState.amountToPay else currentState.cashAmount
            "CARD" -> if (currentState.cardAmount == 0.0) currentState.amountToPay else currentState.cardAmount
            "UPI" -> if (currentState.upiAmount == 0.0) currentState.amountToPay else currentState.upiAmount
            "OTHERS" -> currentState.cashAmount + currentState.cardAmount + currentState.upiAmount
            else -> currentState.amountToPay
        }
        if (paymentMethod == null) {
            _uiState.update { it.copy(errorMessage = "Please select a payment method.") }
            return
        }
        if (amount <= 0) {
            _uiState.update { it.copy(errorMessage = "Payment amount must be greater than zero.") }
            return
        }
        _uiState.update {
            it.copy(
                paymentProcessingState = PaymentProcessingState.Processing,
                errorMessage = null
            )
        }
        viewModelScope.launch {
            val tamil = sessionManager.getGeneralSetting()?.tamil_receipt_print == true
            billRepository.bill(
                orderMasterId = currentState.orderMasterId ?: "",
                paymentMethod = paymentMethod,
                receivedAmt = amount,
                customer = _customer.value ?: TblCustomer(
                    customer_id = 1L,
                    customer_name = "GUEST",
                    contact_no = "",
                    address = "",
                    gst_no = "",
                    is_active = 1L,
                    email_address = "",
                    igst_status = false
                ),
                billNo = _billNo.value,
                totals = Triple(
                    currentState.cashAmount,
                    currentState.cardAmount,
                    currentState.upiAmount
                ),
                voucherType = voucherType,
                total = currentState.amountToPay
            ).collect { result ->
                result.fold(
                    onSuccess = { response ->
                        var sn = 1
                        val orderDetails =
                            orderRepository.getOrdersByOrderId(response.order_master.order_master_id)
                                .body()!!
                        val counter = sessionManager.getUser()?.counter_name ?: "Counter1"
                        val customer = response.customer
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
                            billNo = response.bill_no,
                            date = response.bill_date.toString(),
                            time = response.bill_create_time.toString(),
                            orderNo = response.order_master.order_master_id,
                            counter = counter,
                            tableNo = response.order_master.table_name,
                            custName = customer.customer_name,
                            custNo = customer.contact_no,
                            custAddress = customer.address,
                            custGstin = customer.gst_no,
                            items = billItems,
                            subtotal = response.order_amt,
                            deliveryCharge = 0.0, // Assuming no delivery charge
                            discount = response.disc_amt,
                            roundOff = response.round_off,
                            total = response.grand_total,
                            paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 58 else 80,
                            received_amt = response.received_amt,
                            pending_amt = response.pending_amt
                        )
                        val data = currentState
                        val isReceipt = sessionManager.getGeneralSetting()?.is_receipt ?: false
                        if (isReceipt) {
                            printBill(billDetails, data, amount, paymentMethod)
                        } else {
                            delay(2000) // Simulate network delay
                            val transactionId = UUID.randomUUID().toString()
                            val paidOrder = PaidOrder(
                                items = currentState.billedItems,
                                tableStatus = currentState.tableStatus,
                                subtotal = currentState.subtotal,
                                taxAmount = currentState.taxAmount,
                                discount = currentState.discountFlat,
                                totalAmount = currentState.totalAmount,
                                paidAmount = amount,
                                paymentMethod = paymentMethod.name,
                                transactionId = transactionId,
                                timestamp = System.currentTimeMillis()
                            )
                            Log.d("Payment", "Payment successful$paidOrder")
                            _uiState.update {
                                it.copy(
                                    paymentProcessingState = PaymentProcessingState.Success(
                                        paidOrder,
                                        transactionId
                                    )
                                )
                            }
                        }
                        Log.d("Payment", "Payment successful")
                    },
                    onFailure = { error ->
                        Log.e("Payment", "Payment failed: ${error.message}")
                        _uiState.update {
                            it.copy(paymentProcessingState = PaymentProcessingState.Error("Payment failed: ${error.message}"))
                        }
                    }
                )
            }
        }
    }

    @androidx.annotation.RequiresPermission(android.Manifest.permission.BLUETOOTH_CONNECT)
    fun printBill(
        bill: Bill,
        currentState: BillingPaymentUiState,
        amount: Double,
        paymentMethod: PaymentMethod
    ) {
        viewModelScope.launch {
            val isReceipt = sessionManager.getGeneralSetting()?.is_receipt ?: false
            if (isReceipt) {
                val ip = orderRepository.getIpAddress("COUNTER")
                val printResponse = billRepository.printBill(bill, ip)
                printResponse.collect { result ->
                    result.fold(
                        onSuccess = { message ->
                            delay(2000)
                            val transactionId = UUID.randomUUID().toString()
                            val paidOrder = PaidOrder(
                                items = currentState.billedItems,
                                tableStatus = currentState.tableStatus,
                                subtotal = currentState.subtotal,
                                taxAmount = currentState.taxAmount,
                                discount = currentState.discountFlat,
                                totalAmount = currentState.totalAmount,
                                paidAmount = amount,
                                paymentMethod = paymentMethod.name,
                                transactionId = transactionId,
                                timestamp = System.currentTimeMillis()
                            )
                            _uiState.update {
                                it.copy(
                                    paymentProcessingState = PaymentProcessingState.Success(
                                        paidOrder,
                                        transactionId
                                    )
                                )
                            }
                        },
                        onFailure = { error ->
                            _uiState.update { it.copy(errorMessage = "Failed to print bill: ${error.message}") }
                        }
                    )
                }
            } else {
                val transactionId = UUID.randomUUID().toString()
                val paidOrder = PaidOrder(
                    items = currentState.billedItems,
                    tableStatus = currentState.tableStatus,
                    subtotal = currentState.subtotal,
                    taxAmount = currentState.taxAmount,
                    discount = currentState.discountFlat,
                    totalAmount = currentState.totalAmount,
                    paidAmount = amount,
                    paymentMethod = paymentMethod.name,
                    transactionId = transactionId,
                    timestamp = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        paymentProcessingState = PaymentProcessingState.Success(
                            paidOrder,
                            transactionId
                        )
                    )
                }
            }
        }
    }


    fun updatePaymentMethod(paymentMethodName: String) {
        val currentState = _uiState.value
        if (currentState.selectedPaymentMethod?.name != paymentMethodName) {
            val paymentMethod =
                currentState.availablePaymentMethods.find { it.name == paymentMethodName }
            _uiState.update {
                currentState.copy(selectedPaymentMethod = paymentMethod)
            }
        }
    }

    fun resetPaymentState() {
        _uiState.update {
            it.copy(
                paymentProcessingState = PaymentProcessingState.Idle,
                errorMessage = null
            )
        }
    }

    fun clearErrorMessage() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun updateAmountReceived(amount: Double) {
        _uiState.update { currentState ->
            val change = amount - currentState.totalAmount
            currentState.copy(
                amountReceived = amount,
                changeAmount = maxOf(0.0, change)
            )
        }
    }

    fun updateDiscount(discount: Double) {
        _uiState.value = _uiState.value.copy(discount = discount)
    }

    fun previewDetails(billNo: String) {
        viewModelScope.launch {
            val bill = billRepository.getPaymentByBillNo(billNo)
            val response = bill!!
            val orderDetails =
                orderRepository.getOrdersByOrderId(bill.order_master.order_master_id)
                    .body()!!
            val counter = sessionManager.getUser()?.counter_name ?: "Counter1"
            var sn = 1
            val billItems = orderDetails.map { detail ->
                val menuItem = detail.menuItem
                val qty = detail.qty
                BillItem(
                    sn = sn++,
                    itemName = menuItem.menu_item_name,
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
                billNo = response.bill_no,
                date = response.bill_date.toString(),
                time = response.bill_create_time.toString(),
                orderNo = response.order_master.order_master_id,
                counter = counter,
                tableNo = response.order_master.table_name,
                custName = response.customer.customer_name,
                custNo = response.customer.contact_no,
                custAddress = response.customer.address,
                custGstin = response.customer.gst_no,
                items = billItems,
                subtotal = response.order_amt,
                deliveryCharge = 0.0, // Assuming no delivery charge
                discount = response.disc_amt,
                roundOff = response.round_off,
                total = response.grand_total,
                paperWidth = if(sessionManager.getBluetoothPrinter() !=null) 58 else 80,
                received_amt = response.received_amt,
                pending_amt = response.pending_amt
            )
            preview(billDetails)
        }
    }

    fun preview(bill: Bill){
        viewModelScope.launch {
            val bmp = billRepository.fetchBillPreview(bill)
            _preview.value = bmp
        }
    }


}

data class PaidOrder(
    val items: Map<TblMenuItemResponse, Int>,
    val tableStatus: String,
    val subtotal: Double,
    val taxAmount: Double,
    val discount: Double,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMethod: String,
    val transactionId: String,
    val timestamp: Long
)