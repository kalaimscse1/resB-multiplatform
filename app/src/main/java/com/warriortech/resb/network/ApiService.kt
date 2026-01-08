package com.warriortech.resb.network

import com.warriortech.resb.model.*
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.*

/**
 * API service interface for network operations
 * Includes endpoints with offline caching policies
 */

interface ApiService {

    /**
     * Authentication
     */

    @GET("companyMaster/checkCompanyCode/{companyCode}")
    suspend fun checkIsBlock(
        @Path("companyCode") companyCode: String
    ): ApiResponse<Boolean>

    @GET("companyMaster/checkExistsOrNotByMailId/{mailId}")
    suspend fun checkIsBlockByMailId(
        @Path("mailId") mailId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): ApiResponse<TblCompanyMaster?>

    @POST("auth/staff")
    suspend fun login(
        @Header("X-Tenant-ID") tenantId: String,
        @Body request: LoginRequest
    ): ApiResponse<AuthResponse>

    @PUT("auth/changePassword/{staff_id}")
    suspend fun changePassword(
        @Path("staff_id") staffId: Long,
        @Body passwordRequest: ChangePasswordRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): ApiResponse<Boolean>

    @Multipart
    @POST("logo/upload/{companyCode}")
    suspend fun uploadLogo(
        @Path("companyCode") companyCode: String,
        @Part file: MultipartBody.Part,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Unit>

    /**
     * Day Close
     */

    @POST("dayClose/create")
    suspend fun addDayClose(
        @Query("staff_id") staffId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response <ApiResponse<DayCloseResponse>>

    /**
     * Dashboard Management
     */

    @GET("dashboard/metrics")
    suspend fun getDashboardMetrics(@Header("X-Tenant-ID") tenantId: String): Response<DashboardMetrics>

    @GET("dashboard/running-orders")
    suspend fun getRunningOrders(@Header("X-Tenant-ID") tenantId: String): Response<List<RunningOrder>>

    @GET("dashboard/recent-activity")
    suspend fun getRecentActivity(@Header("X-Tenant-ID") tenantId: String): Response<List<String>>

    @GET("dashboard/getPayModeAmountApp")
    suspend fun getPayModeAmount(@Header("X-Tenant-ID") tenantId: String): Response<List<PaymentModeDataResponse>>

    @GET("dashboard/getWeeklySales")
    suspend fun getWeeklySales(@Header("X-Tenant-ID") tenantId: String): Response<List<WeeklySalesData>>


    /**
     * Area Management
     */

    @GET("table/area/getAreasByIsActive")
    suspend fun getAllAreas(@Header("X-Tenant-ID") tenantId: String): Response<List<Area>>

    @POST("table/area/addArea")
    suspend fun createArea(
        @Body area: Area,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Area>

    @PUT("table/area/updateAreas/{area_id}")
    suspend fun updateArea(
        @Path("area_id") lng: Long,
        @Body area: Area,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("table/area/deleteAreaById/{area_id}")
    suspend fun deleteArea(
        @Path("area_id") lng: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    /**
     * Table Management
     */

    @GET("table/table/getTablesByIsActive")
    suspend fun getAllTables(@Header("X-Tenant-ID") tenantId: String): Response<List<Table>>

    @GET("table/table/getTablesByActive")
    suspend fun getActiveTables(@Header("X-Tenant-ID") tenantId: String): Response<List<TableStatusResponse>>

    @GET("table/table/getTableByAreaId/{area_id}")
    suspend fun getTablesBySection(
        @Path("area_id") section: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TableStatusResponse>>

    @GET("table/table/getTable/{table_id}")
    suspend fun getTablesByStatus(
        @Path("table_id") tableId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Table

    @PUT("tables/{id}/status")
    suspend fun updateTableStatus(
        @Path("id") tableId: Long,
        @Query("status") status: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Void>

    @POST("table/table/addTable")
    suspend fun createTable(
        @Body table: TblTable,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Table>

    @PUT("table/table/updateTables/{table_id}")
    suspend fun updateTable(
        @Path("table_id") lng: Long,
        @Body table: TblTable,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("table/table/deleteTableById/{table_id}")
    suspend fun deleteTable(
        @Path("table_id") lng: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @GET("table/table/updateTableAvailabilityByTableId/{table_id}")
    suspend fun updateTableAvailability(
        @Path("table_id") tableId: Long,
        @Query("table_availability") status: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("table/table/deleteTableById/{table_id}")
    suspend fun deleteTable(
        @Path("table_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    /**
     * Menu Management
     */

    @GET("menu/getMenusByIsActive")
    suspend fun getAllMenus(@Header("X-Tenant-ID") tenantId: String): Response<List<Menu>>

    @POST("menu/addMenu")
    suspend fun createMenu(
        @Body menu: Menu,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Menu>

    @PUT("menu/updateMenus/{menu_id}")
    suspend fun updateMenu(
        @Path("menu_id") id: Long,
        @Body menu: Menu,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("menu/deleteMenuById/{menu_id}")
    suspend fun deleteMenu(
        @Path("menu_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("menu/getMaxOrderBy")
    suspend fun getOrderBy(@Header("X-Tenant-ID") tenantId: String): Response<Map<String, Long>>

    /**
     * MenuCategory Management
     */

    @GET("menu/itemCategory/getItemCategoryByIsActive")
    suspend fun getAllMenuCategories(@Header("X-Tenant-ID") tenantId: String): Response<List<MenuCategory>>

    @POST("menu/itemCategory/addItemCategory")
    suspend fun createMenuCategory(
        @Body category: MenuCategory,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<MenuCategory>

    @PUT("menu/itemCategory/updateItemCategory/{item_cat_id}")
    suspend fun updateMenuCategory(
        @Path("item_cat_id") id: Long,
        @Body category: MenuCategory,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("menu/itemCategory/deleteItemCategoryById/{item_cat_id}")
    suspend fun deleteMenuCategory(
        @Path("item_cat_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("menu/itemCategory/getMaxOrderBy")
    suspend fun getMenuCategoryOrderBy(@Header("X-Tenant-ID") tenantId: String): Response<Map<String, Long>>

    /**
     * MenuItem Management
     */

    @POST("menu/menuItem/addMenuItem")
    suspend fun createMenuItem(
        @Body menuItem: TblMenuItemRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblMenuItemResponse>

    @PUT("menu/menuItem/updateMenuItems/{menu_item_id}")
    suspend fun updateMenuItem(
        @Path("menu_item_id") id: Long,
        @Body menuItem: TblMenuItemRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("menu/menuItem/deleteMenuItemById/{menu_item_id}")
    suspend fun deleteMenuItem(
        @Path("menu_item_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("menu/menuItem/getMenuItemsByIsActive")
    suspend fun getMenuItems(@Header("X-Tenant-ID") tenantId: String): Response<List<TblMenuItemResponse>>

    @GET("menu/menuItem/getMenuItemsByIsActive")
    suspend fun getAllMenuItems(@Header("X-Tenant-ID") tenantId: String): Response<List<TblMenuItemResponse>>

    @GET("menu/menuItem/search")
    suspend fun searchMenuItems(
        @Query("query") query: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblMenuItemResponse>>

    @GET("menu/menuItem/getMaxOrderBy")
    suspend fun getMenuItemOrderBy(@Header("X-Tenant-ID") tenantId: String): Response<Map<String, Long>>

    /**
     * Order Management
     */

    @POST("order/addOrderByCounterId")
    suspend fun createOrder(
        @Body orderRequest: OrderMaster,
        @Header("X-Tenant-ID") tenantId: String,
        @Query("counterId") counterId: Long,
        @Query("type") type: String
    ): Response<TblOrderResponse>

    @GET("order/getOrder/{order_master_id}")
    suspend fun getOrderMasterById(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblOrderResponse>

    @POST("order/orderDetails/addAllOrderDetails")
    suspend fun createOrderDetails(
        @Body orderRequest: List<OrderDetails>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblOrderDetailsResponse>>

    @GET("order/getOrderByTableId/{table_id}")
    suspend fun getOpenOrderMasterForTable(
        @Path("table_id") tableId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblOrderResponse>

    @GET("order/getOrderNoByCounterId")
    suspend fun getOrderNo(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("counterId") counterId: Long,
        @Query("type") type: String
    ): Map<String, String>

    @GET("order/orderDetails/getKotNO")
    suspend fun getKotNo(@Header("X-Tenant-ID") tenantId: String): Map<String, Int>

    @GET("order/getOrdersByRunning")
    suspend fun getAllOrders(@Header("X-Tenant-ID") tenantId: String): Response<List<TblOrderResponse>>

    @GET("order/getRunningOrderAmount/{order_master_id}")
    suspend fun getRunningOrderAmount(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, Double>>

    @GET("order/getOrderNoForEdit/{table_id}")
    suspend fun getOpenOrderItemsForTable(
        @Path("table_id") tableId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, Int>>

    @GET("order/orderDetails/getOrdersDetailsByOrderIdApp/{order_master_id}")
    suspend fun getOpenOrderDetailsForTable(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblOrderDetailsResponse>>

    @GET("order/orderDetails/getOrdersDetailsByIsActive")
    suspend fun getAllOrderDetails(@Header("X-Tenant-ID") tenantId: String): Response<List<TblOrderDetailsResponse>>

    @POST("print/kot")
    suspend fun printKOT(
        @Body orderRequest: KOTRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @POST("print/bill")
    suspend fun printReceipt(
        @Body bill: Bill,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @POST("print/bill/preview")
    suspend fun getBillPreview(
        @Body bill: Bill,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @POST("print/menuItems/{paperWidth}")
    suspend fun printMenuItems(
        @Path("paperWidth") paperWidth:Int,
        @Body menuItems:List<TblMenuItemResponse>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("order/updateOrderStatusByOrderId/{order_master_id}")
    suspend fun updateOrderStatus(
        @Path("order_master_id") orderId: String,
        @Query("orderStatus") statusUpdate: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @POST("table/table/changeTable")
    suspend fun changeTable(
        @Body tableRequest: ChangeTable,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @POST("order/mergeTable")
    suspend fun mergeTables(
        @Body mergeRequest: MergeTable,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @GET("settings/printer/getPrinterByIpAddress/{kitchen_cat_name}")
    suspend fun getIpAddresss(
        @Path("kitchen_cat_name") kitchenCatName: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<PrintResponse>

    @DELETE("order/orderDetails/deleteOrderDetailsById/{order_details_id}")
    suspend fun deleteOrderDetails(
        @Path("order_details_id") orderDetailsId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @POST("order/orderDetails/updateAllOrderDetails")
    suspend fun updateOrderDetails(
        @Body orderRequest: List<OrderDetails>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblOrderDetailsResponse>>


    /**
     * Settings Management
     */

    /**
     * CounterSettings Management
     */

    @GET("settings/counter/getCounterByIsActive")
    suspend fun getCounters(@Header("X-Tenant-ID") tenantId: String): List<TblCounter>

    @GET("settings/counter/getCounter/{counter_id}")
    suspend fun getCounterById(
        @Path("counter_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): TblCounter

    @POST("settings/counter/addCounter")
    suspend fun createCounter(
        @Body counter: TblCounter,
        @Header("X-Tenant-ID") tenantId: String
    ): TblCounter

    @PUT("settings/counter/updateCounter/{counter_id}")
    suspend fun updateCounter(
        @Path("counter_id") id: Long,
        @Body counter: TblCounter,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("settings/counter/deleteCounterById/{counter_id}")
    suspend fun deleteCounter(
        @Path("counter_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<String>

    /**
     * RoleSettings Management
     */

    @GET("role/getRoleByIsActive")
    suspend fun getRoles(@Header("X-Tenant-ID") tenantId: String): Response<List<Role>>

    @GET("role/{role_id}")
    suspend fun getRoleById(
        @Path("role_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Role>

    @POST("role/addRole")
    suspend fun createRole(
        @Body role: Role,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Role>

    @PUT("role/updateRole/{role_id}")
    suspend fun updateRole(
        @Path("role_id") id: Long,
        @Body role: Role,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("role/deleteRoleById/{role_id}")
    suspend fun deleteRole(
        @Path("role_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    )

    /**
     * TaxSettings Management
     */

    @GET("settings/tax/getTaxByIsActive")
    suspend fun getTaxes(@Header("X-Tenant-ID") tenantId: String): List<Tax>

    @GET("settings/tax/getTax/{tax_id}")
    suspend fun getTaxById(
        @Path("tax_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Tax

    @POST("settings/tax/addTax")
    suspend fun createTax(
        @Body tax: Tax,
        @Header("X-Tenant-ID") tenantId: String
    ): Tax

    @PUT("settings/tax/updateTax/{id}")
    suspend fun updateTax(
        @Path("id") id: Long,
        @Body tax: Tax,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("settings/tax/deleteTaxById/{id}")
    suspend fun deleteTax(
        @Path("id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    )

    /**
     * TaxSplitSettings Management
     */

    @GET("settings/tax/getTaxSplitByIsActive")
    suspend fun getTaxSplits(@Header("X-Tenant-ID") tenantId: String): Response<List<TblTaxSplit>>

    @GET("settings/tax/getTaxSplit/{tax_split_id}")
    suspend fun getTaxSplitById(
        @Path("tax_split_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblTaxSplit>

    @POST("settings/tax/addTaxSplit")
    suspend fun createTaxSplit(
        @Body taxSplit: TaxSplit,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblTaxSplit>

    @PUT("settings/tax/updateTaxSplit/{tax_split_id}")
    suspend fun updateTaxSplit(
        @Path("tax_split_id") id: Long,
        @Body taxSplit: TaxSplit,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("settings/tax/deleteTaxSplitById/{tax_split_id}")
    suspend fun deleteTaxSplit(
        @Path("tax_split_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    )

    @GET("settings/tax/getTaxSplitByTaxId/{tax_id}")
    suspend fun getTaxSplit(
        @Path("tax_id") taxId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): List<TblTaxSplit>

    /**
     * RestaurantProfileSettings Management
     */

    @GET("company/getCompany/{company_code}")
    suspend fun getRestaurantProfile(
        @Path("company_code") companyCode: String,
        @Header("X-Tenant-ID") tenantId: String
    ): RestaurantProfile

    @PUT("company/updateCompany/{company_code}")
    suspend fun updateRestaurantProfile(
        @Path("company_code") companyCode: String,
        @Body profile: RestaurantProfile,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @POST("company/addCompany")
    suspend fun addRestaurantProfile(
        @Body profile: RestaurantProfile,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<RestaurantProfile>

    /**
     * GeneralSettings Management
     */

    @GET("settings/generalSetting/getAllGeneralSetting")
    suspend fun getGeneralSettings(@Header("X-Tenant-ID") tenantId: String): Response<List<GeneralSettings>>

    @PUT("settings/generalSetting/updateSetting/{id}")
    suspend fun updateGeneralSettings(
        @Path("id") id: Long, @Body settings: GeneralSettings,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    /**
     * VoucherSettings Management
     */

    @GET("settings/voucher/getVoucherByIsActive")
    suspend fun getVouchers(@Header("X-Tenant-ID") tenantId: String): List<TblVoucherResponse>

    @GET("settings/voucher/getVoucher/{voucher_id}")
    suspend fun getVoucherById(
        @Path("voucher_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): TblVoucherResponse

    @POST("settings/voucher/addVoucher")
    suspend fun createVoucher(
        @Body voucher: TblVoucherRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): TblVoucherResponse

    @PUT("settings/voucher/updateVoucher/{voucher_id}")
    suspend fun updateVoucher(
        @Path("voucher_id") id: Long,
        @Body voucher: TblVoucherRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("settings/voucher/deleteVoucherById/{voucher_id}")
    suspend fun deleteVoucher(
        @Path("voucher_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @GET("settings/voucher/getVoucherByCounterId/{counter_id}")
    suspend fun getVoucherByCounterId(
        @Path("counter_id") counterId: Long,
        @Header("X-Tenant-ID") tenantId: String,
        @Query("type") type: String
    ): Response<TblVoucherResponse>

    /**
     * VoucherTypeSettings Management
     */

    @GET("settings/voucher/voucherType/getVoucherTypeByIsActive")
    suspend fun getVoucherTypes(
        @Header("X-Tenant-ID") tenantId: String
    ): List<TblVoucherType>

    @GET("settings/voucher/voucherType/getVoucherType/{voucher_Type_id}")
    suspend fun getVoucherTypeById(
        @Path("voucher_Type_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): TblVoucherType

    @POST("settings/voucher/voucherType/addVoucherType")
    suspend fun createVoucherType(
        @Body voucherType: TblVoucherType,
        @Header("X-Tenant-ID") tenantId: String
    ): TblVoucherType

    @PUT("settings/voucher/voucherType/updateVoucherType/{voucher_Type_id}")
    suspend fun updateVoucherType(
        @Path("voucher_Type_id") id: Long,
        @Body voucherType: TblVoucherType,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("settings/voucher/voucherType/deleteVoucherTypeById/{voucher_Type_id}")
    suspend fun deleteVoucherType(
        @Path("voucher_Type_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Int


    /**
     * PrinterSettings Management
     */

    @GET("settings/printer/getPrinterByIsActive")
    suspend fun getPrinters(@Header("X-Tenant-ID") tenantId: String): List<TblPrinterResponse>

    @GET("settings/printer/getPrinter/{printer_id}  ")
    suspend fun getPrinterById(
        @Path("printer_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): TblPrinterResponse

    @POST("settings/printer/addPrinter")
    suspend fun createPrinter(
        @Body printer: Printer,
        @Header("X-Tenant-ID") tenantId: String
    ): TblPrinterResponse

    @PUT("settings/printer/updatePrinter/{printer_id}")
    suspend fun updatePrinter(
        @Path("printer_id") id: Long,
        @Body printer: Printer,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    @DELETE("settings/printer/deletePrinterById/{printer_id}")
    suspend fun deletePrinter(
        @Path("printer_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Int

    /**
     * Payment Management
     */

    @GET("payment/getBillNoByCounterId")
    suspend fun getBillNoByCounterId(
        @Query("counterId") counterId: Long,
        @Query("voucherType") voucherType: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Map<String, String>

    @POST("payment/addPayment")
    suspend fun addPayment(
        @Body paymentRequest: TblBillingRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblBillingResponse>

    @GET("payment/checkExistsOrNotByOrderNoForBilling/{order_master_id}")
    suspend fun checkBillExists(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ApiResponse<Boolean>>

    @GET("payment/getPayment/{bill_no}")
    suspend fun getPaymentByBillNo(
        @Path("bill_no") billNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblBillingResponse>

    @DELETE("payment/resetDue/{bill_no}")
    suspend fun resetDue(
        @Path("bill_no") billNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("order/orderDetails/updateByGst/{order_master_id}")
    suspend fun updateGstForOrderDetails(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("order/orderDetails/updateByIgst/{order_master_id}")
    suspend fun updateIgstForOrderDetails(
        @Path("order_master_id") orderId: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("payment/deleteBillByBillId/{bill_no}")
    suspend fun deleteByBillNo(
        @Path("bill_no") billNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @PUT("payment/updatePayment/{bill_no}")
    suspend fun updateByBillNo(
        @Path("bill_no") billNo: String,
        @Body bill: TblBillingRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    /**
     * Reports Management
     */

    @GET("report/today-sales")
    suspend fun getTodaySales(@Header("X-Tenant-ID") tenantId: String): Response<TodaySalesReport>

    @GET("report/gst-summary")
    suspend fun getGSTSummary(@Header("X-Tenant-ID") tenantId: String): Response<GSTSummaryReport>

//    @GET("report/sales-summary/{date}")
//    suspend fun getSalesSummaryByDate(
//        @Path("date") date: String,
//        @Header("X-Tenant-ID") tenantId: String
//    ): Response<SalesSummaryReport>

    @GET("api/reports/today-sales")
    suspend fun getReportsForDate(
        @Query("date") date: String? = null
    ): ReportResponse

    @GET("payment/getPaidBills")
    suspend fun getSalesReport(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") date: String,
        @Query("toDate") toDate: String
    ): Response<List<TblBillingResponse>>

    @GET("payment/getUnPaidBills")
    suspend fun getUnPaidBills(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") date: String,
        @Query("toDate") toDate: String
    ): Response<List<TblBillingResponse>>

    @GET("report/getOrderDetailsByMenuItemId")
    suspend fun getItemReport(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") date: String,
        @Query("toDate") toDate: String
    ): Response<List<ItemReport>>

    @GET("report/getOrderDetailsByMenuCatId")
    suspend fun getCategoryReport(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") date: String,
        @Query("toDate") toDate: String

    ): Response<List<CategoryReport>>

    /**
     * Customers Management
     */

    @GET("customer/getCustomerByIsActive")
    suspend fun getAllCustomers(@Header("X-Tenant-ID") tenantId: String): Response<List<TblCustomer>>

    @POST("customer/addCustomer")
    suspend fun createCustomer(
        @Body customer: TblCustomer,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblCustomer>

    @PUT("customer/updateCustomer/{customer_id}")
    suspend fun updateCustomer(
        @Path("customer_id") id: Long,
        @Body customer: TblCustomer,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblCustomer>

    @DELETE("customer/deleteCustomerById/{customer_id}")
    suspend fun deleteCustomer(
        @Path("customer_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Unit>

    /**
     * Staff Management
     */

    @GET("auth/getStaffByIsActive")
    suspend fun getAllStaff(@Header("X-Tenant-ID") tenantId: String): Response<List<TblStaff>>

    @POST("auth/addStaff")
    suspend fun createStaff(
        @Body staff: TblStaffRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblStaff>

    @PUT("auth/updateStaff/{staff_id}")
    suspend fun updateStaff(
        @Path("staff_id") id: Long,
        @Body staff: TblStaffRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("auth/deleteStaffById/{staff_id}")
    suspend fun deleteStaff(
        @Path("staff_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Unit>

    /**
     * AddOn Management
     */

    @GET("menu/addOn/getAddOnByIsActive")
    suspend fun getAllModifiers(@Header("X-Tenant-ID") tenantId: String): Response<List<Modifiers>>

    @GET("modifiers/category/{categoryId}")
    suspend fun getModifiersByCategory(
        @Path("categoryId") categoryId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): List<Modifiers>

    @GET("menu/addOn/{menuItemId}")
    suspend fun getModifiersByMenuItem(
        @Path("menuItemId") menuItemId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): List<Modifiers>

    @POST("menu/addOn/addAddOn")
    suspend fun createModifier(
        @Body modifier: Modifiers,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Modifiers>

    @PUT("menu/addOn/updateAddOn/{add_on_id}")
    suspend fun updateModifier(
        @Path("add_on_id") id: Long,
        @Body modifier: Modifiers,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Modifiers>

    @DELETE("menu/addOn/deleteAddOnById/{add_on_id}")
    suspend fun deleteModifier(
        @Path("add_on_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("menu/addOn/getAddOnByCategoryId/{item_cat_id}")
    suspend fun getModifierGroupsForMenuItem(
        @Path("item_cat_id") menuItemId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<Modifiers>>

    @GET("modifiers/groups")
    suspend fun getAllModifierGroups(@Header("X-Tenant-ID") tenantId: String): Response<List<Modifiers>>

    /**
     * Kitchen KOT Management
     */

    @GET("order/orderDetails/getRunningKots")
    suspend fun getKitchenKOTs(@Header("X-Tenant-ID") tenantId: String): Response<KitchenKOTResponse>

    @PUT("kitchen/kot/{kotId}/status")
    suspend fun updateKOTStatus(
        @Path("kotId") kotId: Int,
        @Body statusUpdate: KOTStatusUpdate,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<KOTUpdateResponse>

    @GET("order/orderDetails/getRunningKots")
    suspend fun getRunningKots(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<KotResponse>>

    @GET("order/orderDetails/getOrderDetailsByKotAndOrderNo")
    suspend fun getOrderDetailsByKotAndOrderNo(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("kotNo") kotNo: Int,
        @Query("orderNo") orderNo: String
    ): Response<List<TblOrderDetailsResponse>>


    /**
     * Kitchen Category Management
     */

    @GET("settings/kitchenCategory/getKitchenCategoryByIsActive")
    suspend fun getAllKitchenCategories(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<KitchenCategory>>

    @GET("settings/kitchenCategory/getKitchenCategory/{kitchen_cat_id}")
    suspend fun getKitchenCategoryById(
        @Path("kitchen_cat_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<KitchenCategory>

    @POST("settings/kitchenCategory/addKitchenCategory")
    suspend fun createKitchenCategory(
        @Body kitchenCategory: KitchenCategory,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<KitchenCategory>

    @PUT("settings/kitchenCategory/updateKitchenCategory/{kitchen_cat_id}")
    suspend fun updateKitchenCategory(
        @Path("kitchen_cat_id") id: Long,
        @Body kitchenCategory: KitchenCategory,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("settings/kitchenCategory/deleteKitchenCategoryById/{kitchen_cat_id}")
    suspend fun deleteKitchenCategory(
        @Path("kitchen_cat_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>


    /**
     * Unit Management
     */

    @GET("settings/unit/getUnitsByIsActive")
    suspend fun getAllUnits(@Header("X-Tenant-ID") tenantId: String): Response<List<TblUnit>>

    @GET("settings/unit/getUnit/{unit_id}")
    suspend fun getUnitById(
        @Path("unit_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblUnit>

    @POST("settings/unit/addUnit")
    suspend fun createUnit(
        @Body unit: TblUnit,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblUnit>

    @PUT("settings/unit/updateUnit/{unit_id}")
    suspend fun updateUnit(
        @Path("unit_id") id: Long,
        @Body unit: TblUnit,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("settings/unit/deleteUnitById/{unit_id}")
    suspend fun deleteUnit(
        @Path("unit_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    /**
     * Register Management
     */

    @POST("companyMaster/createCompanyMaster")
    suspend fun registerCompany(
        @Body registrationRequest: RegistrationRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Registration>

    @GET("companyMaster/getCompanyCode")
    suspend fun getCompanyCode(@Header("X-Tenant-ID") tenantId: String): Response<Map<String, String>>


    /**
     * Reset Data Management
     */

    @POST("settings/reset")
    suspend fun resetData(@Header("X-Tenant-ID") tenantId: String): Response<ApiResponse<Boolean>>


    /**
     * GST Reports Management
     */

    @GET("report/getGSTReport")
    suspend fun getGSTReport(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<ReportGSTResponse>>

    @GET("report/getHsnReport")
    suspend fun getHsnReport(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<HsnReport>>

    @GET("report/getGstDocs")
    suspend fun getGstDocs(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<GSTRDOCS>>

    /**
     * Ledger Management
     */

    @GET("master/ledger/getLedgersByIsActive")
    suspend fun getAllLedgers(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblLedgerDetails>>

    @POST("master/ledger/addLedger")
    suspend fun createLedger(
        @Body ledger: TblLedgerRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblLedgerDetails>

    @PUT("master/ledger/updateLedger/{ledger_id}")
    suspend fun updateLedger(
        @Path("ledger_id") id: Int,
        @Body ledger: TblLedgerRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @DELETE("master/ledger/deleteLedger/{ledger_id}")
    suspend fun deleteLedger(
        @Path("ledger_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @GET("master/ledger/getLedgerByName/{ledger_name}")
    suspend fun getLedgerByName(
        @Path("ledger_name") ledgerName: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblLedgerDetails>

    @GET("master/ledger/getMaxOrderBy")
    suspend fun getLedgerMaxOrderBy(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, Long>>


    @GET("master/ledger/checkExistsOrNot/{ledger_name}")
    suspend fun checkExistsOrNot(
        @Path("ledger_name") ledgerName: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ApiResponse<Boolean>>

    @GET("master/ledger/findByContactNo/{contact_no}")
    suspend fun findByContactNo(
        @Path("contact_no") contactNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblLedgerDetails>


    /**
     * Group Management
     */

    @GET("master/group/getAllGroups")
    suspend fun getAllGroups(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblGroupDetails>>

    @GET("master/group/getGroup/{group_id}")
    suspend fun getGroupById(
        @Path("group_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblGroupDetails>

    @POST("master/group/addGroup")
    suspend fun createGroup(
        @Body group: TblGroupRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblGroupDetails>

    @PUT("master/group/updateGroup/{group_id}")
    suspend fun updateGroup(
        @Path("group_id") id: Int,
        @Body group: TblGroupRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @DELETE("master/group/deleteGroupById/{group_id}")
    suspend fun deleteGroup(
        @Path("group_id") id: Int,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @GET("master/group/getMaxOrderBy")
    suspend fun getMaxOrderBy(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, Long>>

    @GET("master/group/checkExistsOrNot/{group_name}")
    suspend fun checkExistsOrNotGroup(
        @Path("group_name") groupName: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ApiResponse<Boolean>>


    /**
     * BankDetails Management
     */

    @GET("master/bankDetails/getBankDetailsByIsActive")
    suspend fun getAllBankDetails(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblBankDetails>>

    @POST("master/bankDetails/addBankDetails")
    suspend fun createBankDetails(
        @Body bankDetails: TblBankDetails,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblBankDetails>

    @PUT("master/bankDetails/updateBankDetails/{bank_details_id}")
    suspend fun updateBankDetails(
        @Path("bank_details_id") id: Long,
        @Body bankDetails: TblBankDetails,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("master/bankDetails/deleteBankDetailsById/{bank_details_id}")
    suspend fun deleteBankDetails(
        @Path("bank_details_id") id: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<ResponseBody>

    @GET("master/bankDetails/getBankDetailsByLedgerId/{ledger_name}")
    suspend fun getBankDetailsByLedgerId(
        @Path("ledger_name") ledgerName: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblBankDetails>

    /**
     * GroupNature Management
     */

    @GET("master/group/getGroupNatureByIsActive")
    suspend fun getGroupNatures(
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblGroupNature>>


    /**
     * LedgerDetails Management
     */

    @GET("ledger/transaction/getLedgerDetailsId")
    suspend fun getLedgerdetails(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<TblLedgerDetailsIdResponse>>

    @POST("ledger/transaction/addLedgerDetailsId")
    suspend fun addLedgerDetails(
        @Body ledgerDetails: TblLedgerDetailIdRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblLedgerDetailsIdResponse>

    @POST("ledger/transaction/insertSingleLedgerDetails")
    suspend fun insertSingleLedgerDetails(
        @Body ledgerDetails: List<TblLedgerDetailIdRequest>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblLedgerDetailsIdResponse>>

    @GET("ledger/transaction/getLedgerDetailsById/{ledger_details_id}")
    suspend fun getLedgerDetailsById(
        @Path("ledger_details_id") ledgerDetailId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<TblLedgerDetailsIdResponse>

    @PUT("ledger/transaction/updateLedgerDetails/{ledger_details_id}")
    suspend fun updateLedgerDetails(
        @Path("ledger_details_id") ledgerDetailId: Long,
        @Body ledgerDetails: TblLedgerDetailIdRequest,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @DELETE("ledger/transaction/deleteLedgerDetails/{ledger_details_id}")
    suspend fun deleteLedgerDetails(
        @Path("ledger_details_id") ledgerId: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @GET("ledger/transaction/getEntryNoByCounterId/{counterId}/{voucherType}")
    suspend fun getEntryNo(
        @Path("counterId") counterId: Long,
        @Path("voucherType") voucherType: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, String>>

    @GET("ledger/transaction/findById/{id}")
    suspend fun getByLedgerId(
        @Path("id") ledgerId: Long,
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<TblLedgerDetailsIdResponse>>

    @POST("ledger/transaction/addAllLedgerDetailsId")
    suspend fun saveAllLedgerDetails(
        @Body ledgerDetails: List<TblLedgerDetailIdRequest>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @POST("ledger/transaction/updateAllLedgerDetailsId")
    suspend fun updateAllLedgerDetails(
        @Body ledgerDetails: List<TblLedgerDetailIdRequest>,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Boolean>

    @GET("ledger/transaction/getLedgerDetailsByEntryNo/{entry_no}")
    suspend fun getLedgerDetailsByEntryNo(
        @Path("entry_no") entryNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<List<TblLedgerDetailsIdResponse>>

    @DELETE("ledger/transaction/deleteByEntryNo/{entry_no}")
    suspend fun deleteByEntryNo(
        @Path("entry_no") entryNo: String,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Int>

    @GET("ledger/transaction/getOpeningBalance/{fromDate}/{ledger_id}")
    suspend fun getOpeningBalance(
        @Path("fromDate") counterId: String,
        @Path("ledger_id") voucherType: Long,
        @Header("X-Tenant-ID") tenantId: String
    ): Response<Map<String, Double>>

    @GET("ledger/transaction/getDayBookEntries")
    suspend fun getDayBook(
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<TblLedgerDetailsIdResponse>>

    @GET("ledger/transaction/findByPartyId/{ledger_id}")
    suspend fun getByPartyId(
        @Path("ledger_id") ledgerId: Long,
        @Header("X-Tenant-ID") tenantId: String,
        @Query("fromDate") fromDate: String,
        @Query("toDate") toDate: String
    ): Response<List<TblLedgerDetailsIdResponse>>

}