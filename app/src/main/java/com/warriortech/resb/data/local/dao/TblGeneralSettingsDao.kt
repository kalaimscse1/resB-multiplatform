package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblGeneralSettingsDao {
    @Query("SELECT * FROM tbl_general_settings")

    fun getAll(): Flow<List<TblGeneralSettings>>

    @Query("SELECT * FROM tbl_general_settings WHERE id = :id")
    suspend fun getById(id: Int): TblGeneralSettings?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblGeneralSettings)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblGeneralSettings>)

    @Update
    suspend fun update(item: TblGeneralSettings)

    @Delete
    suspend fun delete(item: TblGeneralSettings)

    @Query("SELECT * FROM tbl_general_settings WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblGeneralSettings>
}