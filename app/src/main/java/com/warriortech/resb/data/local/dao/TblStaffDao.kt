package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblStaffDao {
    @Query("SELECT * FROM tbl_staff")

    fun getAll(): Flow<List<TblStaff>>

    @Query("SELECT * FROM tbl_staff WHERE staff_id = :id")
    suspend fun getById(id: Int): TblStaff?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblStaff)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblStaff>)

    @Update
    suspend fun update(item: TblStaff)

    @Delete
    suspend fun delete(item: TblStaff)

    @Query("SELECT * FROM tbl_staff WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblStaff>
}