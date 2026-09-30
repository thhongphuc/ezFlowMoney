package com.antibudget.app.domain.model

/**
 * Tổng hợp tài sản ròng và tiến trình mục tiêu 600 triệu tuổi 40
 */
data class AssetSummary(
    val totalNetWorth: Double,
    val targetNetWorth: Double = 600_000_000.0,
    val progressPercentage: Float,
    val bankSavingsTotal: Double,
    val gold9999Total: Double,
    val fundNavTotal: Double,
    val remainingToTarget: Double
)
