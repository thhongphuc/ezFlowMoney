package com.antibudget.app.domain.model

/**
 * Trạng thái cảnh báo Burn-Rate theo triết lý Safe-to-Spend
 */
enum class BurnRateStatus(val label: String) {
    SAFE("An toàn"),
    WARNING("Cảnh báo"),
    DANGER("Bội chi")
}
