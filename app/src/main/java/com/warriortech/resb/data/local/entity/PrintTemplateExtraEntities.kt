package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_print_template_column",
    foreignKeys = [ForeignKey(
        entity = PrintTemplateLineEntity::class,
        parentColumns = ["line_id"],
        childColumns = ["line_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrintTemplateColumnEntity(
    @PrimaryKey(autoGenerate = true) val column_id: Int = 0,
    val line_id: Int,
    val field_key: String, // ITEM_NAME, QTY, RATE
    val width_pct: Int,
    val align_type: String,
    val sort_order: Int
)

@Entity(
    tableName = "tbl_kot_settings",
    foreignKeys = [ForeignKey(
        entity = PrintTemplateEntity::class,
        parentColumns = ["template_id"],
        childColumns = ["template_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class KotSettingsEntity(
    @PrimaryKey val template_id: Int,
    val print_item_notes: Boolean = true,
    val print_order_time: Boolean = true,
    val group_by_category: Boolean = false,
    val show_running_no: Boolean = true
)

@Entity(
    tableName = "tbl_print_platform_override",
    foreignKeys = [ForeignKey(
        entity = PrintTemplateEntity::class,
        parentColumns = ["template_id"],
        childColumns = ["template_id"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class PrintPlatformOverrideEntity(
    @PrimaryKey(autoGenerate = true) val override_id: Int = 0,
    val template_id: Int,
    val platform: String,
    val dpi: Int?,
    val char_width: Int?,
    val supports_image: Boolean = true,
    val supports_qr: Boolean = true
)
