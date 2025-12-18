package com.warriortech.resb.model

import androidx.compose.ui.graphics.Color

data class PaymentModeData(
    val paymentMode: String,
    val amount: Double,
    val color: Color
)

data class PaymentModeDataResponse(
    val paymentMode: String,
    val amount: Double,
    val color: Long
)

data class WeeklySalesData(
    val date: String,
    val amount: Double
)

data class DashboardChartData(
    val paymentModeData: List<PaymentModeData>,
    val weeklySalesData: List<WeeklySalesData>
)
