package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblMenuDao {
    @Query("SELECT * FROM tbl_menu")

    fun getAll(): Flow<List<TblMenu>>

    @Query("SELECT * FROM tbl_menu WHERE menu_id = :id")
    suspend fun getById(id: Int): TblMenu?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblMenu)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblMenu>)

    @Update
    suspend fun update(item: TblMenu)

    @Delete
    suspend fun delete(item: TblMenu)

    @Query("SELECT * FROM tbl_menu WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblMenu>
}