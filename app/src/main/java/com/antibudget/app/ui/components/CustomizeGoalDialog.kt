package com.antibudget.app.ui.components

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antibudget.app.data.local.entity.WealthGoalEntity
import com.antibudget.app.domain.WealthProjectionEngine
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.util.FormatUtils
import java.time.LocalDate

@Composable
fun CustomizeGoalDialog(
    currentGoal: WealthGoalEntity,
    currentNetWorth: Double,
    monthlyContribution: Double,
    onDismiss: () -> Unit,
    onSave: (birthYear: Int, targetAge: Int, targetAmount: Double, returnRate: Double) -> Unit
) {
    var birthYearText by remember { mutableStateOf(currentGoal.birthYear.toString()) }
    var targetAge by remember { mutableIntStateOf(currentGoal.targetAge) }
    var targetAmountText by remember { mutableStateOf(currentGoal.targetAmount.toLong().toString()) }
    var returnRate by remember { mutableDoubleStateOf(currentGoal.expectedAnnualReturn) }

    val currentYear = LocalDate.now().year
    val birthYear = birthYearText.toIntOrNull() ?: 1994
    val currentAge = (currentYear - birthYear).coerceIn(18, 80)
    val effectiveTargetAge = targetAge.coerceAtLeast(currentAge + 1)
    val targetAmount = targetAmountText.toDoubleOrNull() ?: 600_000_000.0

    // Tính toán dự phóng tức thời khi người dùng điều chỉnh
    val previewReport = remember(birthYear, effectiveTargetAge, targetAmount, returnRate, currentNetWorth, monthlyContribution) {
        val tempGoal = WealthGoalEntity(
            birthYear = birthYear,
            targetAge = effectiveTargetAge,
            targetAmount = targetAmount,
            expectedAnnualReturn = returnRate
        )
        WealthProjectionEngine.calculateReport(
            goal = tempGoal,
            currentNetWorth = currentNetWorth,
            monthlyContribution = monthlyContribution,
            currentYear = currentYear
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Slate850,
        shape = RoundedCornerShape(24.dp),
        title = {
            Column {
                Text(
                    text = "Hoạch định mục tiêu tài chính",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Tùy chỉnh mục tiêu & tính toán sức mạnh lãi kép",
                    style = MaterialTheme.typography.bodySmall,
                    color = Gold400
                )
            }
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // 1. Năm sinh & Tuổi hiện tại
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = birthYearText,
                        onValueChange = { birthYearText = it.filter { ch -> ch.isDigit() }.take(4) },
                        label = { Text("Năm sinh") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Slate800)
                            .padding(14.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Hiện tại: $currentAge tuổi",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Tuổi mục tiêu (Slider)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tuổi hoàn thành mục tiêu",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "$effectiveTargetAge tuổi (${effectiveTargetAge - currentAge} năm nữa)",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Gold400
                        )
                    }

                    Slider(
                        value = effectiveTargetAge.toFloat(),
                        onValueChange = { targetAge = it.toInt() },
                        valueRange = (currentAge + 1).toFloat()..65f,
                        steps = (65 - (currentAge + 1)).coerceAtLeast(0),
                        colors = SliderDefaults.colors(
                            thumbColor = Gold400,
                            activeTrackColor = Gold400,
                            inactiveTrackColor = Slate700
                        )
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 3. Số tiền mục tiêu
                OutlinedTextField(
                    value = targetAmountText,
                    onValueChange = { targetAmountText = it.filter { ch -> ch.isDigit() } },
                    label = { Text("Số tiền mục tiêu (VNĐ)") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // Quick Target Presets
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 6.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    listOf(500_000_000L to "500 Tr", 600_000_000L to "600 Tr", 1_000_000_000L to "1 Tỷ", 2_000_000_000L to "2 Tỷ").forEach { (amount, label) ->
                        val isSelected = targetAmountText == amount.toString()
                        FilterChip(
                            selected = isSelected,
                            onClick = { targetAmountText = amount.toString() },
                            label = { Text(label, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = Gold400,
                                selectedLabelColor = Slate950
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Tỷ suất sinh lời kỳ vọng (%/năm)
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Lợi nhuận bình quân 3 Lọ",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${String.format("%.1f", returnRate)}%/năm",
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
                    }

                    Slider(
                        value = returnRate.toFloat(),
                        onValueChange = { returnRate = it.toDouble() },
                        valueRange = 4.0f..15.0f,
                        steps = 21,
                        colors = SliderDefaults.colors(
                            thumbColor = Emerald500,
                            activeTrackColor = Emerald500,
                            inactiveTrackColor = Slate700
                        )
                    )
                    Text(
                        text = "Tiền gửi ~5% • Vàng ~8-9% • Quỹ CCQ ~11-12%",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Kết quả phân tích lãi kép tức thời (Live Preview Box)
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(Slate800)
                        .padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "DỰ PHÓNG LÃI KÉP Ở TUỔI $effectiveTargetAge",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold400,
                        letterSpacing = 1.sp
                    )

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Dự kiến đạt được:",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = FormatUtils.formatVnd(previewReport.projectedFutureValue),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = Gold400
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Tích sản hiện tại/tháng:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatVnd(monthlyContribution),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = Emerald500
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Mức tích sản tối thiểu cần:",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = FormatUtils.formatVnd(previewReport.requiredMonthlySavings),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = previewReport.adviceMessage,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(birthYear, effectiveTargetAge, targetAmount, returnRate)
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Gold400,
                    contentColor = Slate950
                )
            ) {
                Text("Lưu cấu hình", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Hủy", color = Slate700)
            }
        }
    )
}
