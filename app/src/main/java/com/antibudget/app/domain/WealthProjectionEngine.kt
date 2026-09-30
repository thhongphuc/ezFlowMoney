package com.antibudget.app.domain

import com.antibudget.app.data.local.entity.WealthGoalEntity
import com.antibudget.app.domain.model.FeasibilityStatus
import com.antibudget.app.domain.model.WealthGoalReport
import com.antibudget.app.domain.model.YearlyMilestone
import java.time.LocalDate
import kotlin.math.max
import kotlin.math.pow

/**
 * Động cơ toán học hoạch định tài chính và lãi kép (Compound Interest Engine)
 */
object WealthProjectionEngine {

    fun calculateReport(
        goal: WealthGoalEntity,
        currentNetWorth: Double,
        monthlyContribution: Double,
        currentYear: Int = LocalDate.now().year
    ): WealthGoalReport {
        val currentAge = max(18, currentYear - goal.birthYear)
        val targetAge = max(currentAge + 1, goal.targetAge)
        val yearsRemaining = targetAge - currentAge
        val monthsRemaining = yearsRemaining * 12

        val pv = max(0.0, currentNetWorth)
        val pmt = max(0.0, monthlyContribution)
        val target = max(1_000_000.0, goal.targetAmount)
        val annualRate = goal.expectedAnnualReturn.coerceIn(0.0, 50.0)
        val monthlyRate = (annualRate / 100.0) / 12.0

        // 1. Tính toán Future Value (FV) ở tuổi mục tiêu
        val compoundFactor = if (monthlyRate > 0.0) (1.0 + monthlyRate).pow(monthsRemaining.toDouble()) else 1.0
        val fvFromPv = pv * compoundFactor
        val annuityFactor = if (monthlyRate > 0.0) (compoundFactor - 1.0) / monthlyRate else monthsRemaining.toDouble()
        val fvFromPmt = pmt * annuityFactor
        val projectedFutureValue = fvFromPv + fvFromPmt

        // 2. Tính toán số tiền tích sản cần thiết mỗi tháng để chạm đúng target (PMT required)
        val remainingTargetNeeded = max(0.0, target - fvFromPv)
        val requiredMonthlySavings = if (annuityFactor > 0.0) {
            remainingTargetNeeded / annuityFactor
        } else {
            0.0
        }

        val surplusDeficit = projectedFutureValue - target
        val progressPercentage = ((pv / target) * 100.0).toFloat().coerceIn(0f, 100f)

        // 3. Đánh giá tính khả thi
        val status: FeasibilityStatus
        val adviceMessage: String

        when {
            projectedFutureValue >= target * 1.05 -> {
                status = FeasibilityStatus.SURPLUS
                val extraYears = ((projectedFutureValue - target) / (pmt * 12 + (pv * annualRate / 100.0))).coerceAtLeast(0.5)
                adviceMessage = "Tuyệt vời! Với mức tích sản hiện tại và sức mạnh lãi kép ${String.format("%.1f", annualRate)}%/năm, bạn dự kiến sẽ vượt mục tiêu ở tuổi $targetAge."
            }
            projectedFutureValue >= target * 0.95 -> {
                status = FeasibilityStatus.BALANCED
                adviceMessage = "Kế hoạch rất cân đối! Bạn đang bám sát tiến độ đạt mốc tài chính tuổi $targetAge một cách bền vững."
            }
            else -> {
                status = FeasibilityStatus.NEEDS_BOOST
                val gapMonthly = max(0.0, requiredMonthlySavings - pmt)
                adviceMessage = "Cần tăng cường: Hãy cân nhắc tăng thêm tích sản khoảng ${String.format("%,.0f", gapMonthly)} ₫/tháng hoặc nâng tỷ trọng Quỹ tăng trưởng."
            }
        }

        // 4. Sinh lộ trình tích sản theo từng năm tuổi (Yearly Milestones)
        val milestones = mutableListOf<YearlyMilestone>()
        for (y in 0..yearsRemaining) {
            val age = currentAge + y
            val year = currentYear + y
            val months = y * 12

            val cFactor = if (monthlyRate > 0.0) (1.0 + monthlyRate).pow(months.toDouble()) else 1.0
            val aFactor = if (monthlyRate > 0.0) (cFactor - 1.0) / monthlyRate else months.toDouble()
            val valueAtAge = (pv * cFactor) + (pmt * aFactor)
            val deposited = pv + (pmt * months)
            val interest = max(0.0, valueAtAge - deposited)

            milestones.add(
                YearlyMilestone(
                    age = age,
                    year = year,
                    projectedValue = valueAtAge,
                    totalContributed = deposited,
                    interestEarned = interest
                )
            )
        }

        return WealthGoalReport(
            birthYear = goal.birthYear,
            currentAge = currentAge,
            targetAge = targetAge,
            yearsRemaining = yearsRemaining,
            monthsRemaining = monthsRemaining,
            targetAmount = target,
            currentNetWorth = pv,
            progressPercentage = progressPercentage,
            monthlyContribution = pmt,
            expectedAnnualReturn = annualRate,
            projectedFutureValue = projectedFutureValue,
            requiredMonthlySavings = requiredMonthlySavings,
            surplusDeficitAmount = surplusDeficit,
            status = status,
            adviceMessage = adviceMessage,
            yearlyMilestones = milestones
        )
    }
}
