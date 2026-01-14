package com.warriortech.resb.ui.viewmodel.setting

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.warriortech.resb.data.local.dao.PrintTemplateDao
import com.warriortech.resb.data.local.entity.PrintTemplateEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PrintSettingsViewModel @Inject constructor(
    private val printTemplateDao: PrintTemplateDao
) : ViewModel() {

    private val _templates = MutableStateFlow<List<PrintTemplateEntity>>(emptyList())
    val templates: StateFlow<List<PrintTemplateEntity>> = _templates.asStateFlow()

    init {
        loadTemplates()
    }

    private fun loadTemplates() {
        viewModelScope.launch {
            printTemplateDao.getAllTemplates().collect {
                _templates.value = it
            }
        }
    }

    fun addTemplate(name: String, docType: String, platform: String, width: Int) {
        viewModelScope.launch {
            val template = PrintTemplateEntity(
                template_name = name,
                document_type = docType,
                platform = platform,
                paper_width_mm = width
            )
            printTemplateDao.insertTemplate(template)
        }
    }
}
