package com.antibudget.app.data.local.entity

/**
 * 3 Lọ tích sản theo mô hình tài sản gia đình
 */
enum class WealthBucket(val displayName: String, val unitLabel: String) {
    BANK_SAVINGS("Tiền gửi ngân hàng", "Sổ"),
    GOLD_9999("Vàng nhẫn 9999", "Chỉ"),
    FUND_NAV("Chứng chỉ quỹ", "CCQ")
}
