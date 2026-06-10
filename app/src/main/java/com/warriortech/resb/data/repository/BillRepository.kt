package com.warriortech.resb.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RequiresPermission
import androidx.compose.ui.platform.LocalContext
import com.warriortech.resb.model.*
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.ui.viewmodel.payment.PaymentMethod
import com.warriortech.resb.util.NetworkMonitor
import com.warriortech.resb.util.PrinterHelper
import com.warriortech.resb.util.getCurrentDateModern
import com.warriortech.resb.util.getCurrentTimeModern
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import com.senraise.printer.SrPrinter
import dagger.hilt.android.qualifiers.ApplicationContext

class BillRepository @Inject constructor(
    private val apiService: ApiService,
    override val networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager,
    private val printerHelper: PrinterHelper
) : OfflineFirstRepository(networkMonitor) {
    fun getPaidBills(
        tenantId: String,
        fromDate: String,
        toDate: String
    ): Flow<Result<List<TblBillingResponse>>> = flow {
        try {
            val response = apiService.getSalesReport(tenantId, fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { bills ->
                    emit(Result.success(bills))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getUnpaidBills(
        tenantId: String,
        fromDate: String,
        toDate: String
    ): Flow<Result<List<TblBillingResponse>>> = flow {
        try {
            val response = apiService.getUnPaidBills(tenantId, fromDate, toDate)
            if (response.isSuccessful) {
                response.body()?.let { bills ->
                    emit(Result.success(bills))
                } ?: emit(Result.failure(Exception("No data received")))
            } else {
                emit(Result.failure(Exception("API Error: ${response.code()} ${response.message()}")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun getBills(
        tenantId: String,
        fromDate: String,
        toDate: String,
        paid: Boolean
    ): Flow<Result<List<TblBillingResponse>>> = flow {
        try {
            val response = if (paid) apiService.getSalesReport(tenantId, fromDate, toDate)
            else apiService.getUnPaidBills(tenantId, fromDate, toDate)

            if (response.isSuccessful) {
                response.body()?.let { emit(Result.success(it)) }
                    ?: emit(Result.failure(Exception("Empty response body")))
            } else emit(Result.failure(Exception("API Error ${response.code()} - ${response.message()}")))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    fun bill(
        orderMasterId: String,
        paymentMethod: PaymentMethod,
        receivedAmt: Double,
        customer: TblCustomer,
        billNo: String,
        voucherType: String,
        totals: Triple<Double, Double, Double> = Triple(0.0, 0.0, 0.0), // cash, card, upi
        total: Double = 0.0,
        tenderedAmt: Double = 0.0,
        discount: Double = 0.0,
        otherCharges: Double = 0.0,
        roundOff: Double=0.0,
        upiTypeId: Long = 1L
    ): Flow<Result<TblBillingResponse>> = flow {
        try {
            val isTendered = sessionManager.getGeneralSetting()?.is_tendered == true
            val isInventory = sessionManager.getGeneralSetting()?.is_inventory == true

            var bill: TblBillingResponse? = null
            if (billNo != "--") {
                apiService.resetDue(
                    billNo,
                    sessionManager.getCompanyCode() ?: ""
                )
                bill = apiService.getPaymentByBillNo(billNo, sessionManager.getCompanyCode() ?: "")
                    .body()!!
            }

            if (paymentMethod.name == "DUE" && customer.customer_id == 1L)
                return@flow emit(Result.failure(Exception("Please select a customer for due payment")))

            val tenant = sessionManager.getCompanyCode() ?: ""

            // total already includes otherCharges from ViewModel

            val billNumber = when {
                paymentMethod.name == "DUE" || voucherType == "DUE" || (receivedAmt+discount) < total -> apiService.getBillNoByCounterId(
                    sessionManager.getUser()?.counter_id!!, "DUE", tenant
                )

                else -> apiService.getBillNoByCounterId(
                    sessionManager.getUser()?.counter_id!!, "BILL", tenant
                )
            }

            val voucher = apiService.getVoucherByCounterId(
                sessionManager.getUser()?.counter_id!!,
                tenant,
                if (paymentMethod.name == "DUE" || voucherType == "DUE" || (receivedAmt+discount) < total) "DUE" else "BILL"
            ).body()

            val orderResponse = apiService.getOpenOrderDetailsForTable(orderMasterId, tenant)
            if (!orderResponse.isSuccessful) return@flow emit(Result.failure(Exception("Failed to load order details")))

            val order = orderResponse.body() ?: emptyList()


            val request = TblBillingRequest(
                bill_no = billNumber["bill_no"] ?: "",
                bill_date = getCurrentDateModern(),
                bill_create_time = getCurrentTimeModern(),
                order_master_id = orderMasterId,
                voucher_id = voucher?.voucher_id ?: 0L,
                staff_id = sessionManager.getUser()?.staff_id ?: 0L,
                customer_id = bill?.customer?.customer_id ?: customer.customer_id,
                cust_contact_no = bill?.cust_contact_no ?: customer.contact_no,
                cust_address = bill?.cust_address ?: customer.address,
                order_amt = order.sumOf { it.total },
                tax_amt = order.sumOf { it.tax_amount },
                cess = order.sumOf { it.cess },
                cess_specific = order.sumOf { it.cess_specific },
                grand_total = total,
                cash = if (paymentMethod.name == "CASH") receivedAmt else totals.first,
                card = if (paymentMethod.name == "CARD") receivedAmt else totals.second,
                upi = if (paymentMethod.name == "UPI") receivedAmt else totals.third,
                due = if (paymentMethod.name == "DUE") receivedAmt else if (voucherType == "DUE" || (receivedAmt+discount) < total) total - receivedAmt else 0.0,
                received_amt = if (paymentMethod.name == "DUE") 0.0 else receivedAmt,
                pending_amt = if (paymentMethod.name == "DUE") receivedAmt else if (voucherType == "DUE" || receivedAmt < total) total - receivedAmt else 0.0,
                note = "",
                is_active = 1L,
                disc_amt = discount,
                delivery_amt = otherCharges,
                round_off = roundOff,
                rounded_amt = total,
                others = if (paymentMethod.name == "ONLINE") total else 0.0,
                change = if (isTendered && tenderedAmt > 0) tenderedAmt - receivedAmt else 0.0,
                tendered_amt = tenderedAmt,
                upi_type_id = upiTypeId
            )


            val check = apiService.checkBillExists(orderMasterId, tenant)
            if (check.body()?.data != true) return@flow emit(Result.failure(Exception("Order already billed or invalid")))

            val response = apiService.addPayment(request, tenant)
            if (!response.isSuccessful) return@flow emit(Result.failure(Exception(response.message())))

            val result = response.body()!!
            apiService.updateTableAvailability(result.order_master.table_id, "AVAILABLE", tenant)
            apiService.updateOrderStatus(orderMasterId, "COMPLETED", tenant)

            if (customer.igst_status)
                apiService.updateIgstForOrderDetails(orderMasterId, tenant)
            else
                apiService.updateGstForOrderDetails(orderMasterId, tenant)

            apiService.createAuditing(
                TblAuditingRequest(
                    id = 5,
                    modify_date = getCurrentDateModern(),
                    modify_time = getCurrentTimeModern(),
                    groups = "NEW",
                    counter_id = sessionManager.getUser()?.counter_id ?: 0,
                    user_id = sessionManager.getUser()?.staff_id ?: 0,
                    created_date = getCurrentDateModern(),
                    member = "${voucher?.voucher_id ?: 0L}",
                    member_id = billNumber["bill_no"] ?: "",
                    narration = "BILL-${billNumber["bill_no"] ?: ""}",
                    credit = response.body()?.grand_total ?: 0.0,
                    debit = 0.0
                ),
                sessionManager.getCompanyCode() ?: ""
            )

            if (isInventory) {
                val led = apiService.getLedgerByName("ONLINE",sessionManager.getCompanyCode()?:"").body()
                order.forEach { it->
                    val conv = apiService.getUnitConversionByItemId(it.menuItem.menu_item_id, tenant).body()
                    if (conv!= null){
                        apiService.updateStockMinus(conv.base_item.menu_item_id,
                            ((conv.conversion_no ) * it.qty), tenant)
                    }
                }
                val itemDetails = order.map { it->
                    val conv = apiService.getUnitConversionByItemId(it.menuItem.menu_item_id, tenant).body()
                    if (conv!= null){
                        TblItemDetailsRequest(
                            item_id = conv.base_item.menu_item_id,
                            godown_id = 1, // Default godown
                            party_member = "${voucher?.voucher_id ?: 0L}",
                            party_id = "5",
                            bill_no = result.bill_no,
                            date = result.bill_date,
                            member = when(paymentMethod.name){
                                "CASH" -> "1"
                                "CARD" -> "2"
                                "UPI" -> "3"
                                "OTHERS" -> "${led?.ledger_id?.toLong() ?: 28}"
                                else -> ""
                            },
                            member_id = result.bill_no,
                            bag_per_amt = 0.0,
                            bag_in = 0.0,
                            bag_out = ((conv.conversion_no) * it.qty.toDouble()),
                            weight_in = 0.0,
                            weight_out = 0.0,
                            amount_in = 0.0,
                            amount_out = it.total
                        )
                    }else{
                        TblItemDetailsRequest(
                            item_id = it.menuItem.menu_item_id,
                            godown_id = 1, // Default godown
                            party_member = "${voucher?.voucher_id ?: 0L}",
                            party_id = "5",
                            bill_no = result.bill_no,
                            date = result.bill_date,
                            member = when(paymentMethod.name){
                                "CASH" -> "1"
                                "CARD" -> "2"
                                "UPI" -> "3"
                                "OTHERS" -> "${led?.ledger_id?.toLong() ?: 28}"
                                else -> ""
                            },
                            member_id = result.bill_no,
                            bag_per_amt = 0.0,
                            bag_in = 0.0,
                            bag_out = it.qty.toDouble(),
                            weight_in = 0.0,
                            weight_out = 0.0,
                            amount_in = 0.0,
                            amount_out = it.total
                        )
                    }
                }
                apiService.createBulkItemDetails(itemDetails, tenant)
                apiService.deleteByOrderId(orderMasterId,sessionManager.getCompanyCode() ?: "")
            }

            if (sessionManager.getGeneralSetting()?.is_accounts == true) {
                val ledgerDetail = apiService.findByContactNo(
                    bill?.customer?.contact_no ?: customer.contact_no,
                    tenant
                ).body()
                val onlineLedger = apiService.getLedgerByName("ONLINE",tenant).body()
                val ledger: TblLedgerDetails? = if (paymentMethod.name == "DUE") {
                    val req = TblLedgerRequest(
                        ledger_name = customer.customer_name,
                        ledger_fullname = customer.customer_name,
                        group_id = 17,
                        address = customer.address,
                        contact_no = customer.contact_no,
                        email = customer.email_address,
                        gst_no = customer.gst_no,
                        igst_status = if (customer.igst_status) "YES" else "NO",
                        due_date = getCurrentDateModern(),
                        order_by = 0,
                        address1 = "",
                        place = "",
                        pincode = 0,
                        country = "",
                        pan_no = "",
                        state_code = "",
                        state_name = "",
                        sac_code = "",
                        opening_balance = "",
                        bank_details = "NO",
                        tamil_text = "",
                        distance = 0.0,
                        is_default = false
                    )
                    if (ledgerDetail?.contact_no != customer.contact_no)
                        apiService.createLedger(req, tenant).body()
                    else
                        null
                } else null
                val ledgerEntries = LedgerEntryBuilder.build(
                    paymentMethod,
                    voucherType,
                    voucher,
                    ledgerDetail,
                    ledger,
                    if (billNo!="--") billNo else result.bill_no,
                    receivedAmt,
                    totals,
                    onlineLedger
                )
                apiService.insertSingleLedgerDetails(ledgerEntries, tenant)
            }

            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    @SuppressLint("SupportAnnotationUsage", "SuspiciousIndentation")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBill(bill: Bill, ipAddress: String,applicationContext: Context): Flow<Result<String>> = flow {
        try {
            val response = apiService.printReceipt(bill, sessionManager.getCompanyCode() ?: "")
            if (!response.isSuccessful) return@flow emit(Result.failure(Exception("Print failed")))
//            val dummyEodReportRequest = EodReportRequest(
//                companyCode = "KTS-RESB",
//                fromDate = "2026-04-01",
//                toDate = "2026-04-01",
//                paymentSummary = listOf(
//                    PaymentSummaryRow("CASH", 45, 18500.00),
//                    PaymentSummaryRow("CARD", 28, 14250.50),
//                    PaymentSummaryRow("UPI", 62, 27890.75),
//                    PaymentSummaryRow("CREDIT", 10, 5200.00)
//                ),
//                onlineOrderSummary = listOf(
//                    OnlineOrderSummaryRow("SWIGGY", 18, 9650.00),
//                    OnlineOrderSummaryRow("ZOMATO", 22, 11840.50),
//                    OnlineOrderSummaryRow("WEBSITE", 6, 3150.00)
//                ),
//                salesDetails = SalesDetails(
//                    itemTotal = 78500.00,
//                    itemDiscount = 2500.00,
//                    itemTax = 7050.00,
//                    billDiscount = 1200.00,
//                    charges = 350.00,
//                    roundOff = -0.25,
//                    grandTotal = 82200.75
//                ),
//                groupSales = listOf(
//                    GroupSalesRow("Beverages", 85, 12750.00),
//                    GroupSalesRow("Starters", 64, 18420.50),
//                    GroupSalesRow("Main Course", 102, 35210.00),
//                    GroupSalesRow("Desserts", 38, 6120.25)
//                ),
//                cashSummary = listOf(
//                    CashSummaryRow("Opening Cash", 5000.00),
//                    CashSummaryRow("Cash Sales", 18500.00),
//                    CashSummaryRow("Cash In", 2000.00),
//                    CashSummaryRow("Cash Out", 1500.00),
//                    CashSummaryRow("Closing Cash", 24000.00)
//                ),
//                expense = listOf(
//                    ExpenseRow("Petty Cash Expense", 750.00),
//                    ExpenseRow("Staff Food", 450.00),
//                    ExpenseRow("Transport", 300.00)
//                ),
//                billCancelDetails = BillCancelDetails(
//                    count = 3,
//                    total = 1890.50
//                ),
//                dineTypeSummary = listOf(
//                    DineTypeSummaryRow("DINE IN", 58, 34250.00),
//                    DineTypeSummaryRow("TAKEAWAY", 49, 22340.25),
//                    DineTypeSummaryRow("DELIVERY", 31, 15610.50)
//                ),
//                footerMessage = "Thank You! Visit Again"
//            )
//            val res = apiService.printEOD(bill.paperWidth,dummyEodReportRequest, sessionManager.getCompanyCode() ?: "")
//            val byte = res.body()?.bytes() ?: return@flow emit(Result.failure(Exception("Empty print data")))

            val bytes = response.body()?.bytes()
                ?: return@flow emit(Result.failure(Exception("Empty print data")))
            var msg = ""
            val printerType = sessionManager.getPrinterType()
            if (printerType == "BT" && sessionManager.getBluetoothPrinter() != null) {
                printerHelper.printViaBluetoothMac(
                    data = bytes,
                    macAddress = sessionManager.getBluetoothPrinter().toString()
                ) { _, m -> msg = m }
//                printerHelper.printViaBluetoothMac(
//                    data = byte,
//                    macAddress = sessionManager.getBluetoothPrinter().toString()
//                ) { _, m -> msg = m }
            }
            else if (printerType == "TCP") {
                printerHelper.printViaTcp(ipAddress, data = bytes) { _, m -> msg = m }
//                printerHelper.printViaTcp(ipAddress, data = byte) { _, m -> msg = m }
            }
            else if (printerType == "POS"){

                SrPrinter.getInstance(applicationContext).printEpson(bytes)
            }
            else if (printerType == "USB"){
                printerHelper.printViaUsb(applicationContext,bytes)
            }
            else
                emit(Result.failure(Exception("Printer not configured")))
            emit(Result.success(msg))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    @SuppressLint("SupportAnnotationUsage", "SuspiciousIndentation")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printEOD(fromDate: String,toDate: String,paperWidth: Int): Flow<Result<String>> = flow{
        try{
            val response = apiService.printEOD(paperWidth,fromDate,toDate, sessionManager.getCompanyCode() ?: "")
            if(!response.isSuccessful) return@flow emit(Result.failure(Exception("Print failed")))
            val bytes = response.body()?.bytes()
                ?: return@flow emit(Result.failure(Exception("Empty print data")))
            var msg = ""
            val printerType = sessionManager.getPrinterType()
            if (printerType == "BT" && sessionManager.getBluetoothPrinter() != null) {
                printerHelper.printViaBluetoothMac(
                    data = bytes,
                    macAddress = sessionManager.getBluetoothPrinter().toString()
                ) { _, m -> msg = m }
            }
            else if (printerType == "TCP") {
              val res =  apiService.getIpAddresss("COUNTER",sessionManager.getCompanyCode() ?: "").body()
                printerHelper.printViaTcp(res?.printerIpAddress?:"", data = bytes) { _, m -> msg = m }
            }
            else if (printerType == "InBuilt"){

                printerHelper.printViaInBuilt(bytes)
            }
            else
                emit(Result.failure(Exception("Printer not configured")))
            emit(Result.success(msg))
        }catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBillWithLocalTemplate(bill: Bill, ipAddress: String): Flow<Result<String>> = flow {
        try {
            val target = sessionManager.getPrinterType()
            val address = if (target == "BLUETOOTH") sessionManager.getBluetoothPrinter().toString() else ipAddress

            printerHelper.printBillWithTemplate(bill, "BILL", target, address)
            emit(Result.success("Print successful"))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun fetchBillPreview(bill: Bill): Bitmap? = try {
        val res = apiService.getBillPreview(bill, sessionManager.getCompanyCode() ?: "")
        if (res.isSuccessful) {
            val bytes = res.body()?.bytes()
            BitmapFactory.decodeByteArray(bytes, 0, bytes!!.size)
        } else {
            null
        }
    } catch (_: Exception) {
        null
    }

    suspend fun getPaymentByBillNo(billNo: String): TblBillingResponse? {
        val response = apiService.getPaymentByBillNo(billNo, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful) {
            response.body()
        } else null
    }

    suspend fun updateBill(billNo: String, orderMasterId: String, request: TblBillingRequest) {
        val bill =
            apiService.getPaymentByBillNo(billNo, sessionManager.getCompanyCode() ?: "").body()!!
        var order: List<TblOrderDetailsResponse> = emptyList()
        val orderMaster = apiService.getOpenOrderDetailsForTable(
            orderMasterId,
            sessionManager.getCompanyCode() ?: ""
        )
        if (orderMaster.isSuccessful) {
            order = orderMaster.body()!!
        }
        val isTendered = sessionManager.getGeneralSetting()?.is_tendered == true
        val request = TblBillingRequest(
            bill_no = billNo,
            bill_date = bill.bill_date,
            bill_create_time = bill.bill_create_time,
            order_master_id = orderMasterId,
            voucher_id = bill.voucher.voucher_id,
            staff_id = bill.staff.staff_id,
            customer_id = bill.customer.customer_id,
            cust_contact_no = bill.cust_contact_no,
            cust_address = bill.cust_address,
            order_amt = order.sumOf { it.total },
            disc_amt = 0.0,
            tax_amt = order.sumOf { it.tax_amount },
            cess = order.sumOf { it.cess },
            cess_specific = order.sumOf { it.cess_specific },
            delivery_amt = bill.delivery_amt,
            grand_total = order.sumOf { it.grand_total },
            round_off = bill.round_off,
            rounded_amt = bill.rounded_amt,
            cash = if (bill.cash > 0.0) order.sumOf { it.grand_total } else 0.0,
            card = if (bill.card > 0.0) order.sumOf { it.grand_total } else 0.0,
            upi = if (bill.upi > 0.0) order.sumOf { it.grand_total } else 0.0,
            due = if (bill.due > 0.0) order.sumOf { it.grand_total } else 0.0,
            others = bill.others,
            received_amt = if (bill.due > 0.0) 0.0 else order.sumOf { it.grand_total },
            pending_amt = if (bill.due > 0.0) order.sumOf { it.grand_total } else 0.0,
//          change = if (paymentMethod.name == "CASH") receivedAmt - orderMaster.sumOf { it.grand_total } else 0.0,
            change = bill.change,
            note = bill.note,
            is_active = 1L,
            tendered_amt = bill.tendered_amt,
            upi_type_id = bill.upi_type.upi_type_id
        )
        if (sessionManager.getGeneralSetting()?.is_accounts == true) {
            val ledgerDetails = apiService.getLedgerDetailsByEntryNo(
                billNo, sessionManager.getCompanyCode() ?: ""
            ).body()!!.filter { it.ledger_details_id.toInt() % 2 != 0 }
            val ledgerRequest = ledgerDetails.map {
                TblLedgerDetailIdRequest(
                    ledger_details_id = it.ledger_details_id,
                    id = it.ledger.ledger_id.toLong(),
                    bill_no = billNo,
                    date = it.date,
                    time = it.time,
                    party_member = it.party_member,
                    party_id = it.party.ledger_id.toLong(),
                    member = it.member,
                    member_id = it.member_id,
                    purpose = it.purpose,
                    amount_in = request.grand_total,
                    amount_out = 0.0
                )
            }
            apiService.updateAllLedgerDetails(ledgerRequest, sessionManager.getCompanyCode() ?: "")
        }
        apiService.createAuditing(
            TblAuditingRequest(
                id = 5,
                modify_date = getCurrentDateModern(),
                modify_time = getCurrentTimeModern(),
                groups = "MODIFY",
                counter_id = sessionManager.getUser()?.counter_id ?: 0,
                user_id = sessionManager.getUser()?.staff_id ?: 0,
                created_date = bill.bill_date,
                member = "${bill.voucher.voucher_id}",
                member_id = bill.bill_no,
                narration = "BILL-${bill.bill_no}",
                credit = if (bill.grand_total < request.grand_total) request.grand_total - bill.grand_total else 0.0,
                debit = if (bill.grand_total > request.grand_total) bill.grand_total - request.grand_total else 0.0
            ),
            sessionManager.getCompanyCode() ?: ""
        )
        apiService.updateByBillNo(billNo, request, sessionManager.getCompanyCode() ?: "")

    }

    suspend fun deleteBill(billNo: String): Int? {
        val bill =
            apiService.getPaymentByBillNo(billNo, sessionManager.getCompanyCode() ?: "").body()!!
        val response = apiService.deleteByBillNo(billNo, sessionManager.getCompanyCode() ?: "")
        apiService.createAuditing(
            TblAuditingRequest(
                id = 5,
                modify_date = getCurrentDateModern(),
                modify_time = getCurrentTimeModern(),
                groups = "DELETE",
                counter_id = sessionManager.getUser()?.counter_id ?: 0,
                user_id = sessionManager.getUser()?.staff_id ?: 0,
                created_date = bill.bill_date,
                member = "${bill.voucher.voucher_id}",
                member_id = bill.bill_no,
                narration = "BILL-${bill.bill_no}",
                credit = 0.0,
                debit = 0.0
            ),
            sessionManager.getCompanyCode() ?: ""
        )
        return if (response.isSuccessful) {
            response.body()
        } else
            null
    }

    suspend fun getYearlySummary(year: String): Flow<Result<List<BillingSummary>>> = flow {
        try {
            val response = apiService.getYearlySummary(sessionManager.getCompanyCode() ?: "", year)
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to fetch yearly summary")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun getMonthlySummary(month: String, year: String): Flow<Result<List<TblBillingResponse>>> = flow {
        try {
            val response = apiService.getMonthlySummary(sessionManager.getCompanyCode() ?: "", month, year)
            if (response.isSuccessful) {
                emit(Result.success(response.body() ?: emptyList()))
            } else {
                emit(Result.failure(Exception("Failed to fetch monthly summary")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
}

object LedgerEntryBuilder {
    fun build(
        paymentMethod: PaymentMethod,
        voucherType: String,
        voucher: TblVoucherResponse?,
        ledgerDetail: TblLedgerDetails?,
        ledger: TblLedgerDetails?,
        billNo: String,
        receivedAmt: Double,
        totals: Triple<Double, Double, Double>,
        onlineLedger: TblLedgerDetails?
    ): List<TblLedgerDetailIdRequest> {
        val (cash, card, upi) = totals
        val base = TblLedgerDetailIdRequest(
            id = if (voucherType == "DUE") ledgerDetail?.ledger_id?.toLong() ?: 0 else 5,
            bill_no = billNo,
            date = getCurrentDateModern(),
            time = getCurrentTimeModern(),
            party_member = voucher?.voucher_name ?: "",
            member = voucher?.voucher_id.toString(),
            member_id = billNo,
            purpose = "",
            amount_in = 0.0,
            amount_out = 0.0,
            party_id = 0
        )
        return when (paymentMethod.name) {
            "CASH" -> listOf(
                base.copy(
                    party_id = 1,
                    purpose = if (voucherType == "DUE") "CREDIT AMOUNT TO ${ledgerDetail?.ledger_name}" else "SALES BY CASH",
                    amount_in = receivedAmt

                ),
                base.copy(
                    id = 1,
                    party_id = if (voucherType == "DUE") ledgerDetail?.ledger_id?.toLong()
                        ?: 0 else 5,
                    purpose = if (voucherType == "DUE") "CREDIT AMOUNT TO ${ledgerDetail?.ledger_name}" else "SALES BY CASH",
                    amount_out = receivedAmt
                )
            )

            "CARD" -> listOf(
                base.copy(
                    party_id = 2,
                    purpose = if (voucherType == "DUE") "SALES CREDIT AMOUNT BY CARD" else "SALES BY CARD",
                    amount_in = receivedAmt
                ),
                base.copy(
                    id = 2,
                    party_id = if (voucherType == "DUE") ledgerDetail?.ledger_id?.toLong()
                        ?: 0 else 5,
                    purpose = if (voucherType == "DUE") "SALES CREDIT AMOUNT BY CARD" else "SALES BY CARD",
                    amount_out = receivedAmt
                )
            )

            "UPI" -> listOf(
                base.copy(
                    party_id = 3,
                    purpose = if (voucherType == "DUE") "SALES CREDIT AMOUNT BY UPI" else "SALES BY UPI",
                    amount_in = receivedAmt
                ),
                base.copy(
                    id = 3,
                    party_id = if (voucherType == "DUE") ledgerDetail?.ledger_id?.toLong()
                        ?: 0 else 5,
                    purpose = if (voucherType == "DUE") "SALES CREDIT AMOUNT BY UPI" else "SALES BY UPI",
                    amount_out = receivedAmt
                )
            )

            "DUE" -> listOf(
                base.copy(
                    party_id = ledger?.ledger_id?.toLong() ?: (ledgerDetail?.ledger_id?.toLong()
                        ?: 0),
                    purpose = "SALES BY DUE",
                    amount_in = receivedAmt
                )
            )

            "CASH/CARD" ->
                if (voucherType == "DUE") {
                    listOfNotNull(
                        base.copy(
                            party_id = 1,
                            purpose = "SALES CREDIT AMOUNT BY CASH",
                            amount_in = cash + card + upi
                        ),
                        if (cash > 0.0)
                            base.copy(
                                id = 1,
                                party_id = ledger?.ledger_id?.toLong() ?: 0,
                                purpose = "SALES CREDIT AMOUNT BY CASH",
                                amount_out = cash
                            ) else null,
                        if (card > 0.0)
                            base.copy(
                                id = 2,
                                party_id = ledger?.ledger_id?.toLong() ?: 0,
                                purpose = "SALES CREDIT AMOUNT BY CARD",
                                amount_out = card
                            ) else null,
                        if (upi > 0.0)
                            base.copy(
                                id = 3,
                                party_id = ledger?.ledger_id?.toLong() ?: 0,
                                purpose = "SALES CREDIT AMOUNT BY UPI",
                                amount_out = upi
                            ) else null
                    )
                } else {
                    listOfNotNull(
                        base.copy(
                            party_id = 1,
                            purpose = "SALES BY CASH",
                            amount_in = cash + card + upi
                        ),
                        if (cash > 0.0)
                            base.copy(
                                id = 1,
                                party_id = 5,
                                purpose = "SALES BY CASH",
                                amount_out = cash
                            )
                        else null,
                        if (card > 0.0)
                            base.copy(
                                id = 2,
                                party_id = 5,
                                purpose = "SALES BY CARD",
                                amount_out = card
                            ) else null,
                        if (upi > 0.0)
                            base.copy(
                                id = 3,
                                party_id = 5,
                                purpose = "SALES BY UPI",
                                amount_out = upi
                            ) else null
                    )
                }
            "ONLINE"->listOf(
                base.copy(
                    party_id = onlineLedger?.ledger_id?.toLong() ?: 0,
                    purpose = "SALES BY ONLINE",
                    amount_in = receivedAmt
                ),
                base.copy(
                    id = onlineLedger?.ledger_id?.toLong()?:0,
                    party_id = 5,
                    purpose = "SALES BY ONLINE",
                    amount_out = receivedAmt
                )
            )

            else -> listOf(
                base.copy(
                    party_id = 1,
                    purpose = "SALES BY CASH",
                    amount_in = receivedAmt
                )
            )
        }
    }
}
