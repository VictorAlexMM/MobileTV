package com.iptvplayer.app.core.designsystem.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.scale
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Modifier de destaque de foco para Android TV e navegação por controle remoto (D-Pad).
 * Quando o elemento ganha foco via setas do controle da Smart TV, ele amplia suavemente
 * e exibe uma borda nítida de destaque, permitindo que o usuário identifique
 * com precisão onde o cursor da TV está posicionado a vários metros de distância.
 */
fun Modifier.tvFocusable(
    shape: Shape = RoundedCornerShape(12.dp),
    focusedBorderColor: Color = Color.White,
    focusedBorderWidth: Dp = 2.dp,
    focusedScale: Float = 1.04f
): Modifier = composed {
    var isFocused by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isFocused) focusedScale else 1.0f,
        animationSpec = tween(durationMillis = 150),
        label = "tvFocusScale"
    )

    this
        .onFocusChanged { isFocused = it.isFocused }
        .scale(scale)
        .then(
            if (isFocused) {
                Modifier.border(focusedBorderWidth, focusedBorderColor, shape)
            } else {
                Modifier
            }
        )
        .focusable()
}
