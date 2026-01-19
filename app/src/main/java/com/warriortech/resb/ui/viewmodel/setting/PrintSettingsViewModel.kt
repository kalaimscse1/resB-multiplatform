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
import com.warriortech.resb.data.local.entity.KotSettingsEntity
import com.warriortech.resb.data.local.entity.PrintPlatformOverrideEntity
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

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

    fun addLogo(sectionId: Int, order: Int) {
        viewModelScope.launch {
            val line = PrintTemplateLineEntity(
                section_id = sectionId,
                field_key = "LOGO",
                sort_order = order,
                display_text = "LOGO",
                align_type = "CENTER",
                font_size = "14",
                max_width_pct = 100
            )
            printTemplateDao.insertLine(line)
        }
    }

    fun addQrCode(sectionId: Int, order: Int) {
        viewModelScope.launch {
            val line = PrintTemplateLineEntity(
                section_id = sectionId,
                field_key = "QRCODE",
                sort_order = order,
                display_text = "QRCODE",
                align_type = "CENTER",
                font_size = "14",
                max_width_pct = 100
            )
            printTemplateDao.insertLine(line)
        }
    }

    fun updateLine(line: PrintTemplateLineEntity) {
        viewModelScope.launch {
            printTemplateDao.insertLine(line)
        }
    }

    fun deleteLine(lineId: Int) {
        viewModelScope.launch {
            printTemplateDao.deleteLine(lineId)
        }
    }

    fun getLinesForSection(sectionId: Int) = printTemplateDao.getLinesForSection(sectionId)

    fun getColumnsForLine(lineId: Int) = printTemplateDao.getColumnsForLine(lineId)

    fun updateColumn(column: PrintTemplateColumnEntity) {
        viewModelScope.launch {
            printTemplateDao.insertColumn(column)
        }
    }

    fun addColumn(lineId: Int, fieldKey: String, width: Int, align: String, order: Int) {
        viewModelScope.launch {
            val column = PrintTemplateColumnEntity(
                line_id = lineId,
                field_key = fieldKey,
                width_pct = width,
                align_type = align,
                sort_order = order
            )
            printTemplateDao.insertColumn(column)
        }
    }

    fun deleteColumn(column: PrintTemplateColumnEntity) {
        viewModelScope.launch {
            printTemplateDao.deleteColumn(column.column_id)
        }
    }

    fun getKotSettings(templateId: Int) = printTemplateDao.getKotSettings(templateId)

    fun updateKotSettings(settings: KotSettingsEntity) {
        viewModelScope.launch {
            printTemplateDao.insertKotSettings(settings)
        }
    }

    fun getPlatformOverrides(templateId: Int) = printTemplateDao.getPlatformOverrides(templateId)

    fun addPlatformOverride(override: PrintPlatformOverrideEntity) {
        viewModelScope.launch {
            printTemplateDao.insertPlatformOverride(override)
        }
    }
}
