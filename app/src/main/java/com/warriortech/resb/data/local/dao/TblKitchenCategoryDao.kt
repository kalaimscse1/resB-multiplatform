package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblKitchenCategoryDao {
    @Query("SELECT * FROM tbl_kitchen_category")

    fun getAll(): Flow<List<TblKitchenCategory>>

    @Query("SELECT * FROM tbl_kitchen_category WHERE kitchen_cat_id = :id")
    suspend fun getById(id: Int): TblKitchenCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblKitchenCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblKitchenCategory>)

    @Update
    suspend fun update(item: TblKitchenCategory)

    @Delete
    suspend fun delete(item: TblKitchenCategory)

    @Query("SELECT * FROM tbl_kitchen_category WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblKitchenCategory>
}