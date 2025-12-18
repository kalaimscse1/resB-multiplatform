// com/warriortech/resb/model/Models.kt
package com.warriortech.resb.model

data class TodaySalesMetrics(
    val totalRevenue: Double,
    val totalOrders: Int,
    val averageOrderValue: Double,
    val taxCollected: Double
)

data class GstRateBreakdown(
    val rate: Double,
    val taxableAmount: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double
)

data class GSTSummaryReport(
    val totalCGST: Double,
    val totalSGST: Double,
    val totalIGST: Double,
    val totalCess: Double,                          // ⬅ added so your card can show CESS
    val gstByRate: Map<String, GstRateBreakdown>
)

data class GstBreakdown(
    val cgst: Double,
    val sgst: Double,
    val igst: Double
) {
    val totalGst: Double get() = cgst + sgst + igst
}

data class HsnSummary(
    val hsnCode: String,
    val description: String,
    val quantity: Double,
    val taxableValue: Double,
    val cgst: Double,
    val sgst: Double,
    val igst: Double,
    val cess: Double,
    val totalTax: Double
)

/**
 * This matches how your screen is reading it:
 * todaySales.gstBreakdown, todaySales.cessTotal, todaySales.hsnSummary
 */
data class TodaySalesReport(
    val totalSales: Double,
    val totalOrders: Int,
    val totalTax: Double,
    val totalCess: Double,
    val salesByHour: Map<String, Double>,

    // extra fields your screen uses:
    val gstBreakdown: GstBreakdown,
    val cessTotal: Double,
    val hsnSummary: List<HsnSummary>
)

/** Unified payload your screen needs */
data class ReportResponse(
    val todaySalesMetrics: TodaySalesMetrics?,
    val gstSummary: GSTSummaryReport?,
    val todaySales: TodaySalesReport?
)

data class SalesReport(
    val orderNo: String,
    val billNo: String,
    val billDate: String,
    val customerName: String,
    val payMode: String,
    val billAmount: Double,
    val discount: Double,
    val receivedAmount: Double
)

data class ItemReport(
    val menu_item_id: Long,
    val menu_item_name: String,
    val item_cat_name: String,
    val rate: Double,
    val tax_id: Long,
    val tax_amount: Double,
    val qty: Int,
    val total: Double,
    val cess: Double,
    val cess_specific: Double,
    val grand_total: Double,
)

data class CategoryReport(
    val item_cat_name: String,
    val qty: Int,
    val grand_total: Double
)
