package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblTaxSplitDao {
    @Query("SELECT * FROM tbl_tax_split")

    fun getAll(): Flow<List<TblTaxSplit>>

    @Query("SELECT * FROM tbl_tax_split WHERE tax_split_id = :id")
    suspend fun getById(id: Int): TblTaxSplit?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblTaxSplit)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblTaxSplit>)

    @Update
    suspend fun update(item: TblTaxSplit)

    @Delete
    suspend fun delete(item: TblTaxSplit)

    @Query("SELECT * FROM tbl_tax_split WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblTaxSplit>
}
