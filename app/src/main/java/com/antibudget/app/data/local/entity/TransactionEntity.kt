package com.antibudget.app.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Bảng lưu trữ chi tiết các giao dịch chi tiêu hoặc tích sản
 */
@Entity(
    tableName = "transactions",
    foreignKeys = [
        ForeignKey(
            entity = BudgetCycleEntity::class,
            parentColumns = ["id"],
            childColumns = ["cycleId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["cycleId"]),
        Index(value = ["createdAt"])
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val cycleId: Long,
    val amount: Double,
    val bucketType: BucketType = BucketType.VARIABLE,
    val note: String,
    val bankSource: String = "Thủ công",
    val createdAt: Long = System.currentTimeMillis()
)
