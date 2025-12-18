package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblVoucherDao {
    @Query("SELECT * FROM tbl_voucher")

    fun getAll(): Flow<List<TblVoucher>>

    @Query("SELECT * FROM tbl_voucher WHERE voucher_id = :id")
    suspend fun getById(id: Int): TblVoucher?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblVoucher)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblVoucher>)

    @Update
    suspend fun update(item: TblVoucher)

    @Delete
    suspend fun delete(item: TblVoucher)

    @Query("SELECT * FROM tbl_voucher WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblVoucher>

    @Query(
        """SELECT tv.* FROM tbl_voucher tv INNER JOIN tbl_voucher_type vt ON vt.voucher_type_id = tv.voucher_type
                    WHERE tv.counter_id=:counterId AND tv.is_active=1 AND vt.voucher_type_name =:type"""
    )
    suspend fun getActiveVoucherByType(counterId: Int, type: String): TblVoucher?
}