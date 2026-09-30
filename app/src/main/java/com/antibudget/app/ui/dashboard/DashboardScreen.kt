package com.antibudget.app.ui.dashboard

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalance
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CreditCard
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Paid
import androidx.compose.material.icons.filled.Payments
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
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
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.ui.components.FixedBillsTrackerCard
import com.antibudget.app.ui.components.PaydayDialog
import com.antibudget.app.ui.components.QuickAddDialog
import com.antibudget.app.ui.components.SafeToSpendCard
import com.antibudget.app.ui.components.TransactionBucketDialog
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate900
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.theme.StatusDanger
import com.antibudget.app.ui.util.FormatUtils
import java.time.LocalDate

@Composable
fun DashboardScreen(
    viewModel: DashboardViewModel,
    onNavigateToWealth: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    var showQuickAddDialog by remember { mutableStateOf(false) }
    var showPaydayDialog by remember { mutableStateOf(false) }
    var selectedTxForBucket by remember { mutableStateOf<TransactionEntity?>(null) }

    Scaffold(
        containerColor = Slate900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showQuickAddDialog = true },
                containerColor = Emerald500,
                contentColor = Slate950,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Ghi nhanh chi tiêu",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Emerald500)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header Bar with Privacy Mode Toggle
                item {
                    HeaderSection(
                        month = uiState.activeCycle?.month ?: LocalDate.now().monthValue,
                        year = uiState.activeCycle?.year ?: LocalDate.now().year,
                        isPrivacyMode = uiState.isPrivacyMode,
                        onTogglePrivacy = { viewModel.togglePrivacyMode() },
                        onPaydayClick = { showPaydayDialog = true }
                    )
                }

                // Hero Card: Safe To Spend
                item {
                    SafeToSpendCard(
                        report = uiState.burnRateReport,
                        isPrivacyMode = uiState.isPrivacyMode
                    )
                }

                // Checklist Hóa đơn cố định tháng này (17 Tr)
                item {
                    FixedBillsTrackerCard(
                        totalFixedBudget = uiState.activeCycle?.fixedBillsTotal ?: 17_000_000.0,
                        fixedTransactions = uiState.fixedTransactions,
                        isPrivacyMode = uiState.isPrivacyMode,
                        onQuickMarkPaid = { title, amt -> viewModel.markFixedBillPaid(title, amt) }
                    )
                }

                // 30% Payday Allocation Breakdown Card
                item {
                    PaydaySummaryCard(
                        income = uiState.activeCycle?.totalIncome ?: 42_000_000.0,
                        savings = uiState.activeCycle?.savingsTarget ?: 12_000_000.0,
                        fixedBills = uiState.activeCycle?.fixedBillsTotal ?: 17_000_000.0,
                        variablePool = uiState.activeCycle?.variablePool ?: 13_000_000.0,
                        isPrivacyMode = uiState.isPrivacyMode,
                        onWealthClick = onNavigateToWealth
                    )
                }

                // Recent Transactions Header
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Giao dịch gần nhất",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Chạm để đổi loại quỹ",
                            style = MaterialTheme.typography.labelSmall,
                            color = Emerald500
                        )
                    }
                }

                // Transaction List
                if (uiState.recentTransactions.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate850)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(32.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Chưa có giao dịch nào được ghi nhận.\nChi tiêu ngân hàng sẽ tự động hiện tại đây.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                                )
                            }
                        }
                    }
                } else {
                    items(uiState.recentTransactions, key = { it.id }) { tx ->
                        TransactionItemCard(
                            transaction = tx,
                            isPrivacyMode = uiState.isPrivacyMode,
                            onClick = { selectedTxForBucket = tx },
                            onDelete = { viewModel.deleteTransaction(tx) }
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showQuickAddDialog) {
        QuickAddDialog(
            onDismiss = { showQuickAddDialog = false },
            onConfirm = { amount, note, source ->
                viewModel.addQuickExpense(amount, note, source)
            }
        )
    }

    if (showPaydayDialog) {
        PaydayDialog(
            currentCycle = uiState.activeCycle,
            onDismiss = { showPaydayDialog = false },
            onConfirm = { income, savings, fixedBills, variablePool ->
                viewModel.updatePaydayAllocation(income, savings, fixedBills, variablePool)
            }
        )
    }

    selectedTxForBucket?.let { tx ->
        TransactionBucketDialog(
            transaction = tx,
            onDismiss = { selectedTxForBucket = null },
            onConfirm = { newBucket ->
                viewModel.switchTransactionBucket(tx, newBucket)
            }
        )
    }
}

@Composable
private fun HeaderSection(
    month: Int,
    year: Int,
    isPrivacyMode: Boolean,
    onTogglePrivacy: () -> Unit,
    onPaydayClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Anti-Budget Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onTogglePrivacy,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isPrivacyMode) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                        contentDescription = "Chế độ riêng tư",
                        tint = if (isPrivacyMode) Gold400 else Slate700,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            Text(
                text = "Chu kỳ chi tiêu Tháng $month/$year",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        OutlinedButton(
            onClick = onPaydayClick,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = Emerald500)
        ) {
            Icon(
                imageVector = Icons.Default.Payments,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("Payday", fontWeight = FontWeight.Bold, fontSize = 13.sp)
        }
    }
}

@Composable
private fun PaydaySummaryCard(
    income: Double,
    savings: Double,
    fixedBills: Double,
    variablePool: Double,
    isPrivacyMode: Boolean = false,
    onWealthClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onWealthClick() },
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Quy tắc Anti-Budget (Lương ${FormatUtils.formatVndShort(income, isPrivacyMode)})",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Xem 3 Lọ →",
                    style = MaterialTheme.typography.labelSmall,
                    color = Gold400,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                AllocationPill(
                    icon = Icons.Default.Savings,
                    label = "Tích sản 30%",
                    amount = savings,
                    isPrivacyMode = isPrivacyMode,
                    color = Gold400
                )

                AllocationPill(
                    icon = Icons.Default.CreditCard,
                    label = "Cố định (Bills)",
                    amount = fixedBills,
                    isPrivacyMode = isPrivacyMode,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                AllocationPill(
                    icon = Icons.Default.Paid,
                    label = "Quỹ tự do",
                    amount = variablePool,
                    isPrivacyMode = isPrivacyMode,
                    color = Emerald500
                )
            }
        }
    }
}

@Composable
private fun AllocationPill(
    icon: ImageVector,
    label: String,
    amount: Double,
    isPrivacyMode: Boolean = false,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Slate800)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp
        )
        Text(
            text = FormatUtils.formatVndShort(amount, isPrivacyMode),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = color
        )
    }
}

@Composable
private fun TransactionItemCard(
    transaction: TransactionEntity,
    isPrivacyMode: Boolean = false,
    onClick: () -> Unit = {},
    onDelete: () -> Unit
) {
    val (bucketLabel, bucketColor) = when (transaction.bucketType) {
        BucketType.VARIABLE -> "Tự do" to Emerald500
        BucketType.FIXED -> "Cố định" to MaterialTheme.colorScheme.onSurfaceVariant
        BucketType.SAVINGS -> "Tích sản" to Gold400
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(Slate800),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = when {
                        transaction.bankSource.contains("VCB", true) -> Icons.Default.AccountBalance
                        transaction.bankSource.contains("Techcombank", true) -> Icons.Default.AccountBalance
                        transaction.bankSource.contains("MB", true) -> Icons.Default.AccountBalance
                        transaction.bankSource.contains("Tiền mặt", true) -> Icons.Default.Paid
                        else -> Icons.Default.CreditCard
                    },
                    contentDescription = null,
                    tint = bucketColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = transaction.note,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(bucketColor.copy(alpha = 0.15f))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = bucketLabel,
                            style = MaterialTheme.typography.labelSmall,
                            color = bucketColor,
                            fontWeight = FontWeight.Bold,
                            fontSize = 10.sp
                        )
                    }
                    Text(
                        text = " • ${transaction.bankSource}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = " • ${FormatUtils.formatDateTime(transaction.createdAt)}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "-${FormatUtils.formatVnd(transaction.amount, isPrivacyMode)}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = StatusDanger
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "Xóa",
                        tint = Slate700,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}
