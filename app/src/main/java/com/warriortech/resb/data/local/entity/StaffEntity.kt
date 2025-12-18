package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey


@Entity(
    tableName = "tbl_staff",
    foreignKeys = [
        ForeignKey(entity = TblCounter::class, parentColumns = ["counter_id"], childColumns = ["counter_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblRole::class, parentColumns = ["role_id"], childColumns = ["role_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblArea::class, parentColumns = ["area_id"], childColumns = ["area_id"], onDelete = ForeignKey.CASCADE),
                  ],
    indices = [
        Index(value = ["counter_id"]),
        Index(value = ["role_id"]),
        Index(value = ["area_id"]),
    ]
)
data class TblStaff(
    @PrimaryKey(autoGenerate = true) val staff_id: Int = 0,
    val staff_name: String?,
    val contact_no: String?,
    val address: String?,
    val user_name: String?,
    val password: String?,
    val role_id: Int?,
    val last_login: String?,
    val commission: Double?,
    val is_block: Boolean?,
    val counter_id: Int?,
    val area_id: Int?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)