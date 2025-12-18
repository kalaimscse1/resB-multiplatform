package com.warriortech.resb.data.local

import androidx.room.TypeConverter
import com.warriortech.resb.data.local.entity.SyncStatus

class Converters {
    @TypeConverter
    fun fromSyncStatus(status: SyncStatus): String {
        return status.name
    }

    @TypeConverter
    fun toSyncStatus(status: String): SyncStatus {
        return try {
            SyncStatus.valueOf(status)
        } catch (e: IllegalArgumentException) {
            SyncStatus.PENDING_SYNC
        }
    }
}