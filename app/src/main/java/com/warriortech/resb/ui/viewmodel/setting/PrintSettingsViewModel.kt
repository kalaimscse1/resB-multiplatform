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

import com.warriortech.resb.data.local.entity.PrintTemplateSectionEntity
import com.warriortech.resb.data.local.entity.PrintTemplateLineEntity
import com.warriortech.resb.data.local.entity.PrintTemplateColumnEntity
import kotlinx.coroutines.flow.flatMapLatest

@HiltViewModel
class PrintSettingsViewModel @Inject constructor(
    private val printTemplateDao: PrintTemplateDao
) : ViewModel() {

    private val _templates = MutableStateFlow<List<PrintTemplateEntity>>(emptyList())
    val templates: StateFlow<List<PrintTemplateEntity>> = _templates.asStateFlow()

    private val _selectedTemplate = MutableStateFlow<PrintTemplateEntity?>(null)
    val selectedTemplate = _selectedTemplate.asStateFlow()

    private val _sections = MutableStateFlow<List<PrintTemplateSectionEntity>>(emptyList())
    val sections = _sections.asStateFlow()

    private val _sectionsLine = MutableStateFlow<List<PrintTemplateLineEntity>>(emptyList())
    val sectionsLine = _sectionsLine.asStateFlow()

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

    fun selectTemplate(template: PrintTemplateEntity?) {
        _selectedTemplate.value = template
        if (template != null) {
            loadSections(template.template_id.toLong())
        } else {
            _sections.value = emptyList()
        }
    }

    private fun loadSections(templateId: Long) {
        viewModelScope.launch {
            printTemplateDao.getSectionsForTemplate(templateId.toInt()).collect {
                _sections.value = it
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

    fun deleteTemplate(template: PrintTemplateEntity) {
        viewModelScope.launch {
            printTemplateDao.deleteTemplate(template.template_id)
        }
    }

    fun addSection(templateId: Long, sectionName: String, order: Int) {
        viewModelScope.launch {
            val section = PrintTemplateSectionEntity(
                template_id = templateId.toInt(),
                section_type = sectionName,
                sort_order = order
            )
            printTemplateDao.insertSection(section)
        }
    }

    fun deleteSection(section: PrintTemplateSectionEntity) {
        viewModelScope.launch {
            printTemplateDao.deleteSection(section.section_id)
        }
    }

    fun addLine(sectionId: Long, lineName: String, order: Int) {
        viewModelScope.launch {
            val line = PrintTemplateLineEntity(
                section_id = sectionId.toInt(),
                field_key = lineName,
                sort_order = order,
                display_text = lineName,
                align_type = "CENTER",
                font_size = "14",
                max_width_pct = 2
            )
            printTemplateDao.insertLine(line)
        }
    }

    fun getLinesForSection(sectionId: Long) {
        viewModelScope.launch {
            printTemplateDao.getLinesForSection(sectionId.toInt()).collect {
                _sectionsLine.value = it
            }
        }
    }

    fun getColumnsForLine(lineId: Long) = {
        viewModelScope.launch {
            printTemplateDao.getColumnsForLine(lineId.toInt())
        }
    }
}
