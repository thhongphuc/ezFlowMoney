package com.antibudget.app.service

import com.antibudget.app.data.local.entity.BucketType
import java.util.regex.Pattern

/**
 * Kết quả bóc tách giao dịch từ tin nhắn ngân hàng
 */
data class ParsedTransaction(
    val amount: Double,
    val bankName: String,
    val note: String,
    val rawMessage: String,
    val bucketType: BucketType = BucketType.VARIABLE
)

/**
 * Bộ phân tích Regex cho thông báo/tin nhắn biến động số dư ngân hàng Việt Nam
 * Hỗ trợ VCB, Techcombank, MBBank, ACB, BIDV, VPBank, TPBank...
 * Tự động phân loại thông minh (Auto-Tagging) vào FIXED, SAVINGS hoặc VARIABLE
 */
object BankRegexParser {

    // Danh sách các ngân hàng phổ biến và package identifier
    private val BANK_IDENTIFIERS = mapOf(
        "com.vietcombank.digibank" to "VCB Digibank",
        "com.VCB" to "VCB",
        "vn.tecombank.bb.app" to "Techcombank",
        "com.techcombank.mobile" to "Techcombank",
        "com.mbmobile" to "MBBank",
        "com.mb.mbbank" to "MBBank",
        "mobile.acb.com.vn" to "ACB ONE",
        "vn.com.acb.mobilebanking" to "ACB",
        "com.vnpay.bidv" to "BIDV SmartBanking",
        "com.bidv.smartbanking" to "BIDV",
        "com.vnpay.vpbank" to "VPBank NEO",
        "com.tpb.mb.gprsandroid" to "TPBank"
    )

    // Các cụm từ chỉ giao dịch chi tiêu/trừ tiền
    private val EXPENSE_PATTERNS = listOf(
        Pattern.compile("""[-−]\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""GD:\s*[-−]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:giam|trừ|thanh toan|rut tien|chuyen di)\s*:?\s*[-−]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:so tien|số tiền)(?:\s*GD)?\s*:?\s*[-−]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:bien dong|biến động)\s*:?\s*[-−]?\s*([\d.,]+)\s*(?:VND|VNĐ|đ|d)?""", Pattern.CASE_INSENSITIVE)
    )

    // Các cụm từ chỉ giao dịch nhận tiền (Cộng tiền) cần loại bỏ
    private val INCOME_INDICATORS = listOf(
        "+", "tang", "tăng", "nhan tien", "nhận tiền", "hoan tien", "hoàn tiền", "chuyen den", "chuyển đến"
    )

    // Pattern tìm nội dung giao dịch
    private val NOTE_PATTERNS = listOf(
        Pattern.compile("""(?:ND|Nội dung|Noi dung|Lý do|Ly do|Memo|Ref|Tại|Tai)\s*[:.]\s*([^.\n]+)""", Pattern.CASE_INSENSITIVE),
        Pattern.compile("""(?:thanh toan|chuyen tien cho|chuyen khoan den)\s*([^.\n]+)""", Pattern.CASE_INSENSITIVE)
    )

    // Từ khóa phân loại Chi phí Cố định (Fixed Bills)
    private val FIXED_KEYWORDS = listOf(
        "tien nha", "tiền nhà", "thue nha", "thuê nhà", "dong tien nha",
        "hoc phi", "học phí", "mam non", "mầm non", "tieu hoc", "tiểu học", "truong", "trường",
        "dien luc", "điện lực", "evn", "tien dien", "tiền điện",
        "tien nuoc", "tiền nước", "cap nuoc", "cấp nước", "sawaco",
        "chung cu", "chung cư", "phi quan ly", "phí quản lý", "phi dich vu",
        "internet", "viettel telecom", "fpt telecom", "vnpt"
    )

    // Từ khóa phân loại Tích sản (Savings / Investment)
    private val SAVINGS_KEYWORDS = listOf(
        "tiet kiem", "tiết kiệm", "so tiet kiem", "sổ tiết kiệm", "gui tiet kiem",
        "dcds", "veof", "vesaf", "chung chi quy", "chứng chỉ quỹ",
        "mua vang", "mua vàng", "nhan 9999", "nhẫn 9999", "pnj", "sjc", "doji", "bao tin minh chau",
        "tich san", "tích sản", "dau tu", "đầu tư"
    )

    /**
     * Nhận diện thông minh loại ngân sách từ nội dung giao dịch
     */
    fun detectBucketType(text: String): BucketType {
        val lower = text.lowercase()
        return when {
            FIXED_KEYWORDS.any { lower.contains(it) } -> BucketType.FIXED
            SAVINGS_KEYWORDS.any { lower.contains(it) } -> BucketType.SAVINGS
            else -> BucketType.VARIABLE
        }
    }

    /**
     * Xác định tên ngân hàng dựa vào package hoặc nội dung
     */
    fun detectBank(packageName: String?, text: String): String {
        if (!packageName.isNullOrBlank()) {
            BANK_IDENTIFIERS[packageName]?.let { return it }
        }

        val upper = text.uppercase()
        return when {
            upper.contains("VIETCOMBANK") || upper.contains("VCB") -> "VCB Digibank"
            upper.contains("TECHCOMBANK") || upper.contains("TCB") -> "Techcombank"
            upper.contains("MBBANK") || upper.contains("MB BANK") || upper.contains("QUAN DOI") -> "MBBank"
            upper.contains("ACB") -> "ACB ONE"
            upper.contains("BIDV") -> "BIDV SmartBanking"
            upper.contains("VPBANK") -> "VPBank NEO"
            upper.contains("TPBANK") -> "TPBank"
            else -> "Ngân hàng"
        }
    }

    /**
     * Phân tích nội dung tin nhắn và trích xuất giao dịch
     */
    fun parse(title: String?, content: String?, packageName: String? = null): ParsedTransaction? {
        val fullText = "${title.orEmpty()} ${content.orEmpty()}".trim()
        if (fullText.isBlank()) return null

        // 1. Kiểm tra xem có phải giao dịch nhận tiền (cộng +) không
        val lowerText = fullText.lowercase()
        val isExplicitIncome = INCOME_INDICATORS.any { indicator ->
            lowerText.contains(indicator) && !lowerText.contains("-") && !lowerText.contains("−")
        }
        if (isExplicitIncome) {
            return null
        }

        // 2. Tìm số tiền bị trừ
        var extractedAmount: Double? = null

        for (pattern in EXPENSE_PATTERNS) {
            val matcher = pattern.matcher(fullText)
            if (matcher.find()) {
                val rawNumberStr = matcher.group(1) ?: continue
                val sanitized = sanitizeNumber(rawNumberStr)
                if (sanitized > 0) {
                    extractedAmount = sanitized
                    break
                }
            }
        }

        if (extractedAmount == null || extractedAmount <= 0) {
            return null
        }

        // 3. Trích xuất nội dung ghi chú (Note)
        var note = "Chi tiêu qua ngân hàng"
        for (notePattern in NOTE_PATTERNS) {
            val matcher = notePattern.matcher(fullText)
            if (matcher.find()) {
                val captured = matcher.group(1)?.trim()
                if (!captured.isNullOrBlank() && captured.length > 2) {
                    note = captured.take(60)
                    break
                }
            }
        }

        if (note == "Chi tiêu qua ngân hàng" && !title.isNullOrBlank()) {
            note = title.take(50)
        }

        val bankName = detectBank(packageName, fullText)
        val bucketType = detectBucketType(fullText)

        return ParsedTransaction(
            amount = extractedAmount,
            bankName = bankName,
            note = note,
            rawMessage = fullText,
            bucketType = bucketType
        )
    }

    private fun sanitizeNumber(raw: String): Double {
        return try {
            val clean = raw.replace(",", "").replace(".", "").trim()
            clean.toDouble()
        } catch (e: Exception) {
            0.0
        }
    }
}
