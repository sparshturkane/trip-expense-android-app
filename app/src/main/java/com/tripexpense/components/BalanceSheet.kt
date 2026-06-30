package com.tripexpense.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.data.model.BalanceEntry
import com.tripexpense.ui.theme.*

@Composable
fun BalanceSheet(
    entries: List<BalanceEntry>,
    memberNames: Map<String, String>,
    modifier: Modifier = Modifier,
) {
    if (entries.isEmpty()) return

    var expanded by remember { mutableStateOf(false) }

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = bgSecondary),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { expanded = !expanded },
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = "BALANCES",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = textSecondary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = if (expanded) "\u25BC" else "\u25B6",
                    fontSize = 12.sp,
                    color = textSecondary,
                )
            }
            AnimatedVisibility(visible = expanded) {
                Column(modifier = Modifier.padding(top = 12.dp)) {
                    entries.forEach { entry ->
                        val nameA = memberNames[entry.userA] ?: entry.userA
                        val nameB = memberNames[entry.userB] ?: entry.userB
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp),
                        ) {
                            Text(
                                text = "$nameA owes $nameB",
                                fontSize = 15.sp,
                                color = textPrimary,
                                modifier = Modifier.weight(1f),
                            )
                            Text(
                                text = "\$${"%.2f".format(entry.amount / 100.0)}",
                                fontSize = 15.sp,
                                fontWeight = FontWeight.Bold,
                                color = tint,
                            )
                        }
                    }
                }
            }
        }
    }
}
