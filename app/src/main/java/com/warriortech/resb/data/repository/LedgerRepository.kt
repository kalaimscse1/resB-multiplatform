package com.warriortech.resb.data.repository

import androidx.datastore.preferences.protobuf.Api
import com.warriortech.resb.model.ApiResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import okhttp3.ResponseBody
import javax.inject.Inject

class LedgerRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {

    suspend fun getLedgers(): List<com.warriortech.resb.model.TblLedgerDetails>? {
        return try {
            apiService.getAllLedgers(sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getLedgerById(ledgerId: String): com.warriortech.resb.model.TblLedgerDetails? {
        return try {
            apiService.getLedgerByName(ledgerId, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createLedger(ledger: com.warriortech.resb.model.TblLedgerRequest): com.warriortech.resb.model.TblLedgerDetails? {
        return try {
            apiService.createLedger(ledger, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateLedger(ledgerId: Int, ledger: com.warriortech.resb.model.TblLedgerRequest): Boolean? {
        return try {
            apiService.updateLedger(ledgerId, ledger, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteLedger(ledgerId: Int): Boolean? {
        return try {
            apiService.deleteLedger(ledgerId, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBankDetails():List<com.warriortech.resb.model.TblBankDetails>?{
        return try {
            apiService.getAllBankDetails(sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createBankDetails(bankDetails: com.warriortech.resb.model.TblBankDetails): com.warriortech.resb.model.TblBankDetails? {
        return try {
            apiService.createBankDetails(bankDetails, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateBankDetails(bankId: Long, bankDetails: com.warriortech.resb.model.TblBankDetails): Int? {
        return try {
            apiService.updateBankDetails(bankId, bankDetails, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }
    
    suspend fun deleteBankDetails(bankId: Long): ResponseBody? {
        return try {
            apiService.deleteBankDetails(bankId, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getBankDetailsById(ledgerName: String): com.warriortech.resb.model.TblBankDetails? {
        return try {
            apiService.getBankDetailsByLedgerId(ledgerName, sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun getOrderBy(): Map<String, Long>{
        val response = apiService.getLedgerMaxOrderBy(sessionManager.getCompanyCode()?:"")
        if (response.isSuccessful) {
            return response.body() ?: emptyMap()
        }
        else{
            throw Exception("Failed to get OrderBy: ${response.message()}")
        }
    }

    suspend fun checkexists(ledgerName: String): ApiResponse<Boolean> {
        return try {
            apiService.checkExistsOrNot(ledgerName, sessionManager.getCompanyCode() ?: "").body()!!
        } catch (e: Exception) {
            ApiResponse(false, e.message.toString(),false )
        }
    }
}