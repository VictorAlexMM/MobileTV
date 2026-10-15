package com.iptvplayer.app.core.designsystem.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ripple
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber

/**
 * Botão animado de download inspirado no padrão oficial da Netflix.
 *
 * Estados:
 * 1. Não baixado: Ícone de download elegante com seta e bandeja.
 * 2. Iniciando / Em fila (QUEUED): Animação de rotação com arco contínuo e seta deslizando suavemente.
 * 3. Baixando (DOWNLOADING): Anel circular de progresso em tempo real (0-100%) com ícone de pausa/stop quadrado central.
 * 4. Concluído (COMPLETED): Pop elástico (spring) com ícone de checkmark circular verde esmeralda.
 * 5. Pausado / Falha: Ícones de retomada ou retry com feedbacks visuais claros.
 */
@Composable
fun NetflixDownloadButton(
    status: DownloadStatus?,
    percent: Float,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 20.dp,
    activeColor: Color = PrimaryIndigo,
    completedColor: Color = StatusDownloadedGreen
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Animação infinita para o estado QUEUED / Iniciando
    val infiniteTransition = rememberInfiniteTransition(label = "NetflixDownloadTransition")
    val spinningAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "SpinAngle"
    )

    val arrowSlide by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 2f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ArrowSlide"
    )

    // Animação de escala (pop) para quando o download conclui
    val isCompleted = status == DownloadStatus.COMPLETED
    val popScale by animateFloatAsState(
        targetValue = if (isCompleted) 1f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "PopScale"
    )

    // Progresso suave animado
    val animatedProgress by animateFloatAsState(
        targetValue = (percent / 100f).coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 350, easing = FastOutSlowInEasing),
        label = "AnimatedProgress"
    )

    Box(
        modifier = modifier
            .size(size)
            .scale(popScale)
            .clip(CircleShape)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(bounded = true, radius = size / 2),
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        when (status) {
            null -> {
                // Estado 1: Inicial - Ícone padrão de Download
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Baixar",
                    tint = Color.White,
                    modifier = Modifier.size(iconSize)
                )
            }

            DownloadStatus.QUEUED -> {
                // Estado 2: Netflix Initiation - Spinner circular contínuo com seta pulsando
                Canvas(modifier = Modifier.size(size - 6.dp)) {
                    val strokeWidth = 2.5.dp.toPx()
                    // Trilho de fundo
                    drawCircle(
                        color = Color.White.copy(alpha = 0.15f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Arco girando em 360 graus
                    drawArc(
                        color = activeColor,
                        startAngle = spinningAngle,
                        sweepAngle = 100f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = "Iniciando download",
                    tint = activeColor,
                    modifier = Modifier
                        .size(iconSize - 2.dp)
                        .scale(1f + (arrowSlide * 0.05f))
                )
            }

            DownloadStatus.DOWNLOADING -> {
                // Estado 3: Netflix Downloading - Anel de progresso preenchido em tempo real + quadrado de stop central
                Canvas(modifier = Modifier.size(size - 6.dp)) {
                    val strokeWidth = 2.5.dp.toPx()
                    // Trilho cinza de fundo
                    drawCircle(
                        color = Color.White.copy(alpha = 0.2f),
                        style = Stroke(width = strokeWidth)
                    )
                    // Arco de progresso ativo
                    val sweep = (animatedProgress * 360f).coerceAtLeast(8f)
                    drawArc(
                        color = activeColor,
                        startAngle = -90f,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )

                    // Quadrado central estilo Netflix (Stop/Pause)
                    val squareSize = 8.dp.toPx()
                    val topLeft = Offset(
                        x = (size.toPx() - 6.dp.toPx() - squareSize) / 2f,
                        y = (size.toPx() - 6.dp.toPx() - squareSize) / 2f
                    )
                    drawRoundRect(
                        color = activeColor,
                        topLeft = topLeft,
                        size = Size(squareSize, squareSize),
                        cornerRadius = CornerRadius(2.dp.toPx(), 2.dp.toPx())
                    )
                }
            }

            DownloadStatus.COMPLETED -> {
                // Estado 4: Download Concluído - Checkmark verde esmeralda com brilho
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = "Download concluído",
                    tint = completedColor,
                    modifier = Modifier.size(iconSize + 2.dp)
                )
            }

            DownloadStatus.PAUSED -> {
                // Estado 5: Pausado - Anel pontilhado/estático com play para retomar
                Canvas(modifier = Modifier.size(size - 6.dp)) {
                    val strokeWidth = 2.dp.toPx()
                    drawCircle(
                        color = StatusWarningAmber.copy(alpha = 0.5f),
                        style = Stroke(width = strokeWidth)
                    )
                }
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Retomar download",
                    tint = StatusWarningAmber,
                    modifier = Modifier.size(iconSize)
                )
            }

            DownloadStatus.FAILED -> {
                // Estado 6: Falha - Ícone de retry com aviso
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Tentar novamente",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(iconSize)
                )
            }
        }
    }
}
