package com.antibudget.app.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Bảng lưu cấu hình mục tiêu tài sản cá nhân
 * Cho phép tùy chỉnh năm sinh, tuổi mục tiêu, số tiền mục tiêu và tỷ suất sinh lời kỳ vọng
 */
@Entity(tableName = "wealth_goal")
data class WealthGoalEntity(
    @PrimaryKey
    val id: Long = 1,
    val birthYear: Int = 1994,
    val targetAge: Int = 40,
    val targetAmount: Double = 600_000_000.0,
    val expectedAnnualReturn: Double = 8.0, // Tỷ suất kỳ vọng %/năm (vd: 8.0%)
    val updatedAt: Long = System.currentTimeMillis()
)
