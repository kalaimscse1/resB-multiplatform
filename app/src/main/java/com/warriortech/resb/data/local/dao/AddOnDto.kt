package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblAddOnDao {
    @Query("SELECT * FROM tbl_add_on")

    fun getAll(): Flow<List<TblAddOn>>

    @Query("SELECT * FROM tbl_add_on WHERE add_on_id = :id")
    suspend fun getById(id: Int): TblAddOn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblAddOn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblAddOn>)

    @Update
    suspend fun update(item: TblAddOn)

    @Delete
    suspend fun delete(item: TblAddOn)

    @Query("SELECT * FROM tbl_add_on WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblAddOn>
}