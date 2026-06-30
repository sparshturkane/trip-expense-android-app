package com.tripexpense.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tripexpense.ui.theme.*

@Composable
fun AppTextInput(
    value: String,
    onValueChange: (String) -> Unit,
    label: String? = null,
    placeholder: String = "",
    prefix: String? = null,
    error: String? = null,
    singleLine: Boolean = true,
    modifier: Modifier = Modifier,
) {
    val borderColor = if (error != null) red else Color.Transparent

    androidx.compose.foundation.layout.Column(modifier = modifier) {
        if (label != null) {
            Text(
                text = label.uppercase(),
                fontSize = 13.sp,
                color = textSecondary,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.padding(bottom = 6.dp),
            )
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 52.dp)
                .background(bgSecondary, RoundedCornerShape(12.dp))
                .then(
                    if (borderColor != Color.Transparent)
                        Modifier.padding(1.dp)
                    else Modifier
                )
                .padding(horizontal = 16.dp),
        ) {
            if (prefix != null) {
                Text(
                    text = prefix,
                    fontSize = 17.sp,
                    color = textPrimary,
                    fontWeight = FontWeight.Medium,
                )
            }
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = singleLine,
                textStyle = TextStyle(
                    fontSize = 17.sp,
                    color = textPrimary,
                ),
                cursorBrush = SolidColor(tint),
                decorationBox = { innerTextField ->
                    Box {
                        if (value.isEmpty()) {
                            Text(
                                text = placeholder,
                                fontSize = 17.sp,
                                color = textTertiary,
                            )
                        }
                        innerTextField()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (error != null) {
            Text(
                text = error,
                fontSize = 13.sp,
                color = red,
                modifier = Modifier.padding(top = 4.dp, start = 4.dp),
            )
        }
    }
}
