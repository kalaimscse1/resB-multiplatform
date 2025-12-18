package com.warriortech.resb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.TblBilling

@Dao
interface TblBillingDao {
    @Query("SELECT * FROM tbl_billing")

    fun getAll(): Flow<List<TblBilling>>

    @Query("SELECT * FROM tbl_billing WHERE bill_no = :id")
    suspend fun getById(id: String): TblBilling?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblBilling)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblBilling>)

    @Update
    suspend fun update(item: TblBilling)

    @Delete
    suspend fun delete(item: TblBilling)

    @Query("SELECT * FROM tbl_billing WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblBilling>
}