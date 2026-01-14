package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(tableName = "tbl_print_template")
data class PrintTemplateEntity(
    @PrimaryKey(autoGenerate = true) val template_id: Int = 0,
    val template_name: String,
    val document_type: String, // BILL / KOT
    val platform: String, // ANDROID / WINDOWS
    val paper_width_mm: Int, // 58 / 80 / 112
    val is_default: Boolean = false,
    val is_active: Boolean = true,
    val created_at: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "tbl_print_template_section",
    foreignKeys = [ForeignKey(
        entity = PrintTemplateEntity::class,
        parentColumns = ["template_id"],
        childColumns = ["template_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrintTemplateSectionEntity(
    @PrimaryKey(autoGenerate = true) val section_id: Int = 0,
    val template_id: Int,
    val section_type: String, // HEADER / BODY / FOOTER
    val sort_order: Int,
    val is_active: Boolean = true
)

@Entity(
    tableName = "tbl_print_template_line",
    foreignKeys = [ForeignKey(
        entity = PrintTemplateSectionEntity::class,
        parentColumns = ["section_id"],
        childColumns = ["section_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrintTemplateLineEntity(
    @PrimaryKey(autoGenerate = true) val line_id: Int = 0,
    val section_id: Int,
    val field_key: String,
    val display_text: String?,
    val align_type: String, // LEFT / CENTER / RIGHT
    val font_size: String, // SMALL / NORMAL / LARGE
    val is_bold: Boolean = false,
    val is_underline: Boolean = false,
    val max_width_pct: Int?,
    val sort_order: Int,
    val is_repeatable: Boolean = false,
    val is_active: Boolean = true
)
