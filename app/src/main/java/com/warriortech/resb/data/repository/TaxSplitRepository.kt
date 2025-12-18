package com.warriortech.resb.data.repository

import com.warriortech.resb.model.Tax
import com.warriortech.resb.model.TaxSplit
import com.warriortech.resb.model.TblTaxSplit
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaxSplitRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllTaxSplits(): List<TblTaxSplit> {
        return try {
            apiService.getTaxSplits(sessionManager.getCompanyCode()?:"").body()?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getTaxSplitById(id: Int): TblTaxSplit? {
        return try {
            apiService.getTaxSplitById(id,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createTaxSplit(taxSplit: TaxSplit): TblTaxSplit? {
        return try {
            apiService.createTaxSplit(taxSplit,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateTaxSplit(taxSplit: TaxSplit): Int? {
        return try {
            apiService.updateTaxSplit(taxSplit.tax_split_id, taxSplit,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteTaxSplit(id: Long): Boolean {
        return try {
            apiService.deleteTaxSplit(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getTaxes():List<Tax>{
        return try {
            apiService.getTaxes(sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            emptyList()
        }
    }
}
