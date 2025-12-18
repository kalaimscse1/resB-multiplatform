package com.warriortech.resb.data.repository

import com.warriortech.resb.model.KitchenCategory
import com.warriortech.resb.model.TblUnit
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class UnitRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllUnits(): List<TblUnit> {
        return try {
            apiService.getAllUnits(sessionManager.getCompanyCode()?:"").body()!!
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getUnit(id: Int): TblUnit? {
        return try {
            apiService.getUnitById(id,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createUnit(printer: TblUnit): TblUnit? {
        return try {
            apiService.createUnit(printer,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUnit(printer: TblUnit): Int? {
        return try {
            apiService.updateUnit(printer.unit_id, printer,sessionManager.getCompanyCode()?:"").body()
        } catch (e: Exception) {
            null
        }
    }

    suspend fun deleteUnit(id: Long): Boolean {
        return try {
            apiService.deleteUnit(id,sessionManager.getCompanyCode()?:"")
            true
        } catch (e: Exception) {
            false
        }
    }
}
