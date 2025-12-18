package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KotResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject


class KotRepository@Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
)  {
    suspend fun fetchKotReports(fromDate:String,toDate:String): List<KotResponse> {
    val  response = apiService.getRunningKots(sessionManager.getCompanyCode()?:"",
        fromDate,toDate)
        return if (response.isSuccessful)
            response.body() ?: emptyList()
        else
            emptyList()
    }
}