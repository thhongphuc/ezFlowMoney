package com.antibudget.app.data.repository

import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import kotlinx.coroutines.flow.Flow

/**
 * Interface cho Finance Repository theo Clean Architecture
 */
interface FinanceRepository {

    // Budget Cycle
    fun getActiveCycle(): Flow<BudgetCycleEntity?>
    suspend fun getActiveCycleSync(): BudgetCycleEntity?
    suspend fun createNewCycle(
        totalIncome: Double = 42_000_000.0,
        savingsTarget: Double = 12_000_000.0,
        fixedBills: Double = 17_000_000.0,
        variablePool: Double = 13_000_000.0
    ): Long
    suspend fun updateCycle(cycle: BudgetCycleEntity)

    // Transactions
    fun getTransactionsForCycle(cycleId: Long): Flow<List<TransactionEntity>>
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>
    fun getFixedBillsForCycle(cycleId: Long): Flow<List<TransactionEntity>>
    fun getVariableSpent(cycleId: Long): Flow<Double>
    suspend fun getVariableSpentSync(cycleId: Long): Double
    suspend fun addTransaction(
        amount: Double,
        note: String,
        bankSource: String = "Thủ công",
        bucketType: BucketType = BucketType.VARIABLE
    ): Long
    suspend fun updateTransactionBucket(txId: Long, newBucket: BucketType)
    suspend fun deleteTransaction(tx: TransactionEntity)

    // Wealth Assets (3 Lọ)
    fun getAllWealthAssets(): Flow<List<WealthAssetEntity>>
    fun getAssetsByBucket(bucket: WealthBucket): Flow<List<WealthAssetEntity>>
    fun getTotalNetWorth(): Flow<Double>
    suspend fun getTotalNetWorthSync(): Double
    suspend fun addWealthAsset(asset: WealthAssetEntity): Long
    suspend fun updateWealthAsset(asset: WealthAssetEntity)
    suspend fun deleteWealthAsset(asset: WealthAssetEntity)

    // Wealth Goal Configuration
    fun getWealthGoal(): Flow<com.antibudget.app.data.local.entity.WealthGoalEntity?>
    suspend fun getWealthGoalSync(): com.antibudget.app.data.local.entity.WealthGoalEntity?
    suspend fun updateWealthGoal(
        birthYear: Int,
        targetAge: Int,
        targetAmount: Double,
        returnRate: Double
    )
}

