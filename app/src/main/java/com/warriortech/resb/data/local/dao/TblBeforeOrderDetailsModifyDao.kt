package com.warriortech.resb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.warriortech.resb.data.local.entity.TblBeforeOrderDetailsModify
import kotlinx.coroutines.flow.Flow

@Dao
interface TblBeforeOrderDetailsModifyDao {
    @Query("SELECT * FROM tbl_before_order_details_modify")

    fun getAll(): Flow<List<TblBeforeOrderDetailsModify>>

    @Query("SELECT * FROM tbl_before_order_details_modify WHERE order_details_id = :id")
    suspend fun getById(id: Int): TblBeforeOrderDetailsModify?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblBeforeOrderDetailsModify)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblBeforeOrderDetailsModify>)

    @Update
    suspend fun update(item: TblBeforeOrderDetailsModify)

    @Delete
    suspend fun delete(item: TblBeforeOrderDetailsModify)

    @Query("SELECT * FROM tbl_before_order_details_modify WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblBeforeOrderDetailsModify>
}