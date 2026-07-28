package com.akshay.fairshare.ui.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.akshay.fairshare.data.repository.ExpenseRepository
import com.akshay.fairshare.domain.balance.BalanceCalculator
import com.akshay.fairshare.domain.model.Balance
import com.akshay.fairshare.domain.model.Expense
import com.akshay.fairshare.domain.model.Group
import com.akshay.fairshare.domain.model.Money
import com.akshay.fairshare.domain.model.Settlement
import com.akshay.fairshare.domain.settle.SettlementCalculator
import com.akshay.fairshare.domain.split.SplitCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

data class GroupUiState(
    val group: Group? = null,
    val expenses: List<Expense> = emptyList(),
    val balances: List<Balance> = emptyList(),
    val settlements: List<Settlement> = emptyList(),
    val error: String? = null,
)

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val repository: ExpenseRepository,
) : ViewModel() {

    private val groupId = ExpenseRepository.DEMO_GROUP_ID
    private val errors = MutableStateFlow<String?>(null)

    init {
        viewModelScope.launch {
            repository.seedDemoGroupIfEmpty()
        }
    }

    val state: StateFlow<GroupUiState> = combine(
        repository.observeGroup(groupId),
        repository.observeExpenses(groupId),
        errors,
    ) { group, expenses, error ->
        val memberIds = group?.members?.map { it.id }.orEmpty()
        val balances = if (memberIds.isEmpty()) {
            emptyList()
        } else {
            BalanceCalculator.balances(memberIds, expenses)
        }
        GroupUiState(
            group = group,
            expenses = expenses,
            balances = balances,
            settlements = SettlementCalculator.settlements(balances),
            error = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), GroupUiState())

    fun addEvenlySplitExpense(description: String, amountText: String, paidBy: String) {
        val total = Money.parse(amountText)
        if (total == null || !total.isPositive) {
            errors.value = "Enter an amount like 12.50"
            return
        }
        val memberIds = state.value.group?.members?.map { it.id }.orEmpty()
        if (memberIds.isEmpty()) {
            errors.value = "Add someone to the group first"
            return
        }

        errors.value = null
        viewModelScope.launch {
            val expense = Expense(
                id = UUID.randomUUID().toString(),
                groupId = groupId,
                description = description.ifBlank { "Expense" },
                total = total,
                paidBy = paidBy,
                shares = SplitCalculator.splitEvenly(total, memberIds),
                createdAt = System.currentTimeMillis(),
            )
            repository.addExpense(expense).onFailure {
                errors.value = "Saved on this device. It will sync when you're back online."
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            repository.refresh(groupId).onFailure {
                errors.value = "Couldn't reach the server. Showing what's on this device."
            }
        }
    }

    fun dismissError() {
        errors.value = null
    }
}
