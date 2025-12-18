package com.warriortech.resb.model

import java.util.Date

data class PaidBill(
    val id: Long,
    val billNo: String,
    val orderMasterId: Long,
    val customerName: String,
    val customerPhone: String,
    val tableNo: String,
    val items: List<BillItem>,
    val subtotal: Double,
    val taxAmount: Double,
    val discountAmount: Double,
    val totalAmount: Double,
    val paymentMethod: String,
    val paymentDate: Date,
    val status: String = "PAID",
    val isRefunded: Boolean = false,
    val refundAmount: Double = 0.0,
    val notes: String? = null
)

data class PaidBillSummary(
    val id: Long,
    val billNo: String,
    val customerName: String,
    val totalAmount: Double,
    val paymentDate: Date,
    val paymentMethod: String,
    val status: String
)
