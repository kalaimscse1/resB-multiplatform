package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblAreaDao {
    @Query("SELECT * FROM tbl_area")

    fun getAll(): Flow<List<TblArea>>

    @Query("SELECT * FROM tbl_area WHERE area_id = :id")
    suspend fun getById(id: Int): TblArea?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblArea)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblArea>)

    @Update
    suspend fun update(item: TblArea)

    @Delete
    suspend fun delete(item: TblArea)

    @Query("SELECT * FROM tbl_area WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblArea>
}