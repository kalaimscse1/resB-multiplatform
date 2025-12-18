package com.warriortech.resb.data.repository

import com.warriortech.resb.model.Printer
import com.warriortech.resb.model.TblPrinterResponse
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrinterRepository @Inject constructor(
    private val apiService: ApiService,
    private val sessionManager: SessionManager
) {
    suspend fun getAllPrinters(): List<TblPrinterResponse> {
        return try {
            apiService.getPrinters(sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun getPrinterById(id: Int): TblPrinterResponse? {
        return try {
            apiService.getPrinterById(id,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun createPrinter(printer: Printer): TblPrinterResponse? {
        return try {
            apiService.createPrinter(printer,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updatePrinter(printer: Printer): Int {
        return try {
            apiService.updatePrinter(printer.printer_id, printer,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            0
        }
    }

    suspend fun deletePrinter(id: Long): Int {
        return try {
            apiService.deletePrinter(id,sessionManager.getCompanyCode()?:"")
        } catch (e: Exception) {
            0
        }
    }
}
