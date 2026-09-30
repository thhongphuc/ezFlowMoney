package com.antibudget.app.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Power
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.School
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.util.FormatUtils

data class FixedBillItem(
    val id: String,
    val title: String,
    val budgetAmount: Double,
    val icon: ImageVector,
    val isPaid: Boolean,
    val paidAmount: Double,
    val paidNote: String
)

@Composable
fun FixedBillsTrackerCard(
    totalFixedBudget: Double = 17_000_000.0,
    fixedTransactions: List<TransactionEntity>,
    isPrivacyMode: Boolean = false,
    onQuickMarkPaid: (title: String, amount: Double) -> Unit = { _, _ -> }
) {
    var isExpanded by remember { mutableStateOf(false) }

    // Kiểm tra từng khoản cố định dựa trên các giao dịch FIXED trong tháng
    val rentTx = fixedTransactions.firstOrNull {
        it.note.contains("nhà", true) || it.note.contains("thue", true) || it.amount in 7_800_000.0..8_500_000.0
    }
    val schoolTx = fixedTransactions.firstOrNull {
        it.note.contains("học", true) || it.note.contains("truong", true) || it.note.contains("bé", true) || (it != rentTx && it.amount in 7_000_000.0..7_600_000.0)
    }
    val utilityTx = fixedTransactions.firstOrNull {
        it != rentTx && it != schoolTx && (it.note.contains("điện", true) || it.note.contains("nước", true) || it.note.contains("evn", true) || it.amount <= 2_500_000.0)
    }

    val bills = listOf(
        FixedBillItem(
            id = "rent",
            title = "Tiền thuê nhà",
            budgetAmount = 8_000_000.0,
            icon = Icons.Default.Home,
            isPaid = rentTx != null,
            paidAmount = rentTx?.amount ?: 8_000_000.0,
            paidNote = rentTx?.note ?: "Chưa thanh toán"
        ),
        FixedBillItem(
            id = "school",
            title = "Học phí 2 bé",
            budgetAmount = 7_500_000.0,
            icon = Icons.Default.School,
            isPaid = schoolTx != null,
            paidAmount = schoolTx?.amount ?: 7_500_000.0,
            paidNote = schoolTx?.note ?: "Chưa thanh toán"
        ),
        FixedBillItem(
            id = "utility",
            title = "Điện, nước & Net",
            budgetAmount = 1_500_000.0,
            icon = Icons.Default.Power,
            isPaid = utilityTx != null,
            paidAmount = utilityTx?.amount ?: 1_500_000.0,
            paidNote = utilityTx?.note ?: "Chưa thanh toán"
        )
    )

    val totalPaid = (if (rentTx != null) rentTx.amount else 0.0) +
            (if (schoolTx != null) schoolTx.amount else 0.0) +
            (if (utilityTx != null) utilityTx.amount else 0.0)

    val paidCount = bills.count { it.isPaid }
    val progressAnimated by animateFloatAsState(
        targetValue = ((totalPaid / totalFixedBudget).toFloat()).coerceIn(0f, 1f),
        animationSpec = tween(500),
        label = "billsProgress"
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Header
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "HÓA ĐƠN CỐ ĐỊNH THÁNG NÀY",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Đã trả $paidCount/3 khoản (${FormatUtils.formatVndShort(totalPaid, isPrivacyMode)} / ${FormatUtils.formatVndShort(totalFixedBudget, isPrivacyMode)})",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (paidCount == 3) Emerald500 else MaterialTheme.colorScheme.onSurface,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Thu gọn" else "Chi tiết",
                        style = MaterialTheme.typography.labelSmall,
                        color = Emerald500,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Progress bar
            LinearProgressIndicator(
                progress = { progressAnimated },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(CircleShape),
                color = Emerald500,
                trackColor = Slate700
            )

            // Expanded Checklist
            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    bills.forEach { bill ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Slate800)
                                .clickable {
                                    if (!bill.isPaid) {
                                        onQuickMarkPaid(bill.title, bill.budgetAmount)
                                    }
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = if (bill.isPaid) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (bill.isPaid) Emerald500 else Slate700,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = bill.title,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = if (bill.isPaid) "Đã thanh toán: ${bill.paidNote}" else "Chạm để đánh dấu đã thanh toán",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (bill.isPaid) Emerald500 else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Text(
                                text = FormatUtils.formatVnd(bill.budgetAmount, isPrivacyMode),
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (bill.isPaid) Emerald500 else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
