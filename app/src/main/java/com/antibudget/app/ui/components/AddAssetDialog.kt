package com.antibudget.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antibudget.app.data.local.entity.WealthBucket
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.util.FormatUtils

@Composable
fun AddAssetDialog(
    initialBucket: WealthBucket = WealthBucket.BANK_SAVINGS,
    onDismiss: () -> Unit,
    onConfirm: (bucket: WealthBucket, name: String, units: Double, unitPrice: Double) -> Unit
) {
    var selectedBucket by remember { mutableStateOf(initialBucket) }
    var nameText by remember {
        mutableStateOf(
            when (initialBucket) {
                WealthBucket.BANK_SAVINGS -> "Sổ tiết kiệm ngân hàng"
                WealthBucket.GOLD_9999 -> "Vàng nhẫn trơn 9999"
                WealthBucket.FUND_NAV -> "Chứng chỉ quỹ mở (DCDS/VEOF)"
            }
        )
    }
    var unitsText by remember {
        mutableStateOf(
            when (initialBucket) {
                WealthBucket.BANK_SAVINGS -> "1"
                WealthBucket.GOLD_9999 -> "1"
                WealthBucket.FUND_NAV -> "500"
            }
        )
    }
    var unitPriceText by remember {
        mutableStateOf(
            when (initialBucket) {
                WealthBucket.BANK_SAVINGS -> "50000000"
                WealthBucket.GOLD_9999 -> "8200000"
                WealthBucket.FUND_NAV -> "28500"
            }
        )
    }

    val units = unitsText.toDoubleOrNull() ?: 0.0
    val unitPrice = unitPriceText.toDoubleOrNull() ?: 0.0
    val totalEstimated = units * unitPrice

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        shape = RoundedCornerShape(20.dp),
        title = {
            Text(
                text = "Thêm tài sản tích lũy",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Chọn 1 trong 3 Lọ tài sản",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    WealthBucket.entries.forEach { bucket ->
                        val isSelected = selectedBucket == bucket
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedBucket = bucket
                                nameText = when (bucket) {
                                    WealthBucket.BANK_SAVINGS -> "Sổ tiết kiệm ngân hàng"
                                    WealthBucket.GOLD_9999 -> "Vàng nhẫn 9999"
                                    WealthBucket.FUND_NAV -> "Chứng chỉ quỹ"
                                }
                            },
                            label = { Text(bucket.displayName, style = MaterialTheme.typography.labelSmall) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold400,
                                selectedLabelColor = Slate950
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = nameText,
                    onValueChange = { nameText = it },
                    label = { Text("Tên tài sản / Mã quỹ / Ngân hàng") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = unitsText,
                        onValueChange = { unitsText = it },
                        label = { Text("Số lượng (${selectedBucket.unitLabel})") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    OutlinedTextField(
                        value = unitPriceText,
                        onValueChange = { unitPriceText = it.filter { ch -> ch.isDigit() } },
                        label = { Text("Đơn giá (VNĐ)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1.4f)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Thành tiền:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatVnd(totalEstimated),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Gold400
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (nameText.isNotBlank() && units > 0 && unitPrice > 0) {
                        onConfirm(selectedBucket, nameText, units, unitPrice)
                        onDismiss()
                    }
                },
                enabled = nameText.isNotBlank() && units > 0 && unitPrice > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold400,
                    contentColor = Slate950
                )
            ) {
                Text("Lưu tài sản", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Slate700)
            }
        }
    )
}
