package com.warriortech.resb.model

import com.google.gson.annotations.SerializedName

data class DashboardMetrics(
    @SerializedName("running_orders")
    val runningOrders: Int,
    @SerializedName("pending_bills")
    val pendingBills: Int,
    @SerializedName("total_sales")
    val totalSales: Double,
    @SerializedName("pending_due")
    val pendingDue: Double
)

data class RunningOrder(
    @SerializedName("order_id")
    val orderId: Long,
    @SerializedName("table_info")
    val tableInfo: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("item_count")
    val itemCount: Int,
    @SerializedName("amount")
    val amount: Double,
    @SerializedName("order_time")
    val orderTime: String
)
