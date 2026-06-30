package com.tripexpense.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.ui.theme.*

enum class ButtonVariant {
    PRIMARY, SECONDARY, DANGER, GHOST
}

@Composable
fun AppButton(
    title: String,
    onClick: () -> Unit,
    variant: ButtonVariant = ButtonVariant.PRIMARY,
    enabled: Boolean = true,
    loading: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val (bgColor, textColor) = when (variant) {
        ButtonVariant.PRIMARY -> tint to Color.White
        ButtonVariant.SECONDARY -> bgSecondary to textPrimary
        ButtonVariant.DANGER -> red to Color.White
        ButtonVariant.GHOST -> Color.Transparent to blue
    }

    Button(
        onClick = onClick,
        enabled = enabled && !loading,
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = bgColor,
            disabledContainerColor = bgColor.copy(alpha = 0.5f),
        ),
    ) {
        if (loading) {
            CircularProgressIndicator(
                color = if (variant == ButtonVariant.PRIMARY || variant == ButtonVariant.DANGER)
                    Color.White else tint,
                strokeWidth = 2.dp,
                modifier = Modifier.height(20.dp),
            )
        } else {
            Text(
                text = title,
                color = textColor,
                fontSize = 17.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}
