package com.warriortech.resb.data.repository

import com.warriortech.resb.data.local.dao.TableDao
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblTableEntity
import timber.log.Timber
import com.warriortech.resb.model.Area
import com.warriortech.resb.model.Table
import com.warriortech.resb.model.TableStatusResponse
import com.warriortech.resb.model.TblTable
import com.warriortech.resb.network.ApiService
import com.warriortech.resb.network.SessionManager
import com.warriortech.resb.util.NetworkMonitor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TableRepository @Inject constructor(
    private val tableDao: TableDao,
    private val apiService: ApiService,
    networkMonitor: NetworkMonitor,
    private val sessionManager: SessionManager
) : OfflineFirstRepository(networkMonitor) {

    private val connectionState = networkMonitor.isOnline

    // Offline-first approach: Always return local data immediately, sync in background
    suspend fun getAllTables(): Flow<List<Table>> = flow {

        try {
            val response = apiService.getAllTables(sessionManager.getCompanyCode() ?: "")
            syncTablesFromRemote()
            if (response.isSuccessful) {
                emit(response.body()!!)
            } else {
                throw Exception("Failed to fetch table: ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun getActiveTables(): Flow<List<TableStatusResponse>> = flow {
        try {
            val response = apiService.getActiveTables(sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {
                emit(response.body()!!)
            } else {
                throw Exception("Failed to fetch table: ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun deleteTable(tableId: Int) {
        val response = apiService.deleteTable(tableId, sessionManager.getCompanyCode() ?: "")
        if (!response.isSuccessful) {
            throw Exception("Failed to delete table: ${response.message()}")
        }
    }

    suspend fun getAllAreas(): List<Area> {
        return if (isOnline()) {
            safeApiCall(
                apiCall = { apiService.getAllAreas(sessionManager.getCompanyCode() ?: "").body()!! }
            ) ?: emptyList()
        } else {
            // Return cached areas or empty list if offline
            emptyList()
        }
    }

    fun getTablesBySection(section: Long): Flow<List<TableStatusResponse>> = flow {
        try {
            val response =
                apiService.getTablesBySection(section, sessionManager.getCompanyCode() ?: "")
            if (response.isSuccessful) {

                emit(response.body()!!)
            } else {
                throw Exception("Failed to fetch tables: ${response.message()}")
            }
        } catch (e: Exception) {
            throw e
        }
    }

    suspend fun updateTableStatus(tableId: Long, status: String): Boolean {
        return try {
            // Always update local first
            tableDao.updateTableStatus(tableId, status, SyncStatus.PENDING_SYNC)

            // Try to sync with remote if online
            if (isOnline()) {
                val success = safeApiCall(
                    apiCall = {
                        apiService.updateTableStatus(
                            tableId,
                            status,
                            sessionManager.getCompanyCode() ?: ""
                        )
                    }
                ) != null

                if (success) {
                    // Mark as synced if successful
                    tableDao.updateTableSyncStatus(tableId, SyncStatus.SYNCED)
                }
                success
            } else {
                // Return true for offline - will sync later
                true
            }
        } catch (e: Exception) {
            Timber.e(e, "Error updating table status")
            false
        }
    }

    private suspend fun syncTablesFromRemote() {
        safeApiCall(
            onSuccess = { remoteTables: List<Table> ->
                withContext(Dispatchers.IO) {
                    val entities = remoteTables.map {
                        TblTableEntity(
                            table_id = it.table_id.toInt(),
                            table_name = it.table_name,
                            seating_capacity = it.seating_capacity,
                            is_ac = it.is_ac,
                            table_status = it.table_status,
                            area_id = it.area_id.toInt(),
                            table_availability = it.table_availability,
                            is_active = it.is_active,
                            is_synced = SyncStatus.SYNCED,
                            last_synced_at = System.currentTimeMillis()
                        )
                    }
                    tableDao.insertTables(entities)
                }
            },
            apiCall = { apiService.getAllTables(sessionManager.getCompanyCode() ?: "").body()!! }
        )
    }

    suspend fun forceSyncAllTables() {
        if (isOnline()) {
            syncTablesFromRemote()
        }
    }

    suspend fun getstatus(tableId: Long): String {
        val data = apiService.getTablesByStatus(tableId, sessionManager.getCompanyCode() ?: "")
        return data.is_ac
    }

    suspend fun insertTable(table: TblTable) {
        try {
            // Validate table data
            if (table.table_name.isBlank()) {
                throw IllegalArgumentException("Table name cannot be empty")
            }

            // First, insert to local database
            val entity = TblTableEntity(
                table_id = 0, // Let Room auto-generate the ID
                area_id = table.area_id.toInt(),
                table_name = table.table_name,
                seating_capacity = table.seating_capacity,
                is_ac = table.is_ac,
                table_status = table.table_status,
                table_availability = table.table_availability,
                is_active = table.is_active,
                is_synced = SyncStatus.PENDING_SYNC,
                last_synced_at = System.currentTimeMillis()
            )

            val insertedId = tableDao.insertTable(entity)
            Timber.d("Table inserted with ID: $insertedId")

            // Then sync with remote if online
            if (isOnline()) {
                try {
                    val response =
                        apiService.createTable(table, sessionManager.getCompanyCode() ?: "")
                    if (response.isSuccessful) {
                        // Update sync status if successful
                        tableDao.updateTableSyncStatus(insertedId, SyncStatus.SYNCED)
                        Timber.d("Table synced successfully")
                    } else {
                        tableDao.updateTableSyncStatus(insertedId, SyncStatus.SYNC_FAILED)
                        Timber.w("Failed to sync table to server: ${response.message()}")
                    }
                } catch (e: Exception) {
                    tableDao.updateTableSyncStatus(insertedId, SyncStatus.SYNC_FAILED)
                    Timber.e(e, "Exception while syncing table to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to insert table: ${table.table_name}")
            throw e
        }
    }

    // Get tables that need to be synced
    suspend fun getPendingSyncTables() = tableDao.getTablesBySyncStatus(SyncStatus.PENDING_SYNC)
    suspend fun updateTable(table: TblTable) {
        try {
            // Update local database first
            val entity = TblTableEntity(
                table_id = table.table_id.toInt(),
                area_id = table.area_id.toInt(),
                table_name = table.table_name,
                seating_capacity = table.seating_capacity,
                is_ac = table.is_ac,
                table_status = table.table_status,
                table_availability = table.table_availability,
                is_active = table.is_active,
                is_synced = SyncStatus.PENDING_SYNC,
                last_synced_at = null,
                created_at = System.currentTimeMillis(),
                updated_at = System.currentTimeMillis()
            )
            tableDao.updateTable(entity)

            // Sync with remote if online
            if (isOnline()) {
                try {
                    val response = apiService.updateTable(
                        table.table_id,
                        table,
                        sessionManager.getCompanyCode() ?: ""
                    )
                    if (response.isSuccessful) {
                        tableDao.updateTableSyncStatus(table.table_id, SyncStatus.SYNCED)
                    } else {
                        tableDao.updateTableSyncStatus(table.table_id, SyncStatus.SYNC_FAILED)
                    }
                } catch (e: Exception) {
                    tableDao.updateTableSyncStatus(table.table_id, SyncStatus.SYNC_FAILED)
                    Timber.e(e, "Failed to sync table update to server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to update table: ${table.table_name}")
            throw e
        }
    }

    suspend fun deleteTable(lng: Long) {
        try {
            // Mark as pending delete in local database
            tableDao.updateTableSyncStatus(lng, SyncStatus.PENDING_DELETE)

            if (isOnline()) {
                try {
                    val response =
                        apiService.deleteTable(lng, sessionManager.getCompanyCode() ?: "")
                    if (response.isSuccessful) {
                        // Actually delete from local database if server delete was successful
                        tableDao.deleteTableById(lng)
                    } else {
                        tableDao.updateTableSyncStatus(lng, SyncStatus.SYNC_FAILED)
                    }
                } catch (e: Exception) {
                    tableDao.updateTableSyncStatus(lng, SyncStatus.SYNC_FAILED)
                    Timber.e(e, "Failed to delete table from server")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to delete table")
            throw e
        }
    }

    suspend fun getTableById(tableId: Long): Table? {
        val entity = tableDao.getTableById(tableId)
        return entity?.let {
            Table(
                table_id = it.table_id.toLong(),
                area_id = it.area_id?.toLong() ?: 0L,
                area_name = "",
                table_name = it.table_name ?: "",
                seating_capacity = it.seating_capacity ?: 0,
                is_ac = it.is_ac ?: "",
                table_status = it.table_status ?: "",
                table_availability = it.table_availability ?: "",
                is_active = it.is_active ?: false
            )
        }
    }

    suspend fun changeTable(sourceTableId: Long, targetTableId: Long): Boolean {
        return try {
            val response = apiService.changeTable(sourceTableId, targetTableId, sessionManager.getCompanyCode() ?: "")
            response.isSuccessful && response.body()?.data == true
        } catch (e: Exception) {
            Timber.e(e, "Error changing table")
            false
        }
    }

    suspend fun mergeTables(tableIds: List<Long>): Boolean {
        return try {
            val response = apiService.mergeTables(tableIds, sessionManager.getCompanyCode() ?: "")
            response.isSuccessful && response.body()?.data == true
        } catch (e: Exception) {
            Timber.e(e, "Error merging tables")
            false
        }
    }
}