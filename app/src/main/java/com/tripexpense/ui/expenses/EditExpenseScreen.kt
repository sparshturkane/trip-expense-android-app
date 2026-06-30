package com.tripexpense.ui.expenses

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import com.tripexpense.components.ButtonVariant
import com.tripexpense.ui.theme.*
import com.tripexpense.viewmodel.ExpenseViewModel

@Composable
fun EditExpenseScreen(
    groupId: String,
    expenseId: String,
    navController: NavHostController,
    expenseViewModel: ExpenseViewModel,
) {
    var description by remember { mutableStateOf("") }
    var amountText by remember { mutableStateOf("") }
    var isSubmitting by remember { mutableStateOf(false) }
    var showDeleteDialog by remember { mutableStateOf(false) }

    val amountInCents = remember(amountText) {
        amountText.filter { it.isDigit() }.let { digits ->
            if (digits.isEmpty()) 0L else digits.toLong()
        }
    }
    val isValid = description.isNotBlank() && amountInCents > 0

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete Expense") },
            text = { Text("Are you sure you want to delete this expense?") },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    expenseViewModel.deleteExpense(
                        groupId = groupId,
                        expenseId = expenseId,
                        onSuccess = { navController.popBackStack() },
                        onError = {},
                    )
                }) {
                    Text("Delete", color = red)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        Text(
            text = "Edit Expense",
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
            value = if (amountText.isEmpty()) "" else amountText,
            onValueChange = { amountText = it.filter { c -> c.isDigit() } },
            label = "AMOUNT",
            placeholder = "0.00",
            prefix = "$",
            modifier = Modifier.fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(32.dp))

        AppButton(
            title = "Save",
            onClick = {
                isSubmitting = true
                expenseViewModel.updateExpense(
                    groupId = groupId,
                    expenseId = expenseId,
                    description = description,
                    amount = amountInCents,
                    onSuccess = { navController.popBackStack() },
                    onError = { isSubmitting = false },
                )
            },
            loading = isSubmitting,
            enabled = isValid,
        )
        Spacer(modifier = Modifier.height(16.dp))
        AppButton(
            title = "Delete Expense",
            onClick = { showDeleteDialog = true },
            variant = ButtonVariant.DANGER,
        )
    }
}
