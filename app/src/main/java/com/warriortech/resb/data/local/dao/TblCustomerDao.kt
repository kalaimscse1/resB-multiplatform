package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblCustomerDao {
    @Query("SELECT * FROM tbl_customer")

    fun getAll(): Flow<List<TblCustomers>>

    @Query("SELECT * FROM tbl_customer WHERE customer_id = :id")
    suspend fun getById(id: Int): TblCustomers?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblCustomers)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblCustomers>)

    @Update
    suspend fun update(item: TblCustomers)

    @Delete
    suspend fun delete(item: TblCustomers)

    @Query("SELECT * FROM tbl_customer WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblCustomers>
}