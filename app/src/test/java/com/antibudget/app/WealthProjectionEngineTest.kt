package com.antibudget.app

import com.antibudget.app.data.local.entity.WealthGoalEntity
import com.antibudget.app.domain.WealthProjectionEngine
import com.antibudget.app.domain.model.FeasibilityStatus
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Kiểm thử đơn vị các công thức toán học tài chính và lãi kép trong WealthProjectionEngine
 */
class WealthProjectionEngineTest {

    @Test
    fun testCompoundInterestFutureValueCalculation() {
        val goal = WealthGoalEntity(
            birthYear = 1994,
            targetAge = 40,
            targetAmount = 600_000_000.0,
            expectedAnnualReturn = 8.0 // 8%/năm
        )

        // Tài sản hiện có: 221.740.000 ₫, tích sản mỗi tháng: 12.000.000 ₫
        val currentNetWorth = 221_740_000.0
        val monthlyContribution = 12_000_000.0
        val currentYear = 2026

        val report = WealthProjectionEngine.calculateReport(
            goal = goal,
            currentNetWorth = currentNetWorth,
            monthlyContribution = monthlyContribution,
            currentYear = currentYear
        )

        // Tuổi hiện tại: 2026 - 1994 = 32 tuổi -> Còn 8 năm (96 tháng)
        assertEquals(32, report.currentAge)
        assertEquals(40, report.targetAge)
        assertEquals(8, report.yearsRemaining)
        assertEquals(96, report.monthsRemaining)

        // Với 221tr gốc + 12tr/tháng trong 8 năm ở mức lãi kép 8%/năm:
        // Tiền gốc gửi thêm: 12tr * 96 = 1.152.000.000 ₫
        // Tổng tiền tương lai FV dự kiến phải > 1.8 Tỷ (vượt xa mục tiêu 600 triệu)
        assertTrue(report.projectedFutureValue > 1_800_000_000.0)
        assertEquals(FeasibilityStatus.SURPLUS, report.status)

        // Kiểm tra danh sách lộ trình Milestone từng năm (từ 32 đến 40 tuổi -> 9 cột mốc)
        assertEquals(9, report.yearlyMilestones.size)
        assertEquals(32, report.yearlyMilestones.first().age)
        assertEquals(40, report.yearlyMilestones.last().age)
    }

    @Test
    fun testRequiredMonthlySavingsWhenBehindTarget() {
        // Trường hợp mục tiêu rất cao (2 Tỷ), thời gian ngắn (4 năm = 48 tháng)
        val goal = WealthGoalEntity(
            birthYear = 1990,
            targetAge = 40, // 2026 - 1990 = 36 tuổi -> Còn 4 năm
            targetAmount = 2_000_000_000.0,
            expectedAnnualReturn = 8.0
        )

        val currentNetWorth = 100_000_000.0
        val monthlyContribution = 5_000_000.0 // Tích sản quá ít so với mục tiêu 2 tỷ
        val currentYear = 2026

        val report = WealthProjectionEngine.calculateReport(
            goal = goal,
            currentNetWorth = currentNetWorth,
            monthlyContribution = monthlyContribution,
            currentYear = currentYear
        )

        // FV chắc chắn < 2 Tỷ -> Báo trạng thái NEEDS_BOOST (Cần tăng tốc)
        assertEquals(FeasibilityStatus.NEEDS_BOOST, report.status)
        // Số tiền cần trích mỗi tháng phải lớn hơn 5 triệu
        assertTrue(report.requiredMonthlySavings > 25_000_000.0)
    }
}
