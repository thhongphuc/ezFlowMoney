package com.antibudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object quản lý danh mục tài sản 3 Lọ
 */
@Dao
interface WealthAssetDao {

    @Query("SELECT * FROM wealth_assets ORDER BY totalValue DESC")
    fun getAllAssets(): Flow<List<WealthAssetEntity>>

    @Query("SELECT * FROM wealth_assets WHERE bucket = :bucket ORDER BY totalValue DESC")
    fun getAssetsByBucket(bucket: WealthBucket): Flow<List<WealthAssetEntity>>

    @Query("SELECT COALESCE(SUM(totalValue), 0.0) FROM wealth_assets")
    fun getTotalNetWorth(): Flow<Double>

    @Query("SELECT COALESCE(SUM(totalValue), 0.0) FROM wealth_assets")
    suspend fun getTotalNetWorthSync(): Double

    @Query("SELECT COALESCE(SUM(totalValue), 0.0) FROM wealth_assets WHERE bucket = :bucket")
    fun getTotalValueByBucket(bucket: WealthBucket): Flow<Double>

    @Query("SELECT * FROM wealth_assets WHERE id = :id LIMIT 1")
    fun getAssetById(id: Long): Flow<WealthAssetEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAsset(asset: WealthAssetEntity): Long

    @Update
    suspend fun updateAsset(asset: WealthAssetEntity)

    @Delete
    suspend fun deleteAsset(asset: WealthAssetEntity)
}
