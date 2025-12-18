package com.warriortech.resb.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.repository.TemplateRepository
import com.warriortech.resb.model.*
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TemplateViewModel @Inject constructor(
    private val templateRepository: TemplateRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TemplateUiState())
    val uiState: StateFlow<TemplateUiState> = _uiState.asStateFlow()

    private val _editingTemplate = MutableStateFlow<ReceiptTemplate?>(null)
    val editingTemplate: StateFlow<ReceiptTemplate?> = _editingTemplate.asStateFlow()

    init {
        loadTemplates()
    }

    fun loadTemplates() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            templateRepository.getAllTemplates().collect { templates ->
                _uiState.update {
                    it.copy(
                        templates = templates,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun loadTemplatesByType(type: ReceiptType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            templateRepository.getTemplatesByType(type).collect { templates ->
                _uiState.update {
                    it.copy(
                        templates = templates,
                        selectedType = type,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun startEditingTemplate(template: ReceiptTemplate) {
        _editingTemplate.value = template
    }

    fun createNewTemplate(type: ReceiptType) {
        val newTemplate = ReceiptTemplate(
            id = "new_${System.currentTimeMillis()}",
            name = "New ${type.name} Template",
            type = type,
            headerSettings = HeaderSettings(),
            bodySettings = BodySettings(),
            footerSettings = FooterSettings(),
            paperSettings = PaperSettings()
        )
        _editingTemplate.value = newTemplate
    }

    fun updateEditingTemplate(template: ReceiptTemplate) {
        _editingTemplate.value = template
    }

    fun saveTemplate(updatedTemplate: ReceiptTemplate) {
        val template = _editingTemplate.value ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            templateRepository.saveTemplate(template).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "Template saved successfully"
                            )
                        }
                        _editingTemplate.value = null
                        loadTemplates()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    fun deleteTemplate(templateId: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            templateRepository.deleteTemplate(templateId).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "Template deleted successfully"
                            )
                        }
                        loadTemplates()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    fun setDefaultTemplate(templateId: String, type: ReceiptType) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            templateRepository.setDefaultTemplate(templateId, type).collect { result ->
                result.fold(
                    onSuccess = {
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                message = "Default template updated"
                            )
                        }
                        loadTemplates()
                    },
                    onFailure = { error ->
                        _uiState.update {
                            it.copy(
                                isLoading = false,
                                errorMessage = error.message
                            )
                        }
                    }
                )
            }
        }
    }

    fun clearMessages() {
        _uiState.update {
            it.copy(
                message = null,
                errorMessage = null
            )
        }
    }

    fun cancelEditing() {
        _editingTemplate.value = null
    }
}

data class TemplateUiState(
    val templates: List<ReceiptTemplate> = emptyList(),
    val selectedType: ReceiptType? = null,
    val isLoading: Boolean = false,
    val message: String? = null,
    val errorMessage: String? = null
)
