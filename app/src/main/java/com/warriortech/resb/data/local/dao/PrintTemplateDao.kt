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
    fun getSectionsForTemplate(templateId: Int): Flow<List<PrintTemplateSectionEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSection(section: PrintTemplateSectionEntity): Long

    @Query("SELECT * FROM tbl_print_template_line WHERE section_id = :sectionId ORDER BY sort_order")
    fun getLinesForSection(sectionId: Int): Flow<List<PrintTemplateLineEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLine(line: PrintTemplateLineEntity): Long

    @Query("UPDATE tbl_print_template SET is_active = 0 WHERE template_id = :templateId")
    suspend fun deleteTemplate(templateId: Int)

    @Query("UPDATE tbl_print_template_section SET is_active = 0 WHERE section_id = :sectionId")
    suspend fun deleteSection(sectionId: Int)

    @Query("SELECT * FROM tbl_print_template_column WHERE line_id = :lineId ORDER BY sort_order")
    suspend fun getColumnsForLine(lineId: Int): List<PrintTemplateColumnEntity>

}
