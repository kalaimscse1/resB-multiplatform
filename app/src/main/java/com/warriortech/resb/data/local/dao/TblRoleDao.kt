package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblRoleDao {
    @Query("SELECT * FROM tbl_role")

    fun getAll(): Flow<List<TblRole>>

    @Query("SELECT * FROM tbl_role WHERE role_id = :id")
    suspend fun getById(id: Int): TblRole?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblRole)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblRole>)

    @Update
    suspend fun update(item: TblRole)

    @Delete
    suspend fun delete(item: TblRole)

    @Query("SELECT * FROM tbl_role WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblRole>
}