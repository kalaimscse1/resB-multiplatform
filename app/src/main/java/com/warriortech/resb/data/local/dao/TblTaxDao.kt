package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblTaxDao {
    @Query("SELECT * FROM tbl_tax")

    fun getAll(): Flow<List<TblTax>>

    @Query("SELECT * FROM tbl_tax WHERE tax_id = :id")
    suspend fun getById(id: Int): TblTax?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblTax)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblTax>)

    @Update
    suspend fun update(item: TblTax)

    @Delete
    suspend fun delete(item: TblTax)

    @Query("SELECT * FROM tbl_tax WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblTax>
}