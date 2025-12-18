package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblCounterDao {
    @Query("SELECT * FROM tbl_counter")

    fun getAll(): Flow<List<TblCounter>>

    @Query("SELECT * FROM tbl_counter WHERE counter_id = :id")
    suspend fun getById(id: Int): TblCounter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblCounter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblCounter>)

    @Update
    suspend fun update(item: TblCounter)

    @Delete
    suspend fun delete(item: TblCounter)

    @Query("SELECT * FROM tbl_counter WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblCounter>
}