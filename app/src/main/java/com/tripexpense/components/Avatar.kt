package com.tripexpense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bumptech.glide.integration.compose.ExperimentalGlideComposeApi
import com.bumptech.glide.integration.compose.GlideImage
import com.tripexpense.ui.theme.avatarColors

@OptIn(ExperimentalGlideComposeApi::class)
@Composable
fun Avatar(
    name: String,
    photoURL: String? = null,
    size: Dp = 40.dp,
    modifier: Modifier = Modifier,
) {
    val bgColor = remember(name) {
        val hash = name.hashCode()
        val index = (hash and Int.MAX_VALUE) % avatarColors.size
        avatarColors[index]
    }
    val initials = remember(name) {
        val parts = name.trim().split("\\s+".toRegex())
        when {
            parts.size >= 2 -> "${parts.first().first()}${parts.last().first()}"
            parts.size == 1 -> parts.first().firstOrNull()?.uppercase() ?: "?"
            else -> "?"
        }
    }
    val fontSize = when {
        size >= 80.dp -> 28.sp
        size >= 48.dp -> 20.sp
        else -> 16.sp
    }

    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(if (photoURL != null) Color.Transparent else bgColor),
        contentAlignment = Alignment.Center,
    ) {
        if (photoURL != null) {
            GlideImage(
                model = photoURL,
                contentDescription = name,
                modifier = Modifier.size(size).clip(CircleShape),
                contentScale = ContentScale.Crop,
            )
        } else {
            Text(
                text = initials.uppercase(),
                color = Color.White,
                fontSize = fontSize,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
