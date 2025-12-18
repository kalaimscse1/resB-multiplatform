package com.warriortech.resb.data.repository

import android.Manifest
import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.annotation.RequiresPermission
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
        total: Double = 0.0
    ): Flow<Result<TblBillingResponse>> = flow {
        try {
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

            val ledgerDetail = apiService.findByContactNo(
                bill?.customer?.contact_no ?: customer.contact_no,
                tenant
            ).body()
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
                    order_by = 0, address1 = "", place = "", pincode = 0, country = "", pan_no = "",
                    state_code = "", state_name = "", sac_code = "", opening_balance = "",
                    bank_details = "NO", tamil_text = "", distance = 0.0, is_default = false
                )
                if (ledgerDetail?.contact_no != customer.contact_no)
                    apiService.createLedger(req, tenant).body()
                else
                    null
            } else null

            val billNumber = when {
                paymentMethod.name == "DUE" || voucherType == "DUE" || receivedAmt < total-> apiService.getBillNoByCounterId(
                    sessionManager.getUser()?.counter_id!!, "DUE", tenant
                )

                else -> apiService.getBillNoByCounterId(
                    sessionManager.getUser()?.counter_id!!, "BILL", tenant
                )
            }

            val voucher = apiService.getVoucherByCounterId(
                sessionManager.getUser()?.counter_id!!,
                tenant,
                if (paymentMethod.name == "DUE" || voucherType == "DUE" || receivedAmt < total) "DUE" else "BILL"
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
                order_amt = order.sumOf { it.total },
                tax_amt = order.sumOf { it.tax_amount },
                cess = order.sumOf { it.cess },
                cess_specific = order.sumOf { it.cess_specific },
                grand_total = order.sumOf { it.grand_total },
                cash = if (paymentMethod.name == "CASH") receivedAmt else totals.first,
                card = if (paymentMethod.name == "CARD") receivedAmt else totals.second,
                upi = if (paymentMethod.name == "UPI") receivedAmt else totals.third,
                due = if (paymentMethod.name == "DUE") receivedAmt else if (voucherType == "DUE" || receivedAmt < total) total - receivedAmt else 0.0,
                received_amt = if (paymentMethod.name == "DUE") 0.0 else receivedAmt,
                pending_amt = if (paymentMethod.name == "DUE") receivedAmt else if (voucherType == "DUE" || receivedAmt < total) total - receivedAmt else 0.0,
                note = "",
                is_active = 1L,
                disc_amt = 0.0,
                delivery_amt = 0.0,
                round_off = 0.0,
                rounded_amt = order.sumOf { it.grand_total },
                others = 0.0,
                change = 0.0
            )

            val ledgerEntries = LedgerEntryBuilder.build(
                paymentMethod,
                voucherType,
                voucher,
                ledgerDetail,
                ledger,
                billNumber["bill_no"] ?: "",
                receivedAmt,
                totals
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

            apiService.insertSingleLedgerDetails(ledgerEntries, tenant)
            emit(Result.success(result))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    @SuppressLint("SupportAnnotationUsage", "SuspiciousIndentation")
    @RequiresPermission(Manifest.permission.BLUETOOTH_CONNECT)
    fun printBill(bill: Bill, ipAddress: String): Flow<Result<String>> = flow {
        try {
            val response = apiService.printReceipt(bill, sessionManager.getCompanyCode() ?: "")
            if (!response.isSuccessful) return@flow emit(Result.failure(Exception("Print failed")))

            val bytes = response.body()?.bytes()
                ?: return@flow emit(Result.failure(Exception("Empty print data")))
            var msg = ""
            if (sessionManager.getBluetoothPrinter() != null)
                printerHelper.printViaBluetoothMac(
                    data = bytes,
                    macAddress = sessionManager.getBluetoothPrinter().toString()
                ) { _, m -> msg = m }
            else
                printerHelper.printViaTcp(ipAddress, data = bytes) { _, m -> msg = m }
            emit(Result.success(msg))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }

    suspend fun fetchBillPreview(bill: Bill): Bitmap? = try {
        val res = apiService.getBillPreview(bill, sessionManager.getCompanyCode() ?: "")
        if (res.isSuccessful) res.body()?.byteStream()
            ?.use { BitmapFactory.decodeStream(it) } else null
    } catch (_: Exception) {
        null
    }

    suspend fun getPaymentByBillNo(billNo: String): TblBillingResponse? {
        val response = apiService.getPaymentByBillNo(billNo, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful) {
            response.body()
        } else null
    }

    suspend fun updateBill(billNo: String, orderMasterId: String) {
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
        val request = TblBillingRequest(
            bill_no = billNo,
            bill_date = bill.bill_date,
            bill_create_time = bill.bill_create_time,
            order_master_id = orderMasterId,
            voucher_id = bill.voucher.voucher_id,
            staff_id = bill.staff.staff_id,
            customer_id = bill.customer.customer_id,
            order_amt = order.sumOf { it.total },
            disc_amt = 0.0,
            tax_amt = order.sumOf { it.tax_amount },
            cess = order.sumOf { it.cess },
            cess_specific = order.sumOf { it.cess_specific },
            delivery_amt = 0.0,
            grand_total = order.sumOf { it.grand_total },
            round_off = 0.0,
            rounded_amt = order.sumOf { it.grand_total },
            cash = if (bill.cash > 0.0) order.sumOf { it.grand_total } else 0.0,
            card = if (bill.card > 0.0) order.sumOf { it.grand_total } else 0.0,
            upi = if (bill.upi > 0.0) order.sumOf { it.grand_total } else 0.0,
            due = if (bill.due > 0.0) order.sumOf { it.grand_total } else 0.0,
            others = 0.0,
            received_amt = if (bill.due > 0.0) 0.0 else order.sumOf { it.grand_total },
            pending_amt = if (bill.due > 0.0) order.sumOf { it.grand_total } else 0.0,
//          change = if (paymentMethod.name == "CASH") receivedAmt - orderMaster.sumOf { it.grand_total } else 0.0,
            change = 0.0,
            note = "",
            is_active = 1L
        )
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
        apiService.updateByBillNo(billNo, request, sessionManager.getCompanyCode() ?: "")

    }

    suspend fun deleteBill(billNo: String): Int? {
        val response = apiService.deleteByBillNo(billNo, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful) {
            response.body()
        } else
            null
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
        totals: Triple<Double, Double, Double>
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

            "OTHERS" ->
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
