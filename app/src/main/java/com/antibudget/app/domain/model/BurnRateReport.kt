package com.antibudget.app.domain.model

/**
 * Báo cáo chi tiết chỉ số Burn-Rate và ngân sách an toàn ngày
 */
data class BurnRateReport(
    val dailySafeToSpend: Double,
    val remainingVariablePool: Double,
    val variablePoolTotal: Double,
    val spentVariablePool: Double,
    val daysRemaining: Int,
    val daysInMonth: Int,
    val initialDailyBudget: Double,
    val burnRatePercentage: Float,
    val status: BurnRateStatus,
    val statusMessage: String
)
