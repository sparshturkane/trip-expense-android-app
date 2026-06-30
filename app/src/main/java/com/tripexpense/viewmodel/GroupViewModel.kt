package com.tripexpense.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tripexpense.data.model.Group
import com.tripexpense.data.model.GroupWithMeta
import com.tripexpense.data.repository.FirestoreRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class GroupListState(
    val groups: List<GroupWithMeta> = emptyList(),
    val netBalance: Long = 0,
    val isLoading: Boolean = true,
)

class GroupViewModel : ViewModel() {
    private val firestoreRepo = FirestoreRepository()

    private val _listState = MutableStateFlow(GroupListState())
    val listState: StateFlow<GroupListState> = _listState.asStateFlow()

    private val _groupDetail = MutableStateFlow<Group?>(null)
    val groupDetail: StateFlow<Group?> = _groupDetail.asStateFlow()

    private val _expenses = MutableStateFlow<List<com.tripexpense.data.model.Expense>>(emptyList())
    val expenses: StateFlow<List<com.tripexpense.data.model.Expense>> = _expenses.asStateFlow()

    private val _expensesLoading = MutableStateFlow(true)
    val expensesLoading: StateFlow<Boolean> = _expensesLoading.asStateFlow()

    private val _unsyncedExpenseIds = MutableStateFlow<Set<String>>(emptySet())
    val unsyncedExpenseIds: StateFlow<Set<String>> = _unsyncedExpenseIds.asStateFlow()

    private var groupsJob: kotlinx.coroutines.Job? = null
    private var groupJob: kotlinx.coroutines.Job? = null
    private var expensesJob: kotlinx.coroutines.Job? = null

    fun subscribeToGroups(userId: String) {
        groupsJob?.cancel()
        groupsJob = viewModelScope.launch {
            firestoreRepo.groupsFlow(userId).collect { groups ->
                val withMeta = groups.map { group ->
                    GroupWithMeta(
                        group = group,
                        userBalance = firestoreRepo.computeUserBalance(group, userId),
                        memberCount = group.members.size,
                    )
                }
                _listState.value = GroupListState(
                    groups = withMeta,
                    netBalance = firestoreRepo.computeNetBalance(withMeta),
                    isLoading = false,
                )
            }
        }
    }

    fun subscribeToGroup(groupId: String) {
        groupJob?.cancel()
        groupJob = viewModelScope.launch {
            firestoreRepo.groupFlow(groupId).collect { group ->
                _groupDetail.value = group
            }
        }
    }

    fun subscribeToExpenses(groupId: String) {
        expensesJob?.cancel()
        expensesJob = viewModelScope.launch {
            firestoreRepo.expensesFlow(groupId).collect { expenseList ->
                _expenses.value = expenseList
                _expensesLoading.value = false
            }
        }
    }

    fun createGroup(
        name: String,
        memberIds: List<String>,
        createdBy: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit,
    ) {
        viewModelScope.launch {
            try {
                firestoreRepo.createGroup(name, memberIds, createdBy)
                onSuccess()
            } catch (e: Exception) {
                onError(e.message ?: "Failed to create group")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        groupsJob?.cancel()
        groupJob?.cancel()
        expensesJob?.cancel()
    }
}
