package com.antibudget.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Quản lý chu kỳ ngân sách theo tháng (Payday Allocation)
 * Theo triết lý Anti-Budget: Thu nhập 42tr = 12tr Tích sản (30%) + 17tr Cố định + 13tr Quỹ tự do
 */
@Entity(tableName = "budget_cycles")
data class BudgetCycleEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val month: Int,
    val year: Int,
    val totalIncome: Double = 42_000_000.0,
    val savingsTarget: Double = 12_000_000.0,
    val fixedBillsTotal: Double = 17_000_000.0,
    val variablePool: Double = 13_000_000.0,
    val startDate: Long,
    val endDate: Long,
    val isActive: Boolean = true
)
