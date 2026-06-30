package com.tripexpense.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.tripexpense.ui.theme.skeleton
import com.tripexpense.ui.theme.skeletonHighlight

@Composable
fun Skeleton(
    width: Dp = 0.dp,
    height: Dp = 20.dp,
    borderRadius: Dp = 8.dp,
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(800),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeletonAlpha",
    )

    val widthMod = if (width > 0.dp) Modifier.width(width) else Modifier.fillMaxWidth()

    Box(
        modifier = modifier
            .then(widthMod)
            .height(height)
            .clip(RoundedCornerShape(borderRadius))
            .background(skeletonHighlight.copy(alpha = alpha))
    )
}
