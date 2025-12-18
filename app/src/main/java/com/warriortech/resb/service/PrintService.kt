package com.warriortech.resb.service

import android.content.Context
import com.warriortech.resb.data.repository.TemplateRepository
import com.warriortech.resb.model.*
import com.warriortech.resb.util.PrinterHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PrintService @Inject constructor(
    context: Context,
    private val templateRepository: TemplateRepository
) {
    private val printerHelper = PrinterHelper(context)

    /**
     * Print KOT using the default or specified template
     */
    suspend fun printKot(kotData: KotData, templateId: String? = null): Flow<Result<String>> = flow {
        try {
            // Get the template to use
            val template = if (templateId != null) {
                templateRepository.getTemplate(templateId)
            } else {
                templateRepository.getDefaultTemplate(ReceiptType.KOT)
            }

            if (template == null) {
                emit(Result.failure(Exception("KOT template not found")))
                return@flow
            }

            // Print using the template
            val success = printerHelper.printKot(kotData, template)
            
            if (success) {
                emit(Result.success("KOT printed successfully"))
            } else {
                emit(Result.failure(Exception("Failed to print KOT")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Error printing KOT: ${e.message}")))
        }
    }

    /**
     * Print bill using the default or specified template
     */
    suspend fun printBill(billData: Bill, templateId: String? = null): Flow<Result<String>> = flow {
        try {
            // Get the template to use
            val template = if (templateId != null) {
                templateRepository.getTemplate(templateId)
            } else {
                templateRepository.getDefaultTemplate(ReceiptType.BILL)
            }

            if (template == null) {
                emit(Result.failure(Exception("Bill template not found")))
                return@flow
            }

            // Print using the template
            val success = printerHelper.printBill(billData, template)
            
            if (success) {
                emit(Result.success("Bill printed successfully"))
            } else {
                emit(Result.failure(Exception("Failed to print bill")))
            }
        } catch (e: Exception) {
            emit(Result.failure(Exception("Error printing bill: ${e.message}")))
        }
    }



    /**
     * Get preview of KOT formatted with template
     */
    suspend fun previewKot(kotData: KotData, templateId: String? = null): String? {
        return try {
            val template = if (templateId != null) {
                templateRepository.getTemplate(templateId)
            } else {
                templateRepository.getDefaultTemplate(ReceiptType.KOT)
            }

            template?.let { 
                printerHelper.formatKotForPrinting(kotData, it)
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Get preview of bill formatted with template
     */
    suspend fun previewBill(billData: Bill, templateId: String? = null): String? {
        return try {
            val template = if (templateId != null) {
                templateRepository.getTemplate(templateId)
            } else {
                templateRepository.getDefaultTemplate(ReceiptType.BILL)
            }

            template?.let { 
                printerHelper.formatBillForPrinting(billData, it)
            }
        } catch (e: Exception) {
            null
        }
    }
}
