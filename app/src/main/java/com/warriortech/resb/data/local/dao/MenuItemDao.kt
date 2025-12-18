package com.warriortech.resb.data.local.dao

import androidx.room.*
import com.warriortech.resb.data.local.entity.SyncStatus
import com.warriortech.resb.data.local.entity.TblMenuItem
import kotlinx.coroutines.flow.Flow

@Dao
interface MenuItemDao {
    @Query("SELECT * FROM tbl_menu_item")
    fun getAllMenuItems(): Flow<List<TblMenuItem>>

    @Query("SELECT * FROM tbl_menu_item WHERE menu_item_id = :id")
    suspend fun getMenuItemById(id: Long): TblMenuItem?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItem(menuItem: TblMenuItem): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMenuItems(menuItems: List<TblMenuItem>)

    @Update
    suspend fun updateMenuItem(menuItem: TblMenuItem)

    @Delete
    suspend fun deleteMenuItem(menuItem: TblMenuItem)

    @Query("SELECT * FROM tbl_menu_item WHERE last_synced_at = :syncStatus")
    suspend fun getMenuItemsBySyncStatus(syncStatus: SyncStatus): List<TblMenuItem>

    @Query("UPDATE tbl_menu_item SET last_synced_at = :newStatus WHERE menu_item_id = :id")
    suspend fun updateMenuItemSyncStatus(id: Long, newStatus: SyncStatus)
}