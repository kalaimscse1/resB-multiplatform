package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblSaleAddOnDao {
    @Query("SELECT * FROM tbl_sale_add_on")

    fun getAll(): Flow<List<TblSaleAddOn>>

    @Query("SELECT * FROM tbl_sale_add_on WHERE sale_add_on_id = :id")
    suspend fun getById(id: Int): TblSaleAddOn?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblSaleAddOn)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblSaleAddOn>)

    @Update
    suspend fun update(item: TblSaleAddOn)

    @Delete
    suspend fun delete(item: TblSaleAddOn)

    @Query("SELECT * FROM tbl_sale_add_on WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblSaleAddOn>
}
