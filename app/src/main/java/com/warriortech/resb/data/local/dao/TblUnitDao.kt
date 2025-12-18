package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblUnitDao {
    @Query("SELECT * FROM tbl_unit")

    fun getAll(): Flow<List<TblUnit>>

    @Query("SELECT * FROM tbl_unit WHERE unit_id = :id")
    suspend fun getById(id: Int): TblUnit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblUnit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblUnit>)

    @Update
    suspend fun update(item: TblUnit)

    @Delete
    suspend fun delete(item: TblUnit)

    @Query("SELECT * FROM tbl_unit WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblUnit>
}