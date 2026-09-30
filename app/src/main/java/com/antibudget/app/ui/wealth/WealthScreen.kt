package com.antibudget.app.ui.wealth

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import com.antibudget.app.domain.model.FeasibilityStatus
import com.antibudget.app.domain.model.WealthGoalReport
import com.antibudget.app.domain.model.YearlyMilestone
import com.antibudget.app.ui.components.AddAssetDialog
import com.antibudget.app.ui.components.CustomizeGoalDialog
import com.antibudget.app.ui.theme.Emerald500
import com.antibudget.app.ui.theme.Gold400
import com.antibudget.app.ui.theme.Slate700
import com.antibudget.app.ui.theme.Slate800
import com.antibudget.app.ui.theme.Slate850
import com.antibudget.app.ui.theme.Slate900
import com.antibudget.app.ui.theme.Slate950
import com.antibudget.app.ui.theme.StatusDanger
import com.antibudget.app.ui.theme.StatusSafe
import com.antibudget.app.ui.theme.StatusWarning
import com.antibudget.app.ui.util.FormatUtils

@Composable
fun WealthScreen(
    viewModel: WealthViewModel,
    isPrivacyMode: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var showAddDialog by remember { mutableStateOf(false) }
    var showCustomizeGoalDialog by remember { mutableStateOf(false) }

    val tabBuckets = listOf(
        WealthBucket.BANK_SAVINGS,
        WealthBucket.GOLD_9999,
        WealthBucket.FUND_NAV
    )

    Scaffold(
        containerColor = Slate900,
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddDialog = true },
                containerColor = Gold400,
                contentColor = Slate950,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Thêm tài sản",
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
                CircularProgressIndicator(color = Gold400)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Header
                item {
                    Column {
                        Text(
                            text = "Tích Sản 3 Lọ",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Mục tiêu: ${FormatUtils.formatVndShort(uiState.goalReport.targetAmount)} tuổi ${uiState.goalReport.targetAge} (${uiState.goalReport.yearsRemaining} năm nữa)",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Gold400
                        )
                    }
                }

                // Dynamic Milestone Hero Card with WealthProjectionEngine
                item {
                    MilestoneHeroCard(
                        goalReport = uiState.goalReport,
                        isPrivacyMode = isPrivacyMode,
                        onConfigureClick = { showCustomizeGoalDialog = true }
                    )
                }

                // Yearly Milestone Roadmap Timeline Card
                item {
                    YearlyRoadmapCard(
                        milestones = uiState.goalReport.yearlyMilestones,
                        isPrivacyMode = isPrivacyMode
                    )
                }

                // 3 Lọ Summary Cards Grid
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        BucketSummaryMiniCard(
                            title = "Tiền gửi",
                            total = uiState.summary.bankSavingsTotal,
                            icon = Icons.Default.AccountBalance,
                            color = Emerald500,
                            isPrivacyMode = isPrivacyMode,
                            modifier = Modifier.weight(1f)
                        )
                        BucketSummaryMiniCard(
                            title = "Vàng 9999",
                            total = uiState.summary.gold9999Total,
                            icon = Icons.Default.Star,
                            color = Gold400,
                            isPrivacyMode = isPrivacyMode,
                            modifier = Modifier.weight(1f)
                        )
                        BucketSummaryMiniCard(
                            title = "Quỹ NAV",
                            total = uiState.summary.fundNavTotal,
                            icon = Icons.Default.ShowChart,
                            color = Color(0xFF38BDF8),
                            isPrivacyMode = isPrivacyMode,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                // Tabs for 3 Buckets
                item {
                    TabRow(
                        selectedTabIndex = selectedTabIndex,
                        containerColor = Slate850,
                        contentColor = Gold400,
                        indicator = { tabPositions ->
                            TabRowDefaults.SecondaryIndicator(
                                Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                                color = Gold400,
                                height = 3.dp
                            )
                        },
                        modifier = Modifier.clip(RoundedCornerShape(14.dp))
                    ) {
                        tabBuckets.forEachIndexed { index, bucket ->
                            Tab(
                                selected = selectedTabIndex == index,
                                onClick = { selectedTabIndex = index },
                                text = {
                                    Text(
                                        text = bucket.displayName,
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                        color = if (selectedTabIndex == index) Gold400 else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            )
                        }
                    }
                }

                // Filtered Asset List for the Selected Tab
                val currentBucket = tabBuckets[selectedTabIndex]
                val currentAssets = uiState.allAssets.filter { it.bucket == currentBucket }

                if (currentAssets.isEmpty()) {
                    item {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Slate850)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(28.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text(
                                    text = "Chưa có tài sản trong ${currentBucket.displayName}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                                Spacer(modifier = Modifier.height(12.dp))
                                Button(
                                    onClick = { showAddDialog = true },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = Gold400,
                                        contentColor = Slate950
                                    ),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(imageVector = Icons.Default.Add, contentDescription = null)
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("Thêm ngay")
                                }
                            }
                        }
                    }
                } else {
                    items(currentAssets, key = { it.id }) { asset ->
                        WealthAssetItemCard(
                            asset = asset,
                            isPrivacyMode = isPrivacyMode,
                            onDelete = { viewModel.deleteAsset(asset) }
                        )
                    }
                }

                // Bottom spacer for FAB
                item {
                    Spacer(modifier = Modifier.height(72.dp))
                }
            }
        }
    }

    if (showAddDialog) {
        AddAssetDialog(
            initialBucket = tabBuckets[selectedTabIndex],
            onDismiss = { showAddDialog = false },
            onConfirm = { bucket, name, units, unitPrice ->
                viewModel.addAsset(bucket, name, units, unitPrice)
            }
        )
    }

    if (showCustomizeGoalDialog) {
        CustomizeGoalDialog(
            currentGoal = uiState.goalEntity,
            currentNetWorth = uiState.goalReport.currentNetWorth,
            monthlyContribution = uiState.goalReport.monthlyContribution,
            onDismiss = { showCustomizeGoalDialog = false },
            onSave = { birthYear, targetAge, targetAmount, returnRate ->
                viewModel.updateGoal(birthYear, targetAge, targetAmount, returnRate)
            }
        )
    }
}

@Composable
private fun MilestoneHeroCard(
    goalReport: WealthGoalReport,
    isPrivacyMode: Boolean = false,
    onConfigureClick: () -> Unit
) {
    val progressAnimated by animateFloatAsState(
        targetValue = (goalReport.progressPercentage / 100f).coerceIn(0f, 1f),
        animationSpec = tween(800),
        label = "netWorthProgress"
    )

    val (statusColor, statusBg) = when (goalReport.status) {
        FeasibilityStatus.SURPLUS -> StatusSafe to StatusSafe.copy(alpha = 0.15f)
        FeasibilityStatus.BALANCED -> Gold400 to Gold400.copy(alpha = 0.15f)
        FeasibilityStatus.NEEDS_BOOST -> StatusDanger to StatusDanger.copy(alpha = 0.15f)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(Gold400.copy(alpha = 0.6f), Slate700)
                ),
                shape = RoundedCornerShape(24.dp)
            ),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            // Header Row: Tiêu đề + Feasibility Badge + Nút Setting
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "TỔNG TÀI SẢN HIỆN CÓ",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        letterSpacing = 1.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Mục tiêu ${FormatUtils.formatVndShort(goalReport.targetAmount)} tuổi ${goalReport.targetAge}",
                        style = MaterialTheme.typography.bodySmall,
                        color = Gold400,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(statusBg)
                            .padding(horizontal = 10.dp, vertical = 5.dp)
                    ) {
                        Text(
                            text = goalReport.status.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = statusColor,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onConfigureClick,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Cấu hình mục tiêu",
                            tint = Gold400,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Hero Net Worth
            Text(
                text = FormatUtils.formatVnd(goalReport.currentNetWorth, isPrivacyMode = isPrivacyMode),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.ExtraBold,
                color = Gold400
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Progress bar
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Tiến độ đạt mục tiêu tuổi ${goalReport.targetAge}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${String.format("%.1f", goalReport.progressPercentage)}%",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Gold400
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                    progress = { progressAnimated },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(CircleShape),
                    color = Gold400,
                    trackColor = Slate700
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Compound Projection Box
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
                        text = "Dự kiến ở tuổi ${goalReport.targetAge} (Lãi kép)",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = FormatUtils.formatVnd(goalReport.projectedFutureValue, isPrivacyMode = isPrivacyMode),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Gold400
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
                        text = "Lãi suất bình quân",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${String.format("%.1f", goalReport.expectedAnnualReturn)}%/năm",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Emerald500
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = goalReport.adviceMessage,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )
        }
    }
}

@Composable
private fun YearlyRoadmapCard(
    milestones: List<YearlyMilestone>,
    isPrivacyMode: Boolean = false
) {
    var isExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.TrendingUp,
                        contentDescription = null,
                        tint = Emerald500,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Lộ trình lãi kép từng năm",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isExpanded) "Thu gọn" else "Xem chi tiết",
                        style = MaterialTheme.typography.labelSmall,
                        color = Gold400,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = null,
                        tint = Gold400,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    milestones.forEach { m ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(Slate800)
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = "Tuổi ${m.age} (${m.year})",
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Tiền gốc: ${FormatUtils.formatVndShort(m.totalContributed, isPrivacyMode = isPrivacyMode)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = FormatUtils.formatVnd(m.projectedValue, isPrivacyMode = isPrivacyMode),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Gold400
                                )
                                Text(
                                    text = "+ ${FormatUtils.formatVndShort(m.interestEarned, isPrivacyMode = isPrivacyMode)} tiền lãi",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = Emerald500
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BucketSummaryMiniCard(
    title: String,
    total: Double,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    isPrivacyMode: Boolean = false,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Slate850)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = FormatUtils.formatVndShort(total, isPrivacyMode = isPrivacyMode),
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
    }
}

@Composable
private fun WealthAssetItemCard(
    asset: WealthAssetEntity,
    isPrivacyMode: Boolean = false,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
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
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = asset.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "${FormatUtils.formatUnits(asset.units)} ${asset.bucket.unitLabel} • Đơn giá: ${FormatUtils.formatVndShort(asset.unitPrice)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = FormatUtils.formatVnd(asset.totalValue, isPrivacyMode = isPrivacyMode),
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Gold400
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
