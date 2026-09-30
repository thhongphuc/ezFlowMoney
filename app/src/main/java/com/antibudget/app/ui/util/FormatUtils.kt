package com.antibudget.app.ui.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Tiện ích định dạng tiền tệ và thời gian cho ứng dụng
 */
object FormatUtils {

    private val vnLocale = Locale("vi", "VN")
    private val currencyFormat = NumberFormat.getInstance(vnLocale)

    /**
     * Định dạng số tiền sang chuẩn VND (ví dụ: 12.000.000 ₫)
     * Có hỗ trợ chế độ riêng tư (Privacy Mode) che số tiền
     */
    fun formatVnd(amount: Double, isPrivacyMode: Boolean = false): String {
        if (isPrivacyMode) return "•••••• ₫"
        return "${currencyFormat.format(amount)} ₫"
    }

    /**
     * Định dạng số tiền ngắn gọn (ví dụ: 12 Tr hoặc 450 K)
     */
    fun formatVndShort(amount: Double, isPrivacyMode: Boolean = false): String {
        if (isPrivacyMode) return "••••"
        return when {
            amount >= 1_000_000_000 -> String.format(vnLocale, "%.1f Tỷ", amount / 1_000_000_000.0)
            amount >= 1_000_000 -> String.format(vnLocale, "%.1f Tr", amount / 1_000_000.0)
            amount >= 1_000 -> String.format(vnLocale, "%.0f K", amount / 1_000.0)
            else -> "${amount.toLong()} ₫"
        }
    }

    /**
     * Định dạng thời gian (ví dụ: 14:30 - Hôm nay, hoặc 28/09/2026)
     */
    fun formatDateTime(timestamp: Long): String {
        val sdf = SimpleDateFormat("HH:mm - dd/MM", vnLocale)
        return sdf.format(Date(timestamp))
    }

    /**
     * Định dạng số lượng (ví dụ: 5.5 chỉ hoặc 1.250 CCQ)
     */
    fun formatUnits(units: Double): String {
        return if (units % 1.0 == 0.0) {
            units.toLong().toString()
        } else {
            String.format(vnLocale, "%.2f", units)
        }
    }
}
