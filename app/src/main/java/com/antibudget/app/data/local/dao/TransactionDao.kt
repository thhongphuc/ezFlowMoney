package com.antibudget.app.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.TransactionEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object quản lý các giao dịch thu chi
 */
@Dao
interface TransactionDao {

    @Query("SELECT * FROM transactions WHERE cycleId = :cycleId ORDER BY createdAt DESC")
    fun getTransactionsForCycle(cycleId: Long): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions ORDER BY createdAt DESC LIMIT :limit")
    fun getRecentTransactions(limit: Int = 10): Flow<List<TransactionEntity>>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE cycleId = :cycleId AND bucketType = 'VARIABLE'")
    fun getVariableSpentForCycle(cycleId: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE cycleId = :cycleId AND bucketType = 'VARIABLE'")
    suspend fun getVariableSpentForCycleSync(cycleId: Long): Double

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM transactions WHERE cycleId = :cycleId AND bucketType = :bucketType")
    fun getSpentByBucket(cycleId: Long, bucketType: BucketType): Flow<Double>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTransaction(tx: TransactionEntity): Long

    @Delete
    suspend fun deleteTransaction(tx: TransactionEntity)

    @Query("SELECT * FROM transactions WHERE cycleId = :cycleId AND bucketType = 'FIXED' ORDER BY createdAt DESC")
    fun getFixedBillsForCycle(cycleId: Long): Flow<List<TransactionEntity>>

    @Query("UPDATE transactions SET bucketType = :newBucket WHERE id = :txId")
    suspend fun updateTransactionBucket(txId: Long, newBucket: BucketType)

    @Query("SELECT COUNT(*) FROM transactions WHERE cycleId = :cycleId")
    suspend fun getTransactionCount(cycleId: Long): Int
}
