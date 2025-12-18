package com.warriortech.resb.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_2_3 = object : Migration(2, 3) {
    override fun migrate(database: SupportSQLiteDatabase) {
        database.execSQL("ALTER TABLE menu_items ADD COLUMN preparation_time INTEGER NOT NULL DEFAULT 0")
    }
}