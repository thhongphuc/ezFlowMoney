package com.antibudget.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.util.FormatUtils

@Composable
fun PaydayDialog(
    currentCycle: BudgetCycleEntity?,
    onDismiss: () -> Unit,
    onConfirm: (income: Double, savings: Double, fixedBills: Double, variablePool: Double) -> Unit
) {
    var incomeText by remember {
        mutableStateOf(currentCycle?.totalIncome?.toLong()?.toString() ?: "42000000")
    }
    var fixedBillsText by remember {
        mutableStateOf(currentCycle?.fixedBillsTotal?.toLong()?.toString() ?: "17000000")
    }

    val income = incomeText.toDoubleOrNull() ?: 0.0
    val fixedBills = fixedBillsText.toDoubleOrNull() ?: 0.0

    // Tự động phân bổ theo Anti-Budget: 30% Tích sản trước
    val savings = income * 0.30
    val variablePool = (income - savings - fixedBills).coerceAtLeast(0.0)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Phân bổ lương (Payday Allocation)",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Quy tắc Anti-Budget: Trả cho mình trước 30%",
                    style = MaterialTheme.typography.bodySmall,
                    color = Emerald500
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                OutlinedTextField(
                    value = incomeText,
                    onValueChange = { incomeText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tổng thu nhập nhận được (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = fixedBillsText,
                    onValueChange = { fixedBillsText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Tổng hóa đơn cố định (Fixed Bills)") },
                    placeholder = { Text("Nhà 8tr + Học 2 con 7.5tr + Điện nước 1.5tr") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Bảng kết quả phân bổ tự động
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "KẾT QUẢ PHÂN BỔ TỰ ĐỘNG",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    // 1. Tích sản 30%
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "1. Tích sản (3 Lọ - 30%)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gold400
                        )
                        Text(
                            text = FormatUtils.formatVnd(savings),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = Gold400
                        )
                    }

                    // 2. Chi phí cố định
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "2. Chi phí cố định (Bills)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = FormatUtils.formatVnd(fixedBills),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // 3. Quỹ tự do cả tháng
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "3. Quỹ tự do cả tháng",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Emerald500
                        )
                        Text(
                            text = FormatUtils.formatVnd(variablePool),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Emerald500
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (income > 0) {
                        onConfirm(income, savings, fixedBills, variablePool)
                        onDismiss()
                    }
                },
                enabled = income > 0,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Emerald500,
                    contentColor = Slate950
                )
            ) {
                Text("Kích hoạt chu kỳ", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Slate700)
            }
        }
    )
}
