package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblOrderMasterDao {
    @Query("SELECT * FROM tbl_order_master")

    fun getAll(): Flow<List<TblOrderMaster>>

    @Query("SELECT * FROM tbl_order_master WHERE order_master_id = :id")
    suspend fun getById(id: String): TblOrderMaster?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblOrderMaster)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblOrderMaster>)

    @Update
    suspend fun update(item: TblOrderMaster)

    @Delete
    suspend fun delete(item: TblOrderMaster)

    @Query("SELECT * FROM tbl_order_master WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblOrderMaster>
}