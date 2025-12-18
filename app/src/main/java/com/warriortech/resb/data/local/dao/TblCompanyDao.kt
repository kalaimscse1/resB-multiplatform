package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblCompanyDao {
    @Query("SELECT * FROM tbl_company")

    fun getAll(): Flow<List<TblCompany>>

    @Query("SELECT * FROM tbl_company WHERE company_code = :id")
    suspend fun getById(id: String): TblCompany?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblCompany)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblCompany>)

    @Update
    suspend fun update(item: TblCompany)

    @Delete
    suspend fun delete(item: TblCompany)

    @Query("SELECT * FROM tbl_company WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblCompany>
}
