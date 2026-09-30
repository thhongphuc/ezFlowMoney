package com.antibudget.app.data.repository

import com.antibudget.app.data.local.AppDatabase
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

/**
 * Hiện thực FinanceRepository với Room Database
 */
class FinanceRepositoryImpl(private val database: AppDatabase) : FinanceRepository {

    private val cycleDao = database.budgetCycleDao()
    private val transactionDao = database.transactionDao()
    private val wealthDao = database.wealthAssetDao()
    private val goalDao = database.wealthGoalDao()

    override fun getActiveCycle(): Flow<BudgetCycleEntity?> {
        return cycleDao.getActiveCycle()
    }

    override suspend fun getActiveCycleSync(): BudgetCycleEntity? = withContext(Dispatchers.IO) {
        cycleDao.getActiveCycleSync()
    }

    override suspend fun createNewCycle(
        totalIncome: Double,
        savingsTarget: Double,
        fixedBills: Double,
        variablePool: Double
    ): Long = withContext(Dispatchers.IO) {
        val now = LocalDate.now()
        val startOfMonth = now.withDayOfMonth(1)
            .atStartOfDay(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()
        val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
            .atTime(23, 59, 59)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli()

        cycleDao.deactivateAllCycles()

        val newCycle = BudgetCycleEntity(
            month = now.monthValue,
            year = now.year,
            totalIncome = totalIncome,
            savingsTarget = savingsTarget,
            fixedBillsTotal = fixedBills,
            variablePool = variablePool,
            startDate = startOfMonth,
            endDate = endOfMonth,
            isActive = true
        )
        cycleDao.insertCycle(newCycle)
    }

    override suspend fun updateCycle(cycle: BudgetCycleEntity) = withContext(Dispatchers.IO) {
        cycleDao.updateCycle(cycle)
    }

    override fun getTransactionsForCycle(cycleId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getTransactionsForCycle(cycleId)
    }

    override fun getRecentTransactions(limit: Int): Flow<List<TransactionEntity>> {
        return transactionDao.getRecentTransactions(limit)
    }

    override fun getFixedBillsForCycle(cycleId: Long): Flow<List<TransactionEntity>> {
        return transactionDao.getFixedBillsForCycle(cycleId)
    }

    override fun getVariableSpent(cycleId: Long): Flow<Double> {
        return transactionDao.getVariableSpentForCycle(cycleId)
    }

    override suspend fun getVariableSpentSync(cycleId: Long): Double = withContext(Dispatchers.IO) {
        transactionDao.getVariableSpentForCycleSync(cycleId)
    }

    override suspend fun addTransaction(
        amount: Double,
        note: String,
        bankSource: String,
        bucketType: BucketType
    ): Long = withContext(Dispatchers.IO) {
        var activeCycle = cycleDao.getActiveCycleSync()
        if (activeCycle == null) {
            val now = LocalDate.now()
            val startOfMonth = now.withDayOfMonth(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
            val endOfMonth = now.withDayOfMonth(now.lengthOfMonth())
                .atTime(23, 59, 59)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()

            val initialCycle = BudgetCycleEntity(
                month = now.monthValue,
                year = now.year,
                totalIncome = 42_000_000.0,
                savingsTarget = 12_000_000.0,
                fixedBillsTotal = 17_000_000.0,
                variablePool = 13_000_000.0,
                startDate = startOfMonth,
                endDate = endOfMonth,
                isActive = true
            )
            val newCycleId = cycleDao.insertCycle(initialCycle)
            activeCycle = initialCycle.copy(id = newCycleId)
        }

        val tx = TransactionEntity(
            cycleId = activeCycle.id,
            amount = amount,
            bucketType = bucketType,
            note = note,
            bankSource = bankSource,
            createdAt = System.currentTimeMillis()
        )
        transactionDao.insertTransaction(tx)
    }

    override suspend fun updateTransactionBucket(txId: Long, newBucket: BucketType) = withContext(Dispatchers.IO) {
        transactionDao.updateTransactionBucket(txId, newBucket)
    }

    override suspend fun deleteTransaction(tx: TransactionEntity) = withContext(Dispatchers.IO) {
        transactionDao.deleteTransaction(tx)
    }

    override fun getAllWealthAssets(): Flow<List<WealthAssetEntity>> {
        return wealthDao.getAllAssets()
    }

    override fun getAssetsByBucket(bucket: WealthBucket): Flow<List<WealthAssetEntity>> {
        return wealthDao.getAssetsByBucket(bucket)
    }

    override fun getTotalNetWorth(): Flow<Double> {
        return wealthDao.getTotalNetWorth()
    }

    override suspend fun getTotalNetWorthSync(): Double = withContext(Dispatchers.IO) {
        wealthDao.getTotalNetWorthSync()
    }

    override suspend fun addWealthAsset(asset: WealthAssetEntity): Long = withContext(Dispatchers.IO) {
        val total = asset.units * asset.unitPrice
        wealthDao.insertAsset(asset.copy(totalValue = total, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun updateWealthAsset(asset: WealthAssetEntity) = withContext(Dispatchers.IO) {
        val total = asset.units * asset.unitPrice
        wealthDao.updateAsset(asset.copy(totalValue = total, updatedAt = System.currentTimeMillis()))
    }

    override suspend fun deleteWealthAsset(asset: WealthAssetEntity) = withContext(Dispatchers.IO) {
        wealthDao.deleteAsset(asset)
    }

    override fun getWealthGoal(): Flow<com.antibudget.app.data.local.entity.WealthGoalEntity?> {
        return goalDao.getGoal()
    }

    override suspend fun getWealthGoalSync(): com.antibudget.app.data.local.entity.WealthGoalEntity? = withContext(Dispatchers.IO) {
        goalDao.getGoalSync()
    }

    override suspend fun updateWealthGoal(
        birthYear: Int,
        targetAge: Int,
        targetAmount: Double,
        returnRate: Double
    ) = withContext(Dispatchers.IO) {
        val updatedGoal = com.antibudget.app.data.local.entity.WealthGoalEntity(
            id = 1,
            birthYear = birthYear,
            targetAge = targetAge,
            targetAmount = targetAmount,
            expectedAnnualReturn = returnRate,
            updatedAt = System.currentTimeMillis()
        )
        goalDao.upsertGoal(updatedGoal)
    }
}
