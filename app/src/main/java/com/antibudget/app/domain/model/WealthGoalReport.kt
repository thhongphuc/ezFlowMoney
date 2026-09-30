package com.antibudget.app.domain.model

/**
 * Đánh giá tính khả thi của mục tiêu tài chính
 */
enum class FeasibilityStatus(val label: String) {
    SURPLUS("Vượt mục tiêu"),
    BALANCED("Đúng kế hoạch"),
    NEEDS_BOOST("Cần tăng tốc")
}

/**
 * Cột mốc tích lũy dự phóng theo từng năm tuổi
 */
data class YearlyMilestone(
    val age: Int,
    val year: Int,
    val projectedValue: Double,
    val totalContributed: Double,
    val interestEarned: Double
)

/**
 * Báo cáo chi tiết mục tiêu tài chính tích sản và phân tích lãi kép
 */
data class WealthGoalReport(
    val birthYear: Int,
    val currentAge: Int,
    val targetAge: Int,
    val yearsRemaining: Int,
    val monthsRemaining: Int,
    val targetAmount: Double,
    val currentNetWorth: Double,
    val progressPercentage: Float,
    val monthlyContribution: Double,
    val expectedAnnualReturn: Double,
    val projectedFutureValue: Double,
    val requiredMonthlySavings: Double,
    val surplusDeficitAmount: Double,
    val status: FeasibilityStatus,
    val adviceMessage: String,
    val yearlyMilestones: List<YearlyMilestone>
)
