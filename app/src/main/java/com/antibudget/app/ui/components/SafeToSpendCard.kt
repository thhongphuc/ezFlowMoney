package com.antibudget.app.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antibudget.app.domain.model.BurnRateReport
import com.antibudget.app.domain.model.BurnRateStatus
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.StatusDanger
import com.antibudget.app.ui.theme.StatusSafe
import com.antibudget.app.ui.theme.StatusWarning
import com.antibudget.app.ui.util.FormatUtils

@Composable
fun SafeToSpendCard(
    report: BurnRateReport,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    val statusColor by animateColorAsState(
        targetValue = when (report.status) {
            BurnRateStatus.SAFE -> StatusSafe
            BurnRateStatus.WARNING -> StatusWarning
            BurnRateStatus.DANGER -> StatusDanger
        },
        animationSpec = tween(400),
        label = "statusColor"
    )

    val progressAnimated by animateFloatAsState(
        targetValue = (report.burnRatePercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "burnRateProgress"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(statusColor.copy(alpha = 0.5f), Slate700)
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = Slate850
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Tiêu đề + Badge Trạng Thái
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "DAILY SAFE-TO-SPEND",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Hạn mức an toàn hôm nay",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                    )
                }

                // Status Badge
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(statusColor.copy(alpha = 0.15f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val icon = when (report.status) {
                        BurnRateStatus.SAFE -> Icons.Default.CheckCircle
                        BurnRateStatus.WARNING -> Icons.Default.Warning
                        BurnRateStatus.DANGER -> Icons.Default.Error
                    }
                    Icon(
                        imageVector = icon,
                        contentDescription = report.status.label,
                        tint = statusColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = report.status.label,
                        style = MaterialTheme.typography.labelSmall,
                        color = statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Hero Main Number
            Row(
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = FormatUtils.formatVnd(report.dailySafeToSpend, isPrivacyMode),
                    style = MaterialTheme.typography.displayMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = statusColor
                )
                Text(
                    text = " / ngày",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(bottom = 6.dp, start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Linear Progress Bar hiển thị tốc độ đốt quỹ tự do
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Đã tiêu: ${FormatUtils.formatVnd(report.spentVariablePool, isPrivacyMode)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${report.burnRatePercentage.toInt()}% quỹ",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = statusColor
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(10.dp)
                        .clip(CircleShape),
                    color = statusColor,
                    trackColor = Slate700
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Metrics Box
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(14.dp))
                    .background(Slate800)
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Quỹ tự do còn lại",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatVnd(report.remainingVariablePool, isPrivacyMode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Box(
                    modifier = Modifier
                        .width(1.dp)
                        .height(30.dp)
                        .background(Slate700)
                )

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Số ngày còn lại",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${report.daysRemaining} ngày",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Lời khuyên Burn-Rate
            Text(
                text = report.statusMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 18.sp
            )
        }
    }
}

@androidx.compose.ui.tooling.preview.Preview(showBackground = true)
@Composable
fun SafeToSpendCardPreview() {
    val sampleReport = BurnRateReport(
        dailySafeToSpend = 385000.0,
        remainingVariablePool = 8470000.0,
        variablePoolTotal = 13000000.0,
        spentVariablePool = 4530000.0,
        daysRemaining = 22,
        daysInMonth = 30,
        initialDailyBudget = 433333.0,
        burnRatePercentage = 34.8f,
        status = BurnRateStatus.SAFE,
        statusMessage = "Hạn mức tối đa bạn có thể thoải mái chi tiêu hôm nay mà không lo thâm hụt."
    )
    Box(modifier = Modifier.padding(16.dp)) {
        SafeToSpendCard(report = sampleReport)
    }
}

