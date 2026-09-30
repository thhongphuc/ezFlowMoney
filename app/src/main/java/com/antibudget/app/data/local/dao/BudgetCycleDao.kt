package com.antibudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object quản lý chu kỳ ngân sách tháng
 */
@Dao
interface BudgetCycleDao {

    @Query("SELECT * FROM budget_cycles WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    fun getActiveCycle(): Flow<BudgetCycleEntity?>

    @Query("SELECT * FROM budget_cycles WHERE isActive = 1 ORDER BY id DESC LIMIT 1")
    suspend fun getActiveCycleSync(): BudgetCycleEntity?

    @Query("SELECT * FROM budget_cycles ORDER BY year DESC, month DESC")
    fun getAllCycles(): Flow<List<BudgetCycleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCycle(cycle: BudgetCycleEntity): Long

    @Update
    suspend fun updateCycle(cycle: BudgetCycleEntity)

    @Query("UPDATE budget_cycles SET isActive = 0 WHERE isActive = 1")
    suspend fun deactivateAllCycles()
}
