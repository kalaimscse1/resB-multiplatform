package com.warriortech.resb.model

data class TblBillingRequest(
    var bill_no: String,
    var bill_date: String,
    var bill_create_time: String,
    var order_master_id: String,
    var voucher_id: Long,
    var staff_id: Long,
    var customer_id: Long,
    var cust_contact_no: String,
    var cust_address: String,
    var order_amt: Double,
    var disc_amt: Double,
    var tax_amt: Double,
    var cess: Double,
    var cess_specific: Double,
    var delivery_amt: Double,
    var grand_total: Double,
    var round_off: Double,
    var rounded_amt: Double,
    var cash: Double,
    var card: Double,
    var upi: Double,
    var due: Double,
    var others: Double,
    var received_amt: Double,
    var pending_amt: Double,
    var change: Double,
    var note: String,
    var is_active: Long,
    var tendered_amt:Double,
    var upi_type_id:Long,
)

data class TblBillingResponse(
    var bill_no: String,
    var bill_date: String,
    var bill_create_time: String,
    var order_master: TblOrderResponse,
    var voucher: TblVoucher,
    var staff: TblStaff,
    var customer: TblCustomer,
    var cust_contact_no: String,
    var cust_address: String,
    var order_amt: Double,
    var disc_amt: Double,
    var tax_amt: Double,
    var cess: Double,
    var cess_specific: Double,
    var delivery_amt: Double,
    var grand_total: Double,
    var round_off: Double,
    var rounded_amt: Double,
    var cash: Double,
    var card: Double,
    var upi: Double,
    var due: Double,
    var others: Double,
    var received_amt: Double,
    var pending_amt: Double,
    var change: Double,
    var note: String,
    var is_active: Long,
    var tendered_amt:Double,
    var upi_type: TblUpiType
)

data class BillItem(
    val sn: Int,
    val itemName: String,
    val qty: Int,
    val price: Double,
    val basePrice: Double,
    val amount: Double,
    val taxPercent: Double,
    val taxAmount: Double,
    val sgstPercent: Double,
    val cgstPercent: Double,
    val igstPercent: Double,
    val cessPercent: Double,
    val sgst: Double,
    val cgst: Double,
    val igst: Double,
    val cess: Double,
    val cess_specific: Double,
)

data class Bill(
    val company_code: String,
    val billNo: String,
    val date: String,
    val time: String,
    val orderNo: String,
    val counter: String,
    val tableNo: String,
    val custName: String,
    val custNo: String,
    val custAddress: String,
    val custGstin: String,
    val items: List<BillItem>,
    val subtotal: Double,
    val deliveryCharge: Double,
    val discount: Double,
    val roundOff: Double,
    val total: Double,
    val paperWidth: Int = 48,
    val received_amt: Double,
    val pending_amt: Double
)

data class TblItemDetailsRequest(
    var id: Long=0,
    var item_id:Long,
    var godown_id:Long,
    var party_member:String,
    var party_id:String,
    var bill_no :String,
    var date:String,
    var member:String,
    var member_id:String,
    var bag_per_amt:Double,
    var bag_in:Double,
    var bag_out:Double,
    var weight_in:Double,
    var weight_out:Double,
    var amount_in:Double,
    var amount_out:Double
)

data class TblItemDetailsResponse(
    var id: Long,
    var godown_id: Long,
    var item:TblMenuItemResponse,
    var party_member:String,
    var party_id:String,
    var bill_no :String,
    var date:String,
    var member:String,
    var member_id:String,
    var bag_per_amt:Double,
    var bag_in:Double,
    var bag_out:Double,
    var weight_in:Double,
    var weight_out:Double,
    var amount_in:Double,
    var amount_out:Double
)

data class TblItemMasterResponse(
    var slno: Long,
    var item:TblMenuItemResponse,
    var box:Long,
    var qty:Double,
    var cost_rate:Double,
    var purchase_rate:Double,
    var is_active:Long=1
)
data class PaymentSummaryRow(val paymentMode: String, val count: Int, val amount: Double)

data class OnlineOrderSummaryRow(val orderMode: String, val count: Int, val amount: Double)

data class SalesDetails(val itemTotal: Double, val itemDiscount: Double, val itemTax: Double, val billDiscount: Double, val charges: Double, val roundOff: Double, val grandTotal: Double)

data class GroupSalesRow(val group: String, val qty: Int, val amount: Double)

data class CashSummaryRow(val description: String, val amount: Double)

data class ExpenseRow(val description: String, val amount: Double)

data class BillCancelDetails(val count: Int, val total: Double)

data class DineTypeSummaryRow(val dineType: String, val count: Int, val amount: Double)

data class EodReportRequest(
    val companyCode: String = "",
    val fromDate: String = "",
    val toDate: String = "",
    val paymentSummary: List<PaymentSummaryRow> = emptyList(),
    val onlineOrderSummary: List<OnlineOrderSummaryRow> = emptyList(),
    val salesDetails: SalesDetails = SalesDetails(0.0,0.0,0.0,0.0,0.0,0.0,0.0),
    val groupSales: List<GroupSalesRow> = emptyList(),
    val cashSummary: List<CashSummaryRow> = emptyList(),
    val expense: List<ExpenseRow> = emptyList(),
    val billCancelDetails: BillCancelDetails = BillCancelDetails(0, 0.0),
    val dineTypeSummary: List<DineTypeSummaryRow> = emptyList(),
    val footerMessage: String = "Thank You"
)
