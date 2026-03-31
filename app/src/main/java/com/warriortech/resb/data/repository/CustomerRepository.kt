package com.warriortech.resb.data.repository

import com.warriortech.resb.model.*
import javax.inject.Inject
import javax.inject.Singleton
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager

@Singleton
class CustomerRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getAllCustomers(): List<TblCustomer> {
        val response = apiService.getAllCustomers(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyList()
        } else {
            throw Exception("Failed to fetch customers: ${response.message()}")
        }
    }

    suspend fun insertCustomer(customer: TblCustomer): TblCustomer {
        val response = apiService.createCustomer(customer,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create customer")
        } else {
            throw Exception("Failed to create customer: ${response.message()}")
        }
    }

    suspend fun updateCustomer(customer: TblCustomer): TblCustomer {
        val response = apiService.updateCustomer(customer.customer_id, customer,sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to update customer")
        } else {
            throw Exception("Failed to update customer: ${response.message()}")
        }
    }

    suspend fun deleteCustomer(customerId: Long) {
        val response = apiService.deleteCustomer(customerId,sessionManager.getCompanyCode()?:"")
        if (!response.isSuccessful) {
            throw Exception("Failed to delete customer: ${response.message()}")
        }
    }

    suspend fun getAllCustomerInfo(): List<TblCustomerInfoResponse> {
        return try {
            val response = apiService.getAllCustomerInfo(sessionManager.getCompanyCode() ?: "")
            response
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getCustomerInfosByCustomerId(customerId: Long): List<TblCustomerInfoResponse> {
        return try {
            val allInfo = getAllCustomerInfo()
            allInfo.filter { it.customer.customer_id == customerId }
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getActiveCustomerInfoByCustomerId(customerId: Long): TblCustomerInfoResponse? {
        val response = apiService.getActiveCustomerInfoByCustomerId(customerId, sessionManager.getCompanyCode() ?: "")
        return if (response.isSuccessful) {
            response.body()
        } else null
    }

    suspend fun createCustomerInfo(request: TblCustomerInfoRequest): TblCustomerInfoResponse {
        val response = apiService.createCustomerInfo(request, sessionManager.getCompanyCode() ?: "")
        if (response.isSuccessful) {
            return response.body() ?: throw Exception("Failed to create customer info")
        } else {
            throw Exception("Failed to create customer info: ${response.message()}")
        }
    }

    suspend fun updateCustomerInfo(request: TblCustomerInfoRequest): Int {
        val response = apiService.updateCustomerInfo(request, sessionManager.getCompanyCode() ?: "")
        if (response.isSuccessful) {
            return response.body() ?: 0
        } else {
            throw Exception("Failed to update customer info: ${response.message()}")
        }
    }
}
