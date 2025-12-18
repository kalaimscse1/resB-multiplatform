package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.warriortech.resb.model.Modifiers
@Entity(tableName = "tbl_tax")
data class TblTax(
    @PrimaryKey(autoGenerate = true) val tax_id: Int = 0,
    val tax_name: String?,
    val tax_percentage: String?,
    val cess_percentage: String?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)
