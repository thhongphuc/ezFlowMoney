package com.antibudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antibudget.app.data.local.entity.WealthGoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object quản lý cấu hình mục tiêu tài sản
 */
@Dao
interface WealthGoalDao {

    @Query("SELECT * FROM wealth_goal WHERE id = 1 LIMIT 1")
    fun getGoal(): Flow<WealthGoalEntity?>

    @Query("SELECT * FROM wealth_goal WHERE id = 1 LIMIT 1")
    suspend fun getGoalSync(): WealthGoalEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGoal(goal: WealthGoalEntity)
}
