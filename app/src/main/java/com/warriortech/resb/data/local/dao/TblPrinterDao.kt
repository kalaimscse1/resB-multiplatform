package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblPrinterDao {
    @Query("SELECT * FROM tbl_printer")

    fun getAll(): Flow<List<TblPrinter>>

    @Query("SELECT * FROM tbl_printer WHERE printer_id = :id")
    suspend fun getById(id: Int): TblPrinter?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblPrinter)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblPrinter>)

    @Update
    suspend fun update(item: TblPrinter)

    @Delete
    suspend fun delete(item: TblPrinter)

    @Query("SELECT * FROM tbl_printer WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblPrinter>
}
