package com.antibudget.app.data.local.entity

/**
 * Loại ngân sách trong triết lý Anti-Budget
 */
enum class BucketType(val displayName: String) {
    FIXED("Chi phí cố định"),
    VARIABLE("Quỹ chi tiêu tự do"),
    SAVINGS("Tích sản")
}
