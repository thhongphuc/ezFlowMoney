package com.antibudget.app.domain

import com.antibudget.app.domain.model.BurnRateReport
import com.antibudget.app.domain.model.BurnRateStatus
import java.time.LocalDate
import kotlin.math.max

/**
 * Động cơ tính toán chỉ số Burn-Rate và ngân sách Daily Safe-to-Spend
 * Công thức: DailySafeToSpend = (VariablePool - Đã_tiêu_quỹ_tự_do) / Số_ngày_còn_lại_trong_tháng
 */
object BurnRateCalculator {

    fun calculate(
        variablePool: Double,
        spentVariable: Double
    ): BurnRateReport {
        val today = LocalDate.now()
        val daysInMonth = today.lengthOfMonth()
        val currentDay = today.dayOfMonth

        // Số ngày còn lại tính cả ngày hôm nay
        val daysRemaining = max(1, daysInMonth - currentDay + 1)

        val remainingVariablePool = max(0.0, variablePool - spentVariable)
        val dailySafeToSpend = if (remainingVariablePool <= 0.0) 0.0 else remainingVariablePool / daysRemaining
        val initialDailyBudget = if (daysInMonth > 0) variablePool / daysInMonth else 0.0

        val burnRatePercentage = if (variablePool > 0.0) {
            ((spentVariable / variablePool) * 100.0).toFloat().coerceIn(0f, 100f)
        } else {
            100f
        }

        val status: BurnRateStatus
        val statusMessage: String

        when {
            spentVariable >= variablePool || dailySafeToSpend <= 0.0 -> {
                status = BurnRateStatus.DANGER
                statusMessage = "Cảnh báo: Quỹ chi tiêu tự do đã cạn kiệt! Hãy hạn chế tối đa mọi khoản chi không thiết yếu."
            }
            dailySafeToSpend < (initialDailyBudget * 0.70) -> {
                status = BurnRateStatus.WARNING
                statusMessage = "Lưu ý: Tốc độ chi tiêu đang nhanh hơn dự kiến. Hãy kiểm soát trong các ngày tới."
            }
            else -> {
                status = BurnRateStatus.SAFE
                statusMessage = "Hạn mức tối đa bạn có thể thoải mái chi tiêu hôm nay mà không lo thâm hụt."
            }
        }

        return BurnRateReport(
            dailySafeToSpend = dailySafeToSpend,
            remainingVariablePool = remainingVariablePool,
            variablePoolTotal = variablePool,
            spentVariablePool = spentVariable,
            daysRemaining = daysRemaining,
            daysInMonth = daysInMonth,
            initialDailyBudget = initialDailyBudget,
            burnRatePercentage = burnRatePercentage,
            status = status,
            statusMessage = statusMessage
        )
    }
}
