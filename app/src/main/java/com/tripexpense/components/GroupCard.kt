package com.tripexpense.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.data.model.GroupWithMeta
import com.tripexpense.ui.theme.*

@Composable
fun GroupCard(
    group: GroupWithMeta,
    hasPendingWrites: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val balanceText = when {
        group.userBalance > 0 -> "+ \$${"%.2f".format(group.userBalance / 100.0)}"
        group.userBalance < 0 -> "- \$${"%.2f".format(-group.userBalance / 100.0)}"
        else -> "\$0.00"
    }
    val balanceColor = when {
        group.userBalance > 0 -> tint
        group.userBalance < 0 -> red
        else -> textSecondary
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = bg),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Avatar(
                name = group.group.name,
                size = 48.dp,
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = group.group.name,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false),
                    )
                    if (hasPendingWrites) {
                        UnsyncedBadge()
                    }
                }
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "${group.memberCount} members",
                    fontSize = 13.sp,
                    color = textSecondary,
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = balanceText,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = balanceColor,
            )
        }
    }
}
