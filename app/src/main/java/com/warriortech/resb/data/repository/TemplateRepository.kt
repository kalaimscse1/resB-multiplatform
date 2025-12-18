package com.warriortech.resb.data.repository

import com.warriortech.resb.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TemplateRepository @Inject constructor() {
    
    private val templates = mutableListOf<ReceiptTemplate>()
    
    init {
        // Add default templates
        templates.addAll(getDefaultTemplates())
    }
    
    suspend fun getAllTemplates(): Flow<List<ReceiptTemplate>> = flow {
        emit(templates.toList())
    }
    
    suspend fun getTemplatesByType(type: ReceiptType): Flow<List<ReceiptTemplate>> = flow {
        emit(templates.filter { it.type == type })
    }
    
    suspend fun getDefaultTemplate(type: ReceiptType): ReceiptTemplate {
       return templates.find { it.type == type && it.isDefault }
            ?: throw NoSuchElementException("Default template for type $type not found")
    }
    suspend fun getTemplate(templateId: String): ReceiptTemplate {
        return templates.find { it.id == templateId }
            ?: throw NoSuchElementException("Template with id $templateId not found")
    }
    
    suspend fun saveTemplate(template: ReceiptTemplate): Flow<Result<ReceiptTemplate>> = flow {
        try {
            val existingIndex = templates.indexOfFirst { it.id == template.id }
            if (existingIndex != -1) {
                templates[existingIndex] = template
            } else {
                templates.add(template)
            }
            emit(Result.success(template))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun deleteTemplate(templateId: String): Flow<Result<Boolean>> = flow {
        try {
            val removed = templates.removeIf { it.id == templateId }
            emit(Result.success(removed))
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    suspend fun setDefaultTemplate(templateId: String, type: ReceiptType): Flow<Result<Boolean>> = flow {
        try {
            // Remove default flag from all templates of this type
            templates.forEach { template ->
                if (template.type == type) {
                    val index = templates.indexOf(template)
                    templates[index] = template.copy(isDefault = false)
                }
            }
            
            // Set new default
            val templateIndex = templates.indexOfFirst { it.id == templateId }
            if (templateIndex != -1) {
                templates[templateIndex] = templates[templateIndex].copy(isDefault = true)
                emit(Result.success(true))
            } else {
                emit(Result.failure(Exception("Template not found")))
            }
        } catch (e: Exception) {
            emit(Result.failure(e))
        }
    }
    
    private fun getDefaultTemplates(): List<ReceiptTemplate> {
        return listOf(
            ReceiptTemplate(
                id = "default_kot",
                name = "Default KOT",
                type = ReceiptType.KOT,
                headerSettings = HeaderSettings(
                    showLogo = true,
                    businessName = "Restaurant Name",
                    businessAddress = "Restaurant Address",
                    businessPhone = "Phone Number",
                    fontSize = 16,
                    fontWeight = FontWeights.BOLD,
                    textAlign = TextAligns.CENTER
                ),
                bodySettings = BodySettings(
                    showItemDetails = true,
                    showQuantity = true,
                    showPrice = false,
                    showTotal = false,
                    fontSize = 14,
                    fontWeight = FontWeights.NORMAL,
                    showBorders = true,
                    lineSpacing = 2
                ),
                footerSettings = FooterSettings(
                    showThankYou = true,
                    customMessage = "Kitchen Order Ticket",
                    showDateTime = true,
                    fontSize = 10,
                    fontWeight = FontWeights.NORMAL,
                    textAlign = TextAligns.CENTER
                ),
                paperSettings = PaperSettings(
                    paperSize = PaperSize.RECEIPT_80MM,
                    margins = Margins(5, 5, 5, 5),
                    characterWidth = 32
                ),
                isDefault = true
            ),
            ReceiptTemplate(
                id = "default_bill",
                name = "Default Bill",
                type = ReceiptType.BILL,
                headerSettings = HeaderSettings(
                    showLogo = true,
                    businessName = "Restaurant Name",
                    businessAddress = "Restaurant Address",
                    businessPhone = "Phone Number",
                    fontSize = 16,
                    fontWeight = FontWeights.BOLD,
                    textAlign = TextAligns.CENTER
                ),
                bodySettings = BodySettings(
                    showItemDetails = true,
                    showQuantity = true,
                    showPrice = true,
                    showTotal = true,
                    fontSize = 12,
                    fontWeight = FontWeights.NORMAL,
                    showBorders = true,
                    lineSpacing = 2
                ),
                footerSettings = FooterSettings(
                    showThankYou = true,
                    customMessage = "Thank you for your visit!",
                    showDateTime = true,
                    fontSize = 10,
                    fontWeight = FontWeights.NORMAL,
                    textAlign = TextAligns.CENTER
                ),
                paperSettings = PaperSettings(
                    paperSize = PaperSize.RECEIPT_80MM,
                    margins = Margins(5, 5, 5, 5),
                    characterWidth = 32
                ),
                isDefault = true
            )
        )
    }
}
