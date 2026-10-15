package com.iptvplayer.app.feature.player

import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.iptvplayer.app.core.designsystem.components.YouTubeProgressBar
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import kotlinx.coroutines.delay

/**
 * Player compacto (16:9 embutido no topo da tela estilo YouTube Mobile / Twitch).
 * Inicia a reprodução sem forçar a tela cheia e permite alternar para tela cheia,
 * pausar, mutar o áudio e avançar/retroceder.
 */
@OptIn(UnstableApi::class)
@Composable
fun CompactPlayerView(
    playerEngine: PlayerEngine,
    onExpandFullscreen: () -> Unit,
    onClosePlayer: () -> Unit,
    onEnterPip: () -> Unit,
    modifier: Modifier = Modifier
) {
    val uiState by playerEngine.uiState.collectAsState()
    var showControls by remember { mutableStateOf(true) }

    LaunchedEffect(showControls, uiState.isPlaying) {
        if (showControls && uiState.isPlaying) {
            delay(3500)
            showControls = false
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(16f / 9f)
            .background(Color.Black)
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Superfície de Vídeo
        AndroidView(
            factory = { context ->
                PlayerView(context).apply {
                    useController = false
                    player = playerEngine.getPlayer()
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // Indicador de Buffering
        if (uiState.isBuffering) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
                modifier = Modifier
                    .size(36.dp)
                    .align(Alignment.Center)
            )
        }

        // Overlay de Controles
        AnimatedVisibility(
            visible = showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Topo: Título e Botão Fechar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = uiState.contentTitle,
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(
                        onClick = onClosePlayer,
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Player",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                // Centro: Replay 10s, Play/Pause, Forward 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(20.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { playerEngine.seekTo(uiState.currentPositionMs - 10000) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Voltar 10s",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (uiState.isPlaying) playerEngine.pause() else playerEngine.play()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pausar" else "Reproduzir",
                            tint = Color.White,
                            modifier = Modifier.size(40.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerEngine.seekTo(uiState.currentPositionMs + 10000) },
                        modifier = Modifier.size(38.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Avançar 10s",
                            tint = Color.White,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Rodapé: Mudo, PiP e Botão de Tela Cheia
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                        .align(Alignment.BottomCenter),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Botão Mudo / Desmudo
                    IconButton(
                        onClick = { playerEngine.toggleMute() },
                        modifier = Modifier.size(34.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (uiState.isMuted) "Ativar Som" else "Mudo",
                            tint = if (uiState.isMuted) Color(0xFFFF5252) else Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Botão PiP
                        IconButton(
                            onClick = onEnterPip,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.PictureInPictureAlt,
                                contentDescription = "PiP",
                                tint = Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(4.dp))

                        // Botão Tela Cheia
                        IconButton(
                            onClick = onExpandFullscreen,
                            modifier = Modifier.size(34.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Fullscreen,
                                contentDescription = "Tela Cheia",
                                tint = Color.White,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }

        // Barra de progresso vermelha estilo YouTube no rodapé do player compacto
        if (!uiState.isLive && uiState.durationMs > 0) {
            val progressFraction = (uiState.currentPositionMs.toFloat() / uiState.durationMs.toFloat()).coerceIn(0f, 1f)
            YouTubeProgressBar(
                progressFraction = progressFraction,
                modifier = Modifier.align(Alignment.BottomCenter)
            )
        }
    }
}
