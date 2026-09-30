# Anti-Budget & Burn-Rate Tracker (Android App)

Ứng dụng quản lý tài chính cá nhân dành cho gia đình theo triết lý **The Anti-Budget** (không nhập liệu thủ công vụn vặt), kết hợp động cơ **Burn-Rate Safe-to-Spend** và chiến lược **Tích sản 3 Lọ** (hướng đến mục tiêu 600 triệu đồng tuổi 40).

---

## 1. Triết Lý Cốt Lõi & Kiến Trúc Nghiệp Vụ

### A. Payday Allocation (Phân bổ lương ngày đầu tháng)
* **Tổng thu nhập mặc định:** `42.000.000 VNĐ`
* **Trích ngay 30% vào Tích sản (3 Lọ):** `12.000.000 VNĐ`
  1. *Lọ 1 - Sổ tiết kiệm ngân hàng:* An toàn thanh khoản cao
  2. *Lọ 2 - Vàng nhẫn 9999 (theo chỉ):* Bảo vệ sức mua chống lạm phát
  3. *Lọ 3 - Chứng chỉ quỹ mở (theo NAV):* Tăng trưởng tài sản dài hạn
* **Chi phí cố định (Fixed Bills):** `17.000.000 VNĐ` (Tiền nhà 8tr + Học phí 2 bé 7.5tr + Điện nước 1.5tr)
* **Quỹ chi tiêu tự do hàng ngày (Variable Pool):** `13.000.000 VNĐ`

### B. Burn-Rate Engine (Chỉ số Daily Safe-to-Spend)
* **Công thức tính toán:**
  $$\text{DailySafeToSpend} = \frac{\text{VariablePool} - \text{Đã tiêu quỹ tự do}}{\text{Số ngày còn lại trong tháng}}$$
* **Hệ thống cảnh báo 3 cấp độ:**
  * 🟢 **XANH (Safe):** Tiêu chuẩn an toàn, nhịp độ chi tiêu chuẩn chỉnh.
  * 🟡 **VÀNG (Warning):** Sát hạn mức, tốc độ chi tiêu đang vượt nhịp độ thời gian.
  * 🔴 **ĐỎ (Danger):** Cảnh báo bội chi, quỹ tự do đã cạn kiệt.

### C. Tự Động Hóa Thông Báo Ngân Hàng (Background Automation)
* Sử dụng `NotificationListenerService` đọc biến động số dư từ các app ngân hàng tại Việt Nam (Vietcombank Digibank, Techcombank, MBBank, ACB ONE, BIDV SmartBanking, VPBank...).
* Bộ `BankRegexParser` bóc tách chính xác các số tiền trừ (-) và tự động lưu vào SQLite Room Database với `bucketType = VARIABLE`.
* Tự động loại trừ các giao dịch nhận tiền/cộng tiền (+) để không làm sai lệch số liệu chi tiêu.

### D. Android Glance AppWidget
* Widget 2x2 Compose-based trên màn hình chính: Cập nhật liên tục số tiền **Daily Safe-to-Spend**, tình trạng Xanh/Vàng/Đỏ và số ngày còn lại trong tháng mà không cần mở app.

---

## 2. Tech Stack & Cấu Trúc Mã Nguồn

* **Ngôn ngữ:** Kotlin 2.0.21
* **Giao diện:** Jetpack Compose + Material 3 + Single-Activity Architecture
* **Database:** Room DB (SQLite) + Reactive Kotlin Flow
* **Kiến trúc:** Clean Architecture + MVVM + Repository Pattern
* **Widget:** AndroidX Glance AppWidget 1.1.1
* **Build System:** Gradle Kotlin DSL (`build.gradle.kts`) với Version Catalog (`libs.versions.toml`)

```
app/src/main/java/com/antibudget/app/
├── MainActivity.kt                      # Single-Activity, Navigation Bar & Permission Handling
├── data/
│   ├── local/
│   │   ├── entity/
│   │   │   ├── BucketType.kt            # FIXED, VARIABLE, SAVINGS
│   │   │   ├── WealthBucket.kt          # BANK_SAVINGS, GOLD_9999, FUND_NAV
│   │   │   ├── BudgetCycleEntity.kt     # Chu kỳ tháng (42tr, 30% tích sản, fixed bills)
│   │   │   ├── TransactionEntity.kt     # Giao dịch tự động & thủ công
│   │   │   └── WealthAssetEntity.kt     # Danh mục tài sản 3 Lọ
│   │   ├── dao/
│   │   │   ├── BudgetCycleDao.kt        # Reactive Room Flow queries
│   │   │   ├── TransactionDao.kt        # Tự động tính tổng chi tiêu quỹ tự do
│   │   │   └── WealthAssetDao.kt        # Tính tổng tài sản ròng rực rỡ
│   │   ├── AppConverters.kt             # Room TypeConverters
│   │   └── AppDatabase.kt               # Singleton DB & Callback khởi tạo dữ liệu mẫu
│   └── repository/
│       ├── FinanceRepository.kt         # Clean Interface
│       └── FinanceRepositoryImpl.kt     # Room Implementation
├── domain/
│   ├── model/
│   │   ├── BurnRateStatus.kt            # SAFE, WARNING, DANGER
│   │   ├── BurnRateReport.kt            # Báo cáo Burn-Rate hàng ngày
│   │   └── AssetSummary.kt              # Tổng hợp tài sản ròng & tiến trình 600 triệu
│   └── BurnRateCalculator.kt            # Engine tính toán số học độc lập
├── service/
│   ├── BankRegexParser.kt               # Regex bóc tách tin nhắn SMS/Notif ngân hàng VN
│   └── BankNotificationListenerService.kt # Lắng nghe background, trigger insert & update widget
├── ui/
│   ├── theme/
│   │   ├── Color.kt                     # Emerald, Slate Dark, Accent Gold, Status colors
│   │   ├── Type.kt                      # Material 3 Typography
│   │   └── Theme.kt                     # AntiBudgetTheme (Dark Mode ưu tiên)
│   ├── components/
│   │   ├── SafeToSpendCard.kt           # Hero Card lớn với LinearProgressIndicator đổi màu
│   │   ├── QuickAddDialog.kt            # Dialog ghi nhận nhanh chi tiêu tiền mặt
│   │   ├── PaydayDialog.kt              # Dialog kích hoạt phân bổ chu kỳ lương mới
│   │   └── AddAssetDialog.kt            # Dialog thêm danh mục vào 3 Lọ tài sản
│   ├── dashboard/
│   │   ├── DashboardViewModel.kt        # StateFlow với SharingStarted.WhileSubscribed(5000)
│   │   └── DashboardScreen.kt           # Màn hình chính Safe-to-Spend & Giao dịch gần nhất
│   ├── wealth/
│   │   ├── WealthViewModel.kt           # Tính tiến độ mục tiêu 600 triệu & phân loại 3 Lọ
│   │   └── WealthScreen.kt              # Màn hình Tích sản 3 Lọ & Milestone Progress
│   └── util/
│       └── FormatUtils.kt               # Định dạng tiền tệ VND chuẩn Việt Nam
└── widget/
    ├── SafeToSpendWidget.kt             # Android Glance Compose AppWidget
    └── SafeToSpendWidgetReceiver.kt     # AppWidget Receiver
```

---

## 3. Hướng Dẫn Cài Đặt & Sử Dụng

1. Mở thư mục dự án `c:\Projects\ezFlowMoney` bằng **Android Studio** (Koala / Ladybug hoặc mới hơn).
2. Chờ Gradle đồng bộ hóa (Sync Project with Gradle Files).
3. Chạy ứng dụng trên máy thật hoặc máy ảo (Android 8.0 / API 26 trở lên).
4. Khi mở app lần đầu, chọn **"Bật"** trên thanh thông báo để cấp quyền **Notification Listener** cho ứng dụng nhằm tự động bóc tách tin nhắn trừ tiền từ Vietcombank, Techcombank, MB, ACB, BIDV...
5. Ra màn hình chính của điện thoại, thêm widget **"Safe-to-Spend Widget"** để theo dõi hạn mức chi tiêu mỗi ngày.
