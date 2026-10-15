package com.iptvplayer.app.feature.player

import android.app.Activity
import android.content.pm.ActivityInfo
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.activity.compose.BackHandler
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureInPictureAlt
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.util.UnstableApi
import androidx.media3.ui.PlayerView
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import kotlinx.coroutines.delay

@OptIn(UnstableApi::class)
@Composable
fun PlayerScreen(
    playerEngine: PlayerEngine,
    onBackClick: () -> Unit,
    onClosePlayer: () -> Unit = onBackClick,
    isInPipMode: Boolean = false,
    onEnterPip: () -> Unit = {},
    onStartRecording: ((channelId: String, title: String, streamUrl: String, durationMinutes: Int) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val uiState by playerEngine.uiState.collectAsState()
    var showControls by remember { mutableStateOf(true) }
    var showRecordDialog by remember { mutableStateOf(false) }
    var selectedDurationMinutes by remember { mutableIntStateOf(30) }
    val playerFocusRequester = remember { FocusRequester() }

    // Foco automático para captura de comandos do controle remoto (Smart TV / D-Pad)
    LaunchedEffect(Unit) {
        playerFocusRequester.requestFocus()
    }

    // Esconde os controles automaticamente após 4 segundos
    LaunchedEffect(showControls, uiState.isPlaying) {
        if (showControls && uiState.isPlaying) {
            delay(4000)
            showControls = false
        }
    }

    val activity = context as? Activity
    DisposableEffect(isInPipMode) {
        if (!isInPipMode) {
            val originalOrientation = activity?.requestedOrientation ?: ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
            activity?.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            onDispose {
                activity?.requestedOrientation = originalOrientation
            }
        } else {
            onDispose { }
        }
    }

    // Intercepta o botão de voltar do celular para sair do fullscreen sem fechar o app
    BackHandler(enabled = !isInPipMode) {
        onBackClick()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .focusRequester(playerFocusRequester)
            .focusable()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyDown) {
                    when (keyEvent.key) {
                        Key.DirectionCenter,
                        Key.Enter,
                        Key.MediaPlayPause -> {
                            if (uiState.isPlaying) playerEngine.pause() else playerEngine.play()
                            showControls = true
                            true
                        }
                        Key.MediaPlay -> {
                            playerEngine.play()
                            showControls = true
                            true
                        }
                        Key.MediaPause -> {
                            playerEngine.pause()
                            showControls = true
                            true
                        }
                        Key.DirectionRight,
                        Key.MediaFastForward -> {
                            playerEngine.seekTo(uiState.currentPositionMs + 10000)
                            showControls = true
                            true
                        }
                        Key.DirectionLeft,
                        Key.MediaRewind -> {
                            playerEngine.seekTo(uiState.currentPositionMs - 10000)
                            showControls = true
                            true
                        }
                        Key.DirectionUp,
                        Key.DirectionDown -> {
                            showControls = !showControls
                            true
                        }
                        else -> false
                    }
                } else false
            }
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                showControls = !showControls
            }
    ) {
        // Player Surface Media3
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

        // Overlay de Buffering
        if (uiState.isBuffering) {
            CircularProgressIndicator(
                color = PrimaryIndigo,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Controles Overlay Animados (ocultos em modo PiP)
        AnimatedVisibility(
            visible = !isInPipMode && showControls,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.55f))
            ) {
                // Barra Superior: Voltar, Título, Badge Offline, PiP e Player Externo
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .align(Alignment.TopStart),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Voltar",
                            tint = Color.White
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = uiState.contentTitle,
                        style = MaterialTheme.typography.titleMedium.copy(
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )

                    if (uiState.isOfflinePlayback) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusDownloadedGreen.copy(alpha = 0.25f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CloudOff,
                                    contentDescription = null,
                                    tint = StatusDownloadedGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Offline",
                                    color = StatusDownloadedGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                    }

                    // Botão Mudo / Desmudo
                    IconButton(onClick = { playerEngine.toggleMute() }) {
                        Icon(
                            imageVector = if (uiState.isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                            contentDescription = if (uiState.isMuted) "Ativar Som" else "Mudo",
                            tint = if (uiState.isMuted) Color(0xFFFF5252) else Color.White
                        )
                    }

                    // Botão Picture-in-Picture
                    IconButton(onClick = onEnterPip) {
                        Icon(
                            imageVector = Icons.Default.PictureInPictureAlt,
                            contentDescription = "Modo Picture-in-Picture",
                            tint = Color.White
                        )
                    }

                    // Botão Gravação de Canal ao Vivo
                    if (uiState.isLive && onStartRecording != null && uiState.streamUrl.isNotBlank()) {
                        IconButton(onClick = { showRecordDialog = true }) {
                            Icon(
                                imageVector = Icons.Default.Videocam,
                                contentDescription = "Gravar Transmissão",
                                tint = Color.Red
                            )
                        }
                    }

                    // Botão Player Externo (VLC / MX Player)
                    if (uiState.streamUrl.isNotBlank()) {
                        IconButton(onClick = {
                            ExternalPlayerHelper.launchExternalPlayer(
                                context = context,
                                streamUrl = uiState.streamUrl,
                                title = uiState.contentTitle
                            )
                        }) {
                            Icon(
                                imageVector = Icons.Default.OpenInNew,
                                contentDescription = "Player Externo",
                                tint = Color.White
                            )
                        }
                    }

                    // Botão Sair da Tela Cheia (Retornar ao player compacto / página anterior)
                    IconButton(onClick = onBackClick) {
                        Icon(
                            imageVector = Icons.Default.FullscreenExit,
                            contentDescription = "Reduzir Reprodutor",
                            tint = Color.White
                        )
                    }

                    // Botão Fechar Reprodutor (Encerra a reprodução)
                    IconButton(onClick = onClosePlayer) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Fechar Reprodutor",
                            tint = Color.White
                        )
                    }
                }

                // Controles Centrais: Replay 10s, Play/Pause, Forward 10s
                Row(
                    modifier = Modifier.align(Alignment.Center),
                    horizontalArrangement = Arrangement.spacedBy(28.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = { playerEngine.seekTo(uiState.currentPositionMs - 10000) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Replay10,
                            contentDescription = "Voltar 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    IconButton(
                        onClick = {
                            if (uiState.isPlaying) playerEngine.pause() else playerEngine.play()
                        },
                        modifier = Modifier.size(64.dp)
                    ) {
                        Icon(
                            imageVector = if (uiState.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (uiState.isPlaying) "Pausar" else "Reproduzir",
                            tint = Color.White,
                            modifier = Modifier.size(54.dp)
                        )
                    }

                    IconButton(
                        onClick = { playerEngine.seekTo(uiState.currentPositionMs + 10000) },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Forward10,
                            contentDescription = "Avançar 10s",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }
                }

                // Barra Inferior: Seekbar e Tempo
                if (!uiState.isLive) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                            .align(Alignment.BottomCenter)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = formatTime(uiState.currentPositionMs),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                            Text(
                                text = formatTime(uiState.durationMs),
                                color = Color.White,
                                fontSize = 12.sp
                            )
                        }

                        val duration = if (uiState.durationMs > 0) uiState.durationMs.toFloat() else 1f
                        val position = uiState.currentPositionMs.toFloat().coerceIn(0f, duration)

                        Slider(
                            value = position,
                            onValueChange = { newPosition ->
                                playerEngine.seekTo(newPosition.toLong())
                            },
                            valueRange = 0f..duration,
                            colors = SliderDefaults.colors(
                                thumbColor = PrimaryIndigo,
                                activeTrackColor = PrimaryIndigo,
                                inactiveTrackColor = Color.White.copy(alpha = 0.3f)
                            ),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        }
    }

    if (showRecordDialog && onStartRecording != null) {
        AlertDialog(
            onDismissRequest = { showRecordDialog = false },
            title = { Text("Gravar Canal ao Vivo", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text("Selecione a duração da gravação para ${uiState.contentTitle}:")
                    Spacer(modifier = Modifier.height(12.dp))
                    listOf(15, 30, 60, 120).forEach { minutes ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { selectedDurationMinutes = minutes }
                                .padding(vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            RadioButton(
                                selected = selectedDurationMinutes == minutes,
                                onClick = { selectedDurationMinutes = minutes }
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("$minutes minutos")
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onStartRecording(
                            uiState.contentId,
                            uiState.contentTitle,
                            uiState.streamUrl,
                            selectedDurationMinutes
                        )
                        showRecordDialog = false
                    }
                ) {
                    Text("Iniciar Gravação")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRecordDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val hours = minutes / 60
    return if (hours > 0) {
        "%d:%02d:%02d".format(hours, minutes % 60, seconds)
    } else {
        "%02d:%02d".format(minutes, seconds)
    }
}
