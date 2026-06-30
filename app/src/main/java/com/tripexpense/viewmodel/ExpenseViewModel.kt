package com.tripexpense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripexpense.data.model.SplitStrategy
import com.tripexpense.data.repository.FirestoreRepository
import kotlinx.coroutines.launch

class ExpenseViewModel : ViewModel() {
    private val firestoreRepo = FirestoreRepository()

    fun addExpense(
        groupId: String,
        description: String,
        amount: Long,
        paidBy: String,
        splitStrategy: SplitStrategy,
        owedBy: Map<String, Long>,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                firestoreRepo.createExpense(
                    groupId = groupId,
                    description = description,
                    amount = amount,
                    paidBy = paidBy,
                    splitStrategy = splitStrategy,
                    owedBy = owedBy,
                )
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to add expense")
            }
        }
    }

    fun updateExpense(
        groupId: String,
        expenseId: String,
        description: String,
        amount: Long,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                firestoreRepo.updateExpense(groupId, expenseId, description, amount)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to update expense")
            }
        }
    }

    fun deleteExpense(
        groupId: String,
        expenseId: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                firestoreRepo.deleteExpense(groupId, expenseId)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to delete expense")
            }
        }
    }
}
