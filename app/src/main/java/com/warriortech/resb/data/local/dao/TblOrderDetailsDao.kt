package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblOrderDetailsDao {
    @Query("SELECT * FROM tbl_order_details")

    fun getAll(): Flow<List<TblOrderDetails>>

    @Query("SELECT * FROM tbl_order_details WHERE order_details_id = :id")
    suspend fun getById(id: Int): TblOrderDetails?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblOrderDetails)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblOrderDetails>)

    @Update
    suspend fun update(item: TblOrderDetails)

    @Delete
    suspend fun delete(item: TblOrderDetails)

    @Query("SELECT * FROM tbl_order_details WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblOrderDetails>

    @Query(" SELECT IFNULL(MAX(DISTINCT kot_number),0) FROM tbl_order_details od INNER JOIN tbl_order_master om ON od.order_master_id = om.order_master_id WHERE om.order_date =:orderDate")
    suspend fun getMaxKOTNumber(orderDate: String): Int

    @Query("SELECT * FROM tbl_order_details WHERE order_master_id = :orderMasterId AND is_active=1")
    suspend fun getByOrderMasterId(orderMasterId: String): List<TblOrderDetails>
}