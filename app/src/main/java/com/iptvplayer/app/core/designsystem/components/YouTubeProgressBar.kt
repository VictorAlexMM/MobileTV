package com.iptvplayer.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Barra de progresso inspirada fielmente no estilo visual do YouTube:
 * Fundo escuro sutil e preenchimento em vermelho vivo proporcional
 * ao tempo já assistido do vídeo/episódio.
 */
@Composable
fun YouTubeProgressBar(
    progressFraction: Float,
    modifier: Modifier = Modifier,
    barHeight: Dp = 3.5.dp,
    barColor: Color = Color(0xFFFF0000), // Vermelho clássico YouTube (#FF0000)
    backgroundColor: Color = Color.Black.copy(alpha = 0.6f)
) {
    val clampedProgress = progressFraction.coerceIn(0f, 1f)
    if (clampedProgress > 0f) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(barHeight)
                .background(backgroundColor)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(clampedProgress)
                    .fillMaxHeight()
                    .background(barColor)
            )
        }
    }
}
