package com.warriortech.resb.data.local.dao

import androidx.room.*
import com.warriortech.resb.data.local.entity.*
import kotlinx.coroutines.flow.Flow

@Dao
interface PrintTemplateDao {
    @Query("SELECT * FROM tbl_print_template WHERE is_active = 1")
    fun getAllTemplates(): Flow<List<PrintTemplateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTemplate(template: PrintTemplateEntity): Long

    @Query("SELECT * FROM tbl_print_template_section WHERE template_id = :templateId ORDER BY sort_order")
    suspend fun getSectionsForTemplate(templateId: Int): List<PrintTemplateSectionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: PrintTemplateSectionEntity): Long

    @Query("SELECT * FROM tbl_print_template_line WHERE section_id = :sectionId ORDER BY sort_order")
    suspend fun getLinesForSection(sectionId: Int): List<PrintTemplateLineEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: PrintTemplateLineEntity): Long
}
