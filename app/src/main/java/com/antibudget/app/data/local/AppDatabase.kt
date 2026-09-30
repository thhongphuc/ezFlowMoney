package com.antibudget.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.sqlite.db.SupportSQLiteDatabase
import com.antibudget.app.data.local.dao.BudgetCycleDao
import com.antibudget.app.data.local.dao.TransactionDao
import com.antibudget.app.data.local.dao.WealthAssetDao
import com.antibudget.app.data.local.dao.WealthGoalDao
import com.antibudget.app.data.local.entity.WealthGoalEntity
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

/**
 * Cơ sở dữ liệu Room cục bộ cho ứng dụng Anti-Budget
 */
@Database(
    entities = [
        BudgetCycleEntity::class,
        TransactionEntity::class,
        WealthAssetEntity::class,
        WealthGoalEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(AppConverters::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun budgetCycleDao(): BudgetCycleDao
    abstract fun transactionDao(): TransactionDao
    abstract fun wealthAssetDao(): WealthAssetDao
    abstract fun wealthGoalDao(): WealthGoalDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "antibudget_database.db"
                )
                    .fallbackToDestructiveMigration()
                    .addCallback(DatabaseCallback())
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: AppDatabase) {
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

                // Khởi tạo chu kỳ ngân sách mặc định (Payday Allocation)
                val defaultCycle = BudgetCycleEntity(
                    month = now.monthValue,
                    year = now.year,
                    totalIncome = 42_000_000.0,
                    savingsTarget = 12_000_000.0, // 30%
                    fixedBillsTotal = 17_000_000.0, // Nhà 8tr + Học phí 7.5tr + Điện nước 1.5tr
                    variablePool = 13_000_000.0, // Quỹ tự do
                    startDate = startOfMonth,
                    endDate = endOfMonth,
                    isActive = true
                )
                val cycleId = database.budgetCycleDao().insertCycle(defaultCycle)

                // Một số giao dịch mẫu ban đầu
                val tx1 = TransactionEntity(
                    cycleId = cycleId,
                    amount = 65_000.0,
                    bucketType = BucketType.VARIABLE,
                    note = "Cafe sáng gia đình",
                    bankSource = "VCB Digibank",
                    createdAt = System.currentTimeMillis() - 7_200_000
                )
                val tx2 = TransactionEntity(
                    cycleId = cycleId,
                    amount = 210_000.0,
                    bucketType = BucketType.VARIABLE,
                    note = "Siêu thị WinMart mua rau quả",
                    bankSource = "Techcombank",
                    createdAt = System.currentTimeMillis() - 86_400_000
                )
                val tx3 = TransactionEntity(
                    cycleId = cycleId,
                    amount = 1_500_000.0,
                    bucketType = BucketType.FIXED,
                    note = "Tiền điện & nước tháng",
                    bankSource = "MBBank",
                    createdAt = System.currentTimeMillis() - 172_800_000
                )
                database.transactionDao().insertTransaction(tx1)
                database.transactionDao().insertTransaction(tx2)
                database.transactionDao().insertTransaction(tx3)

                // 3 Lọ tài sản ban đầu tích lũy cho mục tiêu 600 triệu tuổi 40
                val assetSavings = WealthAssetEntity(
                    bucket = WealthBucket.BANK_SAVINGS,
                    name = "Sổ tiết kiệm VCB 6 tháng (5.2%/năm)",
                    units = 1.0,
                    unitPrice = 120_000_000.0,
                    totalValue = 120_000_000.0
                )
                val assetGold = WealthAssetEntity(
                    bucket = WealthBucket.GOLD_9999,
                    name = "Vàng nhẫn 9999 PNJ",
                    units = 6.0, // 6 chỉ
                    unitPrice = 8_200_000.0,
                    totalValue = 49_200_000.0
                )
                val assetFund = WealthAssetEntity(
                    bucket = WealthBucket.FUND_NAV,
                    name = "Chứng chỉ quỹ Dragon Capital DCDS",
                    units = 1_850.0,
                    unitPrice = 28_400.0,
                    totalValue = 52_540_000.0
                )

                database.wealthAssetDao().insertAsset(assetSavings)
                database.wealthAssetDao().insertAsset(assetGold)
                database.wealthAssetDao().insertAsset(assetFund)

                // Cấu hình mục tiêu tài chính mặc định
                val defaultGoal = WealthGoalEntity(
                    id = 1,
                    birthYear = 1994,
                    targetAge = 40,
                    targetAmount = 600_000_000.0,
                    expectedAnnualReturn = 8.0
                )
                database.wealthGoalDao().upsertGoal(defaultGoal)
            }
        }
    }
}
