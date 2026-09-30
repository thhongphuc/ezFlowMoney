package com.antibudget.app.ui.wealth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antibudget.app.data.local.entity.WealthAssetEntity
import com.antibudget.app.data.local.entity.WealthBucket
import com.antibudget.app.data.local.entity.WealthGoalEntity
import com.antibudget.app.data.repository.FinanceRepository
import com.antibudget.app.domain.WealthProjectionEngine
import com.antibudget.app.domain.model.AssetSummary
import com.antibudget.app.domain.model.WealthGoalReport
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.math.max

/**
 * UI State cho màn hình Tích sản 3 Lọ & Hoạch định mục tiêu tài chính
 */
data class WealthUiState(
    val isLoading: Boolean = true,
    val summary: AssetSummary = AssetSummary(
        totalNetWorth = 0.0,
        progressPercentage = 0f,
        bankSavingsTotal = 0.0,
        gold9999Total = 0.0,
        fundNavTotal = 0.0,
        remainingToTarget = 600_000_000.0
    ),
    val goalEntity: WealthGoalEntity = WealthGoalEntity(),
    val goalReport: WealthGoalReport = WealthProjectionEngine.calculateReport(
        goal = WealthGoalEntity(),
        currentNetWorth = 0.0,
        monthlyContribution = 12_000_000.0
    ),
    val allAssets: List<WealthAssetEntity> = emptyList(),
    val bankSavingsAssets: List<WealthAssetEntity> = emptyList(),
    val goldAssets: List<WealthAssetEntity> = emptyList(),
    val fundAssets: List<WealthAssetEntity> = emptyList()
)

/**
 * ViewModel quản lý Tích sản 3 Lọ & Động cơ hoạch định tài chính
 */
class WealthViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    val uiState: StateFlow<WealthUiState> = combine(
        repository.getAllWealthAssets(),
        repository.getTotalNetWorth(),
        repository.getWealthGoal(),
        repository.getActiveCycle()
    ) { assets, totalNetWorth, goalFromDb, cycle ->
        val goal = goalFromDb ?: WealthGoalEntity()
        val monthlyContribution = cycle?.savingsTarget ?: 12_000_000.0

        val bankTotal = assets.filter { it.bucket == WealthBucket.BANK_SAVINGS }.sumOf { it.totalValue }
        val goldTotal = assets.filter { it.bucket == WealthBucket.GOLD_9999 }.sumOf { it.totalValue }
        val fundTotal = assets.filter { it.bucket == WealthBucket.FUND_NAV }.sumOf { it.totalValue }

        val goalReport = WealthProjectionEngine.calculateReport(
            goal = goal,
            currentNetWorth = totalNetWorth,
            monthlyContribution = monthlyContribution
        )

        val progress = ((totalNetWorth / goal.targetAmount) * 100.0).toFloat().coerceIn(0f, 100f)
        val remaining = max(0.0, goal.targetAmount - totalNetWorth)

        val summary = AssetSummary(
            totalNetWorth = totalNetWorth,
            targetNetWorth = goal.targetAmount,
            progressPercentage = progress,
            bankSavingsTotal = bankTotal,
            gold9999Total = goldTotal,
            fundNavTotal = fundTotal,
            remainingToTarget = remaining
        )

        WealthUiState(
            isLoading = false,
            summary = summary,
            goalEntity = goal,
            goalReport = goalReport,
            allAssets = assets,
            bankSavingsAssets = assets.filter { it.bucket == WealthBucket.BANK_SAVINGS },
            goldAssets = assets.filter { it.bucket == WealthBucket.GOLD_9999 },
            fundAssets = assets.filter { it.bucket == WealthBucket.FUND_NAV }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = WealthUiState()
    )

    fun updateGoal(birthYear: Int, targetAge: Int, targetAmount: Double, returnRate: Double) {
        viewModelScope.launch {
            repository.updateWealthGoal(birthYear, targetAge, targetAmount, returnRate)
        }
    }

    fun addAsset(bucket: WealthBucket, name: String, units: Double, unitPrice: Double) {
        viewModelScope.launch {
            val asset = WealthAssetEntity(
                bucket = bucket,
                name = name,
                units = units,
                unitPrice = unitPrice,
                totalValue = units * unitPrice
            )
            repository.addWealthAsset(asset)
        }
    }

    fun updateAsset(asset: WealthAssetEntity) {
        viewModelScope.launch {
            repository.updateWealthAsset(asset)
        }
    }

    fun deleteAsset(asset: WealthAssetEntity) {
        viewModelScope.launch {
            repository.deleteWealthAsset(asset)
        }
    }

    companion object {
        fun provideFactory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return WealthViewModel(repository) as T
                }
            }
    }
}
