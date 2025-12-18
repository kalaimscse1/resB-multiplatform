package com.warriortech.resb.data.repository

import com.warriortech.resb.model.TblVoucher
import com.warriortech.resb.model.TblVoucherRequest
import com.warriortech.resb.model.TblVoucherResponse
import com.warriortech.resb.model.TblVoucherType
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllVouchers(): List<TblVoucherResponse> {
        return try {
            apiService.getVouchers(sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVoucherById(id: Int): TblVoucherResponse? {
        return try {
            apiService.getVoucherById(id,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createVoucher(voucher: TblVoucherRequest): TblVoucherResponse? {
        return try {
            apiService.createVoucher(voucher,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVoucher(voucher: TblVoucherRequest): Int? {
        return try {
            apiService.updateVoucher(voucher.voucher_id, voucher,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteVoucher(id: Long): Boolean {
        return try {
            apiService.deleteVoucher(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getAllVoucherTypes(): List<TblVoucherType> {
        return try {
            apiService.getVoucherTypes(sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVoucherByCounterId(type: String): TblVoucherResponse?{
        return try {
            apiService.getVoucherByCounterId(sessionManager.getUser()?.counter_id?:0,sessionManager.getCompanyCode()?:"",type).body()
        }catch (e: Exception){
            null
        }
    }
}
