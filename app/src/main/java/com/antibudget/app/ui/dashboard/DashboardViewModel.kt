package com.antibudget.app.ui.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.antibudget.app.data.local.entity.BucketType
import com.antibudget.app.data.local.entity.BudgetCycleEntity
import com.antibudget.app.data.local.entity.TransactionEntity
import com.antibudget.app.data.repository.FinanceRepository
import com.antibudget.app.domain.BurnRateCalculator
import com.antibudget.app.domain.model.BurnRateReport
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * UI State cho màn hình Dashboard Anti-Budget
 */
data class DashboardUiState(
    val isLoading: Boolean = true,
    val isPrivacyMode: Boolean = false,
    val activeCycle: BudgetCycleEntity? = null,
    val burnRateReport: BurnRateReport = BurnRateCalculator.calculate(13_000_000.0, 0.0),
    val recentTransactions: List<TransactionEntity> = emptyList(),
    val fixedTransactions: List<TransactionEntity> = emptyList()
)

/**
 * ViewModel xử lý logic màn hình chính và Burn-Rate Safe-to-Spend
 */
@OptIn(ExperimentalCoroutinesApi::class)
class DashboardViewModel(
    private val repository: FinanceRepository
) : ViewModel() {

    private val _isPrivacyMode = MutableStateFlow(false)
    private val _recentTransactions = repository.getRecentTransactions(limit = 10)
    private val _activeCycle = repository.getActiveCycle()

    // Lắng nghe số tiền biến động đã tiêu theo cycleId hiện tại
    private val _variableSpent = _activeCycle.flatMapLatest { cycle ->
        if (cycle != null) {
            repository.getVariableSpent(cycle.id)
        } else {
            flowOf(0.0)
        }
    }

    // Lắng nghe các giao dịch Chi phí cố định trong tháng
    private val _fixedTransactions = _activeCycle.flatMapLatest { cycle ->
        if (cycle != null) {
            repository.getFixedBillsForCycle(cycle.id)
        } else {
            flowOf(emptyList())
        }
    }

    val uiState: StateFlow<DashboardUiState> = combine(
        _activeCycle,
        _variableSpent,
        _recentTransactions,
        _fixedTransactions,
        _isPrivacyMode
    ) { cycle, spent, transactions, fixedBills, privacy ->
        val variablePool = cycle?.variablePool ?: 13_000_000.0
        val report = BurnRateCalculator.calculate(
            variablePool = variablePool,
            spentVariable = spent
        )

        DashboardUiState(
            isLoading = false,
            isPrivacyMode = privacy,
            activeCycle = cycle,
            burnRateReport = report,
            recentTransactions = transactions,
            fixedTransactions = fixedBills
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DashboardUiState()
    )

    /**
     * Bật / Tắt chế độ riêng tư che số tiền
     */
    fun togglePrivacyMode() {
        _isPrivacyMode.value = !_isPrivacyMode.value
    }

    /**
     * Thêm nhanh giao dịch tiền mặt hoặc thủ công
     */
    fun addQuickExpense(amount: Double, note: String, bankSource: String = "Tiền mặt") {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                note = note.ifBlank { "Chi tiêu tiền mặt" },
                bankSource = bankSource,
                bucketType = BucketType.VARIABLE
            )
        }
    }

    /**
     * Chuyển đổi loại ngân sách của giao dịch (VARIABLE <-> FIXED <-> SAVINGS)
     */
    fun switchTransactionBucket(tx: TransactionEntity, newBucket: BucketType) {
        viewModelScope.launch {
            repository.updateTransactionBucket(tx.id, newBucket)
        }
    }

    /**
     * Đánh dấu nhanh đã thanh toán một khoản cố định (Tiền nhà, học phí, điện nước)
     */
    fun markFixedBillPaid(billTitle: String, amount: Double) {
        viewModelScope.launch {
            repository.addTransaction(
                amount = amount,
                note = "Đã thanh toán $billTitle",
                bankSource = "Thủ công",
                bucketType = BucketType.FIXED
            )
        }
    }

    /**
     * Khởi tạo hoặc cập nhật chu kỳ lương tháng (Payday Allocation)
     */
    fun updatePaydayAllocation(
        income: Double,
        savings: Double,
        fixedBills: Double,
        variablePool: Double
    ) {
        viewModelScope.launch {
            repository.createNewCycle(
                totalIncome = income,
                savingsTarget = savings,
                fixedBills = fixedBills,
                variablePool = variablePool
            )
        }
    }

    /**
     * Xóa giao dịch nếu người dùng nhập nhầm
     */
    fun deleteTransaction(tx: TransactionEntity) {
        viewModelScope.launch {
            repository.deleteTransaction(tx)
        }
    }

    companion object {
        fun provideFactory(repository: FinanceRepository): ViewModelProvider.Factory =
            object : ViewModelProvider.Factory {
                @Suppress("UNCHECKED_CAST")
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    return DashboardViewModel(repository) as T
                }
            }
    }
}
