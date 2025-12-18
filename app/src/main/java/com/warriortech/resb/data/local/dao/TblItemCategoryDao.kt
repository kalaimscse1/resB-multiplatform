package com.warriortech.resb.data.local.dao

import androidx.room.*
import kotlinx.coroutines.flow.Flow
import com.warriortech.resb.data.local.entity.*

@Dao
interface TblItemCategoryDao {
    @Query("SELECT * FROM tbl_item_category")

    fun getAll(): Flow<List<TblItemCategory>>

    @Query("SELECT * FROM tbl_item_category WHERE item_cat_id = :id")
    suspend fun getById(id: Int): TblItemCategory?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(item: TblItemCategory)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(items: List<TblItemCategory>)

    @Update
    suspend fun update(item: TblItemCategory)

    @Delete
    suspend fun delete(item: TblItemCategory)

    @Query("SELECT * FROM tbl_item_category WHERE is_synced = 0")
    suspend fun getUnsynced(): List<TblItemCategory>
}