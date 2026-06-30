package com.tripexpense.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.ui.theme.*

@Composable
fun BalanceCard(
    netBalance: Long,
    groupCount: Int = 0,
    modifier: Modifier = Modifier,
) {
    val (balanceText, balanceColor) = when {
        netBalance > 0 -> "You are owed \$${"%.2f".format(netBalance / 100.0)}" to tint
        netBalance < 0 -> "You owe \$${"%.2f".format(-netBalance / 100.0)}" to red
        else -> "All settled up" to textSecondary
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgSecondary),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "YOUR BALANCES",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = textSecondary,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = balanceText,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor,
            )
            if (groupCount > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Across $groupCount groups",
                    fontSize = 15.sp,
                    color = textSecondary,
                )
            }
        }
    }
}
