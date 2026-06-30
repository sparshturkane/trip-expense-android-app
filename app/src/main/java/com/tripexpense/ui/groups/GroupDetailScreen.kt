package com.tripexpense.ui.groups

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tripexpense.components.BalanceSheet
import com.tripexpense.components.EmptyState
import com.tripexpense.components.ExpenseItem
import com.tripexpense.components.Skeleton
import com.tripexpense.data.model.BalanceEntry
import com.tripexpense.data.repository.FirestoreRepository
import com.tripexpense.navigation.Screen
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import com.tripexpense.viewmodel.ExpenseViewModel
import com.tripexpense.viewmodel.GroupViewModel
import java.net.URLEncoder

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GroupDetailScreen(
    groupId: String,
    groupName: String,
    navController: NavHostController,
    groupViewModel: GroupViewModel,
    expenseViewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
) {
    val group by groupViewModel.groupDetail.collectAsState()
    val expenses by groupViewModel.expenses.collectAsState()
    val isLoading by groupViewModel.expensesLoading.collectAsState()
    val unsyncedIds by groupViewModel.unsyncedExpenseIds.collectAsState()
    val authState by authViewModel.state.collectAsState()
    val currentUserId = authState.user?.uid ?: ""
    val repo = remember { FirestoreRepository() }

    LaunchedEffect(groupId) {
        groupViewModel.subscribeToGroup(groupId)
        groupViewModel.subscribeToExpenses(groupId)
    }

    val memberNames = remember(group) {
        val names = mutableMapOf<String, String>()
        group?.members?.forEach { uid ->
            names[uid] = uid.take(6)
        }
        names
    }

    val balanceEntries = remember(group) {
        group?.balances?.let { repo.parseBalances(it) } ?: emptyList()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = groupName,
                        fontWeight = FontWeight.SemiBold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = bg,
                    titleContentColor = textPrimary,
                    navigationIconContentColor = blue,
                ),
            )
        },
        floatingActionButton = {
            if (!isLoading) {
                FloatingActionButton(
                    onClick = {
                        val memberList = group?.members ?: emptyList()
                        navController.navigate(
                            Screen.AddExpense.createRoute(groupId, memberList)
                        )
                    },
                    containerColor = tint,
                    contentColor = bg,
                    shape = CircleShape,
                    modifier = Modifier.size(56.dp),
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Expense")
                }
            }
        },
    ) { innerPadding ->
        if (isLoading) {
            LoadingGroupDetail(modifier = Modifier.padding(innerPadding))
        } else if (expenses.isEmpty()) {
            EmptyState(
                title = "No expenses yet",
                subtitle = "Add your first expense to start splitting",
                actionLabel = "Add Expense",
                onAction = {
                    val memberList = group?.members ?: emptyList()
                    navController.navigate(
                        Screen.AddExpense.createRoute(groupId, memberList)
                    )
                },
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                item {
                    BalanceSheet(
                        entries = balanceEntries,
                        memberNames = memberNames,
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Expenses",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = textSecondary,
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(expenses, key = { it.expenseId }) { expense ->
                    ExpenseItem(
                        expense = expense,
                        memberNames = memberNames,
                        currentUserId = currentUserId,
                        hasPendingWrites = unsyncedIds.contains(expense.expenseId),
                        onClick = {
                            navController.navigate(
                                Screen.EditExpense.createRoute(groupId, expense.expenseId)
                            )
                        },
                    )
                }
                item { Spacer(modifier = Modifier.height(88.dp)) }
            }
        }
    }
}

@Composable
private fun LoadingGroupDetail(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Skeleton(height = 120.dp, borderRadius = 16.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
        Skeleton(height = 80.dp, borderRadius = 12.dp)
    }
}
