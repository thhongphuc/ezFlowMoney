package com.antibudget.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import com.antibudget.app.data.local.AppDatabase
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.repository.FinanceRepositoryImpl
import com.antibudget.app.widget.SafeToSpendWidget
import androidx.glance.appwidget.updateAll
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Service lắng nghe biến động số dư từ các ứng dụng ngân hàng Việt Nam
 * Tự động bóc tách số tiền trừ (-) và ghi nhận vào Quỹ chi tiêu tự do (VARIABLE)
 */
class BankNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val tag = "BankNotifListener"

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val packageName = sbn.packageName
        val extras = sbn.notification.extras ?: return

        val title = extras.getString(Notification.EXTRA_TITLE).orEmpty()
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString().orEmpty()
        val bigText = extras.getCharSequence(Notification.EXTRA_BIG_TEXT)?.toString().orEmpty()

        val fullContent = if (bigText.isNotBlank()) bigText else text

        Log.d(tag, "Nhận thông báo từ: $packageName | Title: $title | Content: $fullContent")

        val parsed = BankRegexParser.parse(title = title, content = fullContent, packageName = packageName)

        if (parsed != null) {
            Log.i(tag, "Bóc tách thành công: ${parsed.bankName} - Số tiền: ${parsed.amount} đ - Ghi chú: ${parsed.note}")

            serviceScope.launch {
                try {
                    val db = AppDatabase.getInstance(applicationContext)
                    val repository = FinanceRepositoryImpl(db)

                    repository.addTransaction(
                        amount = parsed.amount,
                        note = parsed.note,
                        bankSource = parsed.bankName,
                        bucketType = parsed.bucketType
                    )

                    // Cập nhật lại Widget ngoài màn hình chính
                    try {
                        SafeToSpendWidget().updateAll(applicationContext)
                    } catch (e: Exception) {
                        Log.e(tag, "Lỗi cập nhật widget: ${e.message}")
                    }

                } catch (e: Exception) {
                    Log.e(tag, "Lỗi lưu giao dịch tự động: ${e.message}", e)
                }
            }
        }
    }
}
