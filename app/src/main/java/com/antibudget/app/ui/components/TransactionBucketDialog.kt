package com.antibudget.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.util.FormatUtils

@Composable
fun TransactionBucketDialog(
    transaction: TransactionEntity,
    onDismiss: () -> Unit,
    onConfirm: (newBucket: BucketType) -> Unit
) {
    var selectedBucket by remember { mutableStateOf(transaction.bucketType) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Phân loại giao dịch",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${transaction.note} • ${FormatUtils.formatVnd(transaction.amount)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald500,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "Chọn quỹ ngân sách tương ứng:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // 1. Quỹ tự do (VARIABLE)
                BucketOptionRow(
                    icon = Icons.Default.Paid,
                    title = "Quỹ tự do (VARIABLE)",
                    subtitle = "Trừ trực tiếp vào hạn mức Daily Safe-to-Spend",
                    color = Emerald500,
                    isSelected = selectedBucket == BucketType.VARIABLE,
                    onClick = { selectedBucket = BucketType.VARIABLE }
                )

                // 2. Chi phí cố định (FIXED)
                BucketOptionRow(
                    icon = Icons.Default.CreditCard,
                    title = "Chi phí cố định (FIXED)",
                    subtitle = "Thuộc quỹ 17 triệu (Tiền nhà, học phí, điện nước)",
                    color = MaterialTheme.colorScheme.onSurface,
                    isSelected = selectedBucket == BucketType.FIXED,
                    onClick = { selectedBucket = BucketType.FIXED }
                )

                // 3. Tích sản (SAVINGS)
                BucketOptionRow(
                    icon = Icons.Default.Savings,
                    title = "Tích sản (SAVINGS)",
                    subtitle = "Thuộc quỹ 12 triệu (Sổ tiết kiệm, Vàng 9999, Quỹ NAV)",
                    color = Gold400,
                    isSelected = selectedBucket == BucketType.SAVINGS,
                    onClick = { selectedBucket = BucketType.SAVINGS }
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onConfirm(selectedBucket)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate950
                )
            ) {
                Text("Cập nhật", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Đóng", color = Slate700)
            }
        }
    )
}

@Composable
private fun BucketOptionRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    color: androidx.compose.ui.graphics.Color,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) color.copy(alpha = 0.15f) else Slate800)
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) color else Slate700,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSelected) color else MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
