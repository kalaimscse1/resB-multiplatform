package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblVoucherTypeDao {
    @Query("SELECT * FROM tbl_voucher_type")

    fun getAll(): Flow<List<TblVoucherType>>

    @Query("SELECT * FROM tbl_voucher_type WHERE voucher_type_id = :id")
    suspend fun getById(id: Int): TblVoucherType?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblVoucherType)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblVoucherType>)

    @Update
    suspend fun update(item: TblVoucherType)

    @Delete
    suspend fun delete(item: TblVoucherType)

    @Query("SELECT * FROM tbl_voucher_type WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblVoucherType>
}