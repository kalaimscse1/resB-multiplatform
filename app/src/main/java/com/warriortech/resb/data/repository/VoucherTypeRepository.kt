package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.TblVoucherType
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class VoucherTypeRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllVoucherTypes(): List<TblVoucherType> {
        return try {
            apiService.getVoucherTypes(sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getVoucherType(id: Int): TblVoucherType? {
        return try {
            apiService.getVoucherTypeById(id,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createVoucherType(printer: TblVoucherType): TblVoucherType? {
        return try {
            apiService.createVoucherType(printer,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateVoucherType(printer: TblVoucherType): Int? {
        return try {
            apiService.updateVoucherType(printer.voucher_Type_id, printer,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteVoucherType(id: Long): Boolean {
        return try {
            apiService.deleteVoucherType(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }
}
