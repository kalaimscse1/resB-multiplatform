package com.warriortech.resb.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tbl_order_master",
    foreignKeys = [
        ForeignKey(entity = TblTableEntity::class, parentColumns = ["table_id"], childColumns = ["table_id"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = TblStaff::class, parentColumns = ["staff_id"], childColumns = ["staff_id"], onDelete = ForeignKey.CASCADE),
    ],
    indices = [
        Index(value = ["table_id"]),
        Index(value = ["staff_id"]),
    ]
)
data class TblOrderMaster(
    @PrimaryKey val order_master_id: String,
    val order_date: String?,
    val order_create_time: String?,
    val order_completed_time: String?,
    val staff_id: Int?,
    val is_dine_in: Boolean?,
    val is_take_away: Boolean?,
    val is_delivery: Boolean?,
    val table_id: Int?,
    val no_of_person: Int?,
    val waiter_request_status: Boolean?,
    val kitchen_response_status: Boolean?,
    val order_status: String?,
    val is_delivered: Boolean?,
    val is_online: Boolean?,
    val online_ref_no: String?,
    val online_order_id: Int?,
    val is_online_paid: Boolean?,
    val is_merge: Boolean?,
    val is_active: Boolean?,
    val is_synced: SyncStatus = SyncStatus.PENDING_SYNC,
    val last_synced_at: Long? = null
)