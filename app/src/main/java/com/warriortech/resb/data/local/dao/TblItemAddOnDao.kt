package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblItemAddOnDao {
    @Query("SELECT * FROM tbl_item_add_on")

    fun getAll(): Flow<List<TblItemAddOn>>

    @Query("SELECT * FROM tbl_item_add_on WHERE item_add_on_id = :id")
    suspend fun getById(id: Int): TblItemAddOn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblItemAddOn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblItemAddOn>)

    @Update
    suspend fun update(item: TblItemAddOn)

    @Delete
    suspend fun delete(item: TblItemAddOn)

    @Query("SELECT * FROM tbl_item_add_on WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblItemAddOn>
}