package com.warriortech.resb.util

import android.content.Context
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DatabaseDebugUtils @Inject constructor() {

    fun logDatabaseInfo(context: Context) {
        try {
            val dbPath = context.getDatabasePath("resb_database").absolutePath
            Timber.d("Database location: $dbPath")

            // Add more database inspection logic here
            // You can query your database tables and log their contents

        } catch (e: Exception) {
            Timber.e(e, "Error inspecting database")
        }
    }

    fun exportDatabaseContents() {
        // Add logic to export database contents to a file
        // This can help you inspect the database manually
    }
}
