package com.tripexpense.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import com.tripexpense.components.AppButton
import com.tripexpense.components.AppTextInput
import com.tripexpense.components.SegmentedControl
import com.tripexpense.data.model.SplitStrategy
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.AuthViewModel
import com.tripexpense.viewmodel.ExpenseViewModel

@Composable
fun AddExpenseScreen(
    groupId: String,
    members: List<String>,
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel,
    authViewModel: AuthViewModel,
) {
    val authState by authViewModel.state.collectAsState()
    val currentUserId = authState.user?.uid ?: ""

    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var paidBy by remember { mutableStateOf(currentUserId) }
    var splitIndex by remember { mutableIntStateOf(0) }
    var isSubmitting by remember { mutableStateOf(false) }

    val amountInCents = remember(amountText) {
        amountText.filter { it.isDigit() }.let { digits ->
            if (digits.isEmpty()) 0L else digits.toLong()
        }
    }
    val displayAmount = remember(amountInCents) {
        if (amountInCents == 0L) "" else "\$${"%.2f".format(amountInCents / 100.0)}"
    }

    val isValid = description.isNotBlank() && amountInCents > 0 && paidBy.isNotEmpty()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Add Expense",
            fontSize = 22.sp,
            fontWeight = FontWeight.SemiBold,
            color = textPrimary,
        )
        Spacer(modifier = Modifier.height(24.dp))

        AppTextInput(
            value = description,
            onValueChange = { description = it },
            label = "DESCRIPTION",
            placeholder = "What was the expense for?",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(16.dp))

        AppTextInput(
            value = if (amountText.isEmpty()) "" else displayAmount,
            onValueChange = { amountText = it.filter { c -> c.isDigit() } },
            label = "AMOUNT",
            placeholder = "0.00",
            prefix = "$",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "PAID BY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        // Paid by row - simplified (pill buttons for each member)
        // In a full implementation, render pill buttons here

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "SPLIT STRATEGY",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = textSecondary,
        )
        Spacer(modifier = Modifier.height(8.dp))
        SegmentedControl(
            options = listOf("EQUAL", "EXACT"),
            selectedIndex = splitIndex,
            onSelect = { splitIndex = it },
        )

        Spacer(modifier = Modifier.height(32.dp))
        AppButton(
            title = "Save",
            onClick = {
                isSubmitting = true
                val strategy = if (splitIndex == 0) SplitStrategy.EQUAL else SplitStrategy.EXACT
                val equalShare = amountInCents / (members.size.coerceAtLeast(1))
                val remainder = amountInCents % (members.size.coerceAtLeast(1))
                val owedBy = members.mapIndexed { index, uid ->
                    uid to (equalShare + if (index < remainder) 1 else 0)
                }.toMap()

                expenseViewModel.addExpense(
                    groupId = groupId,
                    description = description,
                    amount = amountInCents,
                    paidBy = paidBy,
                    splitStrategy = strategy,
                    owedBy = owedBy,
                    onSuccess = { navController.popBackStack() },
                    onError = { isSubmitting = false },
                )
            },
            loading = isSubmitting,
            enabled = isValid,
        )
    }
}
