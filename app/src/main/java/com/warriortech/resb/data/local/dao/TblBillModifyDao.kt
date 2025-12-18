package com.warriortech.resb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.TblBillModify

@Dao
interface TblBillModifyDao {
    @Query("SELECT * FROM tbl_bill_modify")

    fun getAll(): Flow<List<TblBillModify>>

    @Query("SELECT * FROM tbl_bill_modify WHERE bill_modify_id = :id")
    suspend fun getById(id: Int): TblBillModify?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblBillModify)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblBillModify>)

    @Update
    suspend fun update(item: TblBillModify)

    @Delete
    suspend fun delete(item: TblBillModify)

    @Query("SELECT * FROM tbl_bill_modify WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblBillModify>
}