package com.antibudget.app.widget

import android.content.Context
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import com.antibudget.app.MainActivity
import com.antibudget.app.data.local.AppDatabase
import com.antibudget.app.domain.BurnRateCalculator
import com.antibudget.app.domain.model.BurnRateStatus
import com.antibudget.app.ui.util.FormatUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Android Glance AppWidget hiển thị chỉ số Daily Safe-to-Spend ngoài màn hình chính
 */
class SafeToSpendWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        // Lấy dữ liệu mới nhất từ Room DB
        val report = withContext(Dispatchers.IO) {
            val db = AppDatabase.getInstance(context)
            val cycle = db.budgetCycleDao().getActiveCycleSync()
            val spent = if (cycle != null) {
                db.transactionDao().getVariableSpentForCycleSync(cycle.id)
            } else {
                0.0
            }
            BurnRateCalculator.calculate(
                variablePool = cycle?.variablePool ?: 13_000_000.0,
                spentVariable = spent
            )
        }

        val statusColor = when (report.status) {
            BurnRateStatus.SAFE -> Color(0xFF10B981) // Emerald
            BurnRateStatus.WARNING -> Color(0xFFF59E0B) // Amber
            BurnRateStatus.DANGER -> Color(0xFFEF4444) // Rose
        }

        provideContent {
            Box(
                modifier = GlanceModifier
                    .fillMaxSize()
                    .background(Color(0xFF0F172A))
                    .cornerRadius(20.dp)
                    .clickable(actionStartActivity<MainActivity>())
                    .padding(14.dp)
            ) {
                Column(
                    modifier = GlanceModifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalAlignment = Alignment.Start
                ) {
                    // Header Bar
                    Row(
                        modifier = GlanceModifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "HẠN MỨC HÔM NAY",
                            style = TextStyle(
                                color = ColorProvider(Color(0xFF94A3B8)),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        )
                        Spacer(modifier = GlanceModifier.defaultWeight())
                        Box(
                            modifier = GlanceModifier
                                .background(statusColor)
                                .cornerRadius(8.dp)
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = report.status.label,
                                style = TextStyle(
                                    color = ColorProvider(Color(0xFF0F172A)),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            )
                        }
                    }

                    Spacer(modifier = GlanceModifier.height(8.dp))

                    // Hero Number
                    Text(
                        text = FormatUtils.formatVnd(report.dailySafeToSpend),
                        style = TextStyle(
                            color = ColorProvider(statusColor),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Bold
                        )
                    )

                    Spacer(modifier = GlanceModifier.height(4.dp))

                    // Subtext
                    Text(
                        text = "Còn ${FormatUtils.formatVndShort(report.remainingVariablePool)} • ${report.daysRemaining} ngày",
                        style = TextStyle(
                            color = ColorProvider(Color(0xFFCBD5E1)),
                            fontSize = 11.sp
                        )
                    )
                }
            }
        }
    }
}
