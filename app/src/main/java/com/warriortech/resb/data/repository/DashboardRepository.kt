package com.warriortech.resb.data.repository

import com.warriortech.resb.model.ApiResponse
import com.warriortech.resb.model.DashboardMetrics
import com.warriortech.resb.model.RunningOrder
import com.warriortech.resb.model.DashboardChartData
import com.warriortech.resb.model.DayCloseResponse
import com.warriortech.resb.model.PaymentModeData
import com.warriortech.resb.model.WeeklySalesData
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DashboardRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getDashboardMetrics(): DashboardMetrics {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getDashboardMetrics(sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful && response.body() != null) {
                    val metrics = response.body()!!
                    val chartData = getChartData()
                    metrics
                } else {
                    // Return default metrics if API fails
                    DashboardMetrics(
                        runningOrders = 0,
                        pendingBills = 0,
                        totalSales = 0.0,
                        pendingDue = 0.0,
                    )
                }
            } catch (e: Exception) {
                // Return default metrics on error
                DashboardMetrics(
                    runningOrders = 0,
                    pendingBills = 0,
                    totalSales = 0.0,
                    pendingDue = 0.0,
                )
            }
        }
    }

    suspend fun getChartData(): DashboardChartData {
        return try {
            // In a real app, these would be separate API calls
            val paymentModeData =    getPaymentModeData()
            val weeklySalesData = getWeeklySalesData()
            DashboardChartData(paymentModeData, weeklySalesData)
        } catch (e: Exception) {
            getDefaultChartData()
        }
    }

    suspend fun addDayClose(staffId: Long): ApiResponse<DayCloseResponse>{
        return apiService.addDayClose(staffId,sessionManager.getCompanyCode()?:"").body()!!
    }

  suspend  fun getPaymentModeData(): List<PaymentModeData> {
        val res= apiService.getPayModeAmount(sessionManager.getCompanyCode()?:"")
      return if(res.isSuccessful && res.body()!=null){
          val responseData = res.body()!!
          val data = responseData.map {

              PaymentModeData(it.paymentMode,it.amount,androidx.compose.ui.graphics.Color(it.color))

          }
          data
      }
        else {
          // Mock data - replace with actual API call
          listOf(
              PaymentModeData("Cash", 0.0, androidx.compose.ui.graphics.Color(0xFF4CAF50)),
              PaymentModeData("Card", 0.0, androidx.compose.ui.graphics.Color(0xFF2196F3)),
              PaymentModeData("UPI", 0.0, androidx.compose.ui.graphics.Color(0xFFFF9800)),
              PaymentModeData("Others", 0.0, androidx.compose.ui.graphics.Color(0xFF9C27B0))
          )
      }
    }

   suspend fun getWeeklySalesData(): List<WeeklySalesData> {
        // Mock data - replace with actual API call
       val res = apiService.getWeeklySales(sessionManager.getCompanyCode()?:"")
        return if(res.isSuccessful && res.body()!=null){
            val responseData = res.body()!!
            responseData
        }
       else {
            listOf(
                WeeklySalesData("Mon", 0.0),
                WeeklySalesData("Tue", 0.0),
                WeeklySalesData("Wed", 0.0),
                WeeklySalesData("Thu", 0.0),
                WeeklySalesData("Fri", 0.0),
                WeeklySalesData("Sat", 0.0),
                WeeklySalesData("Sun", 0.0)
            )
        }
    }

    private suspend fun getDefaultChartData(): DashboardChartData {
        return DashboardChartData(
            paymentModeData = getPaymentModeData(),
            weeklySalesData = getWeeklySalesData()
        )
    }

    suspend fun getRunningOrders(): List<RunningOrder> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRunningOrders(sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    suspend fun getRecentActivity(): List<String> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getRecentActivity(sessionManager.getCompanyCode()?:"")
                if (response.isSuccessful && response.body() != null) {
                    response.body()!!
                } else {
                    emptyList()
                }
            } catch (e: Exception) {
                listOf(
                    "Order #1001 completed",
                    "New order received - Table 5",
                    "Payment received - Order #998"
                )
            }
        }
    }
}
