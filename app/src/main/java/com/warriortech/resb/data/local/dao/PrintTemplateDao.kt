package com.warriortech.resb.data.local.dao

import androidx.room.*
import com.warriortech.resb.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintTemplateDao {
    @Query("SELECT * FROM tbl_print_template WHERE is_active = 1")
    fun getAllTemplates(): Flow<List<PrintTemplateEntity>>

    @Query("SELECT * FROM tbl_print_template WHERE document_type = :type AND is_default = 1 AND is_active = 1 LIMIT 1")
    suspend fun getDefaultTemplate(type: String): PrintTemplateEntity?

    @Query("UPDATE tbl_print_template SET is_default = 0 WHERE document_type = :type")
    suspend fun clearDefault(type: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: PrintTemplateEntity): Long

    @Query("SELECT * FROM tbl_print_template_section WHERE template_id = :templateId AND is_active = 1 ORDER BY sort_order")
    suspend fun getSectionsForTemplateSync(templateId: Int): List<PrintTemplateSectionEntity>

    @Query("SELECT * FROM tbl_print_template_section WHERE template_id = :templateId ORDER BY sort_order")
    fun getSectionsForTemplate(templateId: Int): Flow<List<PrintTemplateSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: PrintTemplateSectionEntity): Long

    @Query("SELECT * FROM tbl_print_template_line WHERE section_id = :sectionId AND is_active = 1 ORDER BY sort_order")
    suspend fun getLinesForSectionSync(sectionId: Int): List<PrintTemplateLineEntity>

    @Query("SELECT * FROM tbl_print_template_line WHERE section_id = :sectionId ORDER BY sort_order")
    fun getLinesForSection(sectionId: Int): Flow<List<PrintTemplateLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: PrintTemplateLineEntity): Long

    @Query("UPDATE tbl_print_template_line SET is_active = 0 WHERE line_id = :lineId")
    suspend fun deleteLine(lineId: Int)

    @Query("UPDATE tbl_print_template SET is_active = 0 WHERE template_id = :templateId")
    suspend fun deleteTemplate(templateId: Int)

    @Query("UPDATE tbl_print_template_section SET is_active = 0 WHERE section_id = :sectionId")
    suspend fun deleteSection(sectionId: Int)

    @Query("SELECT * FROM tbl_print_template_column WHERE line_id = :lineId ORDER BY sort_order")
    suspend fun getColumnsForLineSync(lineId: Int): List<PrintTemplateColumnEntity>

    @Query("SELECT * FROM tbl_print_template_column WHERE line_id = :lineId ORDER BY sort_order")
    fun getColumnsForLine(lineId: Int): Flow<List<PrintTemplateColumnEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertColumn(column: PrintTemplateColumnEntity): Long

    @Query("DELETE FROM tbl_print_template_column WHERE column_id = :columnId")
    suspend fun deleteColumn(columnId: Int)

    @Query("SELECT * FROM tbl_kot_settings WHERE template_id = :templateId")
    fun getKotSettings(templateId: Int): Flow<KotSettingsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertKotSettings(settings: KotSettingsEntity): Long

    @Query("SELECT * FROM tbl_print_platform_override WHERE template_id = :templateId")
    fun getPlatformOverrides(templateId: Int): Flow<List<PrintPlatformOverrideEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPlatformOverride(override: PrintPlatformOverrideEntity): Long
}
