package com.tripexpense.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.data.model.Expense
import com.tripexpense.ui.theme.*

@Composable
fun ExpenseItem(
    expense: Expense,
    memberNames: Map<String, String>,
    currentUserId: String,
    hasPendingWrites: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val payerName = memberNames[expense.paidBy] ?: expense.paidBy
    val isCurrentUserPayer = expense.paidBy == currentUserId
    val userShare = expense.owedBy[currentUserId] ?: 0L

    val shareText = when {
        isCurrentUserPayer && userShare == 0L -> null
        userShare > 0 -> "you owe \$${"%.2f".format(userShare / 100.0)}"
        else -> "others owe \$${"%.2f".format(-userShare / 100.0)}"
    }
    val shareColor = when {
        shareText == null -> textSecondary
        userShare > 0 -> red
        else -> tint
    }

    val relativeTime = formatRelativeTime(expense.timestamp?.seconds ?: 0)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(
            name = payerName,
            size = 40.dp,
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = expense.description,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = "\$${"%.2f".format(expense.amount / 100.0)}",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = textPrimary,
                )
            }
            Spacer(modifier = Modifier.padding(2.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = payerName,
                    fontSize = 13.sp,
                    color = textSecondary,
                )
                Text(
                    text = " \u00B7 $relativeTime",
                    fontSize = 13.sp,
                    color = textSecondary,
                )
                if (hasPendingWrites) {
                    Spacer(modifier = Modifier.width(6.dp))
                    UnsyncedBadge()
                }
            }
            if (shareText != null) {
                Text(
                    text = shareText,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = shareColor,
                )
            }
        }
    }
}

private fun formatRelativeTime(epochSeconds: Long): String {
    if (epochSeconds == 0L) return ""
    val now = System.currentTimeMillis() / 1000
    val diff = now - epochSeconds
    return when {
        diff < 60 -> "Just now"
        diff < 3600 -> "${diff / 60}m ago"
        diff < 86400 -> "${diff / 3600}h ago"
        diff < 2592000 -> "${diff / 86400}d ago"
        else -> {
            val date = java.text.SimpleDateFormat("MMM d", java.util.Locale.getDefault())
            date.format(java.util.Date(epochSeconds * 1000))
        }
    }
}
