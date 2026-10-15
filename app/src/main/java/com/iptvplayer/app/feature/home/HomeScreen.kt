package com.iptvplayer.app.feature.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.ui.draw.rotate
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.designsystem.components.YouTubeProgressBar
import com.iptvplayer.app.core.designsystem.theme.DarkBackground
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.live.ChannelRepository
import com.iptvplayer.app.feature.playlists.PlaylistRepository
import com.iptvplayer.app.ui.AppDestination
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ContinueWatchingItem(
    val contentId: String,
    val title: String,
    val subtitle: String,
    val posterUrl: String?,
    val streamUrl: String,
    val positionMs: Long,
    val durationMs: Long,
    val progressFraction: Float,
    val contentType: ContentType,
    val isDownloaded: Boolean
)

@Composable
fun HomeScreen(
    database: AppDatabase,
    channelRepository: ChannelRepository,
    downloadController: DownloadController,
    activePlaylistId: Long?,
    isOfflineMode: Boolean,
    onNavigateTo: (AppDestination) -> Unit,
    onPlayItem: (id: String, title: String, streamUrl: String, isDownloaded: Boolean, startPositionMs: Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    playlistRepository: PlaylistRepository? = null,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()

    val isSyncing by (playlistRepository?.isSyncing ?: kotlinx.coroutines.flow.MutableStateFlow(false)).collectAsState()
    val syncStatus by (playlistRepository?.syncStatus ?: kotlinx.coroutines.flow.MutableStateFlow(null)).collectAsState()

    val infiniteTransition = rememberInfiniteTransition(label = "SyncRotate")
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = androidx.compose.animation.core.LinearEasing)
        ),
        label = "Rotation"
    )

    // 1. Monitora o progresso de visualização salvo no Room
    val recentlyWatched by database.watchProgressDao().getRecentlyWatched(30).collectAsState(initial = emptyList())

    // 2. Resolve itens de "Continuar Assistindo" em background
    val continueWatchingList by produceState<List<ContinueWatchingItem>>(initialValue = emptyList(), key1 = recentlyWatched) {
        val items = mutableListOf<ContinueWatchingItem>()
        for (progress in recentlyWatched) {
            // Ignora se assistiu menos de 5 segundos ou se já concluiu mais de 98% de um vídeo com duração conhecida
            if (progress.positionMs < 5_000L) continue
            if (progress.durationMs > 0 && progress.positionMs >= (progress.durationMs - 15_000L)) continue

            val channel = withContext(Dispatchers.IO) {
                database.channelDao().getChannelById(progress.contentId)
            }
            if (channel != null) {
                val progressFraction = if (progress.durationMs > 0) {
                    (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                } else 0f

                val subtitle = when (channel.contentType) {
                    ContentType.SERIES -> "Série • ${channel.groupTitle ?: "Episódio"}"
                    ContentType.EPISODE -> "Episódio • ${channel.groupTitle ?: "Série"}"
                    ContentType.MOVIE -> "Filme • ${channel.groupTitle ?: "VOD"}"
                    ContentType.LIVE -> "Canal • ${channel.groupTitle ?: "Ao Vivo"}"
                }

                items.add(
                    ContinueWatchingItem(
                        contentId = channel.id,
                        title = channel.name,
                        subtitle = subtitle,
                        posterUrl = channel.logoUrl,
                        streamUrl = channel.streamUrl,
                        positionMs = progress.positionMs,
                        durationMs = progress.durationMs,
                        progressFraction = progressFraction,
                        contentType = channel.contentType,
                        isDownloaded = false
                    )
                )
            } else {
                // Se não está na tabela de canais ativos, pode ser uma mídia baixada offline
                val download = withContext(Dispatchers.IO) {
                    database.downloadDao().getDownloadById(progress.contentId)
                }
                if (download != null) {
                    val progressFraction = if (progress.durationMs > 0) {
                        (progress.positionMs.toFloat() / progress.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f

                    items.add(
                        ContinueWatchingItem(
                            contentId = download.id,
                            title = download.title,
                            subtitle = "Mídia Baixada (Offline)",
                            posterUrl = download.posterUrl,
                            streamUrl = download.sourceUrl,
                            positionMs = progress.positionMs,
                            durationMs = progress.durationMs,
                            progressFraction = progressFraction,
                            contentType = ContentType.MOVIE,
                            isDownloaded = true
                        )
                    )
                }
            }
        }
        value = items
    }

    // 3. Informações da playlist ativa
    val activePlaylist by produceState<com.iptvplayer.app.core.database.entity.PlaylistEntity?>(initialValue = null, key1 = activePlaylistId) {
        if (activePlaylistId != null) {
            value = withContext(Dispatchers.IO) {
                database.playlistDao().getPlaylistById(activePlaylistId)
            }
        }
    }

    // 4. Canais favoritos para acesso rápido
    val favoriteChannels by database.channelDao().getFavoriteChannels().collectAsState(initial = emptyList())

    // 5. Mídias baixadas concluídas
    val completedDownloads by database.downloadDao().getCompletedDownloads().collectAsState(initial = emptyList())

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentPadding = PaddingValues(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        // Banner de Boas-Vindas e Status Minimalista
        item {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurface)
                    .border(1.dp, Color.White.copy(alpha = 0.06f), RoundedCornerShape(14.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Bem-vindo de volta",
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                            Text(
                                text = if (activePlaylist != null) {
                                    "Playlist: ${activePlaylist?.name}"
                                } else {
                                    "Nenhuma playlist ativa selecionada"
                                },
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark, fontSize = 11.sp)
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            // Botão de Sincronização Manual / Atualizar Lançamentos
                            if (activePlaylistId != null && !isOfflineMode) {
                                IconButton(
                                    onClick = {
                                        if (!isSyncing) {
                                            scope.launch(Dispatchers.IO) {
                                                playlistRepository?.syncPlaylist(activePlaylistId)
                                            }
                                        }
                                    },
                                    modifier = Modifier.size(32.dp),
                                    enabled = !isSyncing
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Sync,
                                        contentDescription = "Atualizar Catálogo",
                                        tint = if (isSyncing) PrimaryIndigo else Color.White.copy(alpha = 0.8f),
                                        modifier = Modifier
                                            .size(18.dp)
                                            .then(if (isSyncing) Modifier.rotate(rotationAngle) else Modifier)
                                    )
                                }
                                Spacer(modifier = Modifier.width(4.dp))
                            }

                            // Status Chip
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (isOfflineMode) StatusWarningAmber.copy(alpha = 0.15f)
                                        else StatusDownloadedGreen.copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 7.dp, vertical = 3.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = if (isOfflineMode) Icons.Default.CloudOff else Icons.Default.Cloud,
                                        contentDescription = null,
                                        tint = if (isOfflineMode) StatusWarningAmber else StatusDownloadedGreen,
                                        modifier = Modifier.size(12.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = if (isOfflineMode) "Offline" else "Online",
                                        color = if (isOfflineMode) StatusWarningAmber else StatusDownloadedGreen,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                        }
                    }

                    // Indicador de Sincronização em Segundo Plano (Últimos lançamentos)
                    AnimatedVisibility(visible = isSyncing) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.2f))
                                .padding(horizontal = 10.dp, vertical = 5.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(12.dp),
                                color = PrimaryIndigo,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = syncStatus ?: "Atualizando catálogo com últimos lançamentos...",
                                fontSize = 11.sp,
                                color = Color.White
                            )
                        }
                    }
                }
            }
        }

        // Seção: Continuar Reproduzindo de Onde Parou (Minimalista e Organizado)
        if (continueWatchingList.isNotEmpty()) {
            item {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 2.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.PlayCircle,
                                contentDescription = null,
                                tint = Color(0xFFFF3333),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Continuar Assistindo",
                                style = MaterialTheme.typography.titleSmall.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = Color.White
                                )
                            )
                        }

                        Text(
                            text = "${continueWatchingList.size} em andamento",
                            fontSize = 11.sp,
                            color = TextMutedDark
                        )
                    }

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        continueWatchingList.take(3).forEach { item ->
                            MinimalContinueWatchingCard(
                                item = item,
                                onResumeClick = {
                                    onPlayItem(
                                        item.contentId,
                                        item.title,
                                        item.streamUrl,
                                        item.isDownloaded,
                                        item.positionMs
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Continuando ${item.title} de ${formatTime(item.positionMs)}")
                                    }
                                },
                                onRestartClick = {
                                    onPlayItem(
                                        item.contentId,
                                        item.title,
                                        item.streamUrl,
                                        item.isDownloaded,
                                        0L
                                    )
                                    scope.launch {
                                        snackbarHostState.showSnackbar("Reiniciando ${item.title} do começo")
                                    }
                                },
                                onRemoveClick = {
                                    scope.launch {
                                        database.watchProgressDao().deleteProgress(item.contentId)
                                        snackbarHostState.showSnackbar("${item.title} removido do histórico")
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Seção: Atalhos Rápidos para as Categorias Principais
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Acesso Rápido",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickCategoryCard(
                        title = "Canais",
                        subtitle = "Ao Vivo",
                        icon = Icons.Default.LiveTv,
                        tint = PrimaryIndigo,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(AppDestination.LIVE) }
                    )

                    QuickCategoryCard(
                        title = "Filmes",
                        subtitle = "Catálogo VOD",
                        icon = Icons.Default.Movie,
                        tint = Color(0xFF26A69A),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(AppDestination.MOVIES) }
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    QuickCategoryCard(
                        title = "Séries",
                        subtitle = "Episódios",
                        icon = Icons.Default.VideoLibrary,
                        tint = Color(0xFFAB47BC),
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(AppDestination.SERIES) }
                    )

                    QuickCategoryCard(
                        title = "Downloads",
                        subtitle = "100% Offline",
                        icon = Icons.Default.Download,
                        tint = StatusDownloadedGreen,
                        modifier = Modifier.weight(1f),
                        onClick = { onNavigateTo(AppDestination.DOWNLOADS) }
                    )
                }
            }
        }

        // Seção: Canais Favoritos (se houver)
        if (favoriteChannels.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = Color.Red,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Seus Canais Favoritos",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(favoriteChannels.take(15), key = { it.id }) { channel ->
                            Card(
                                modifier = Modifier
                                    .width(140.dp)
                                    .clickable {
                                        onPlayItem(channel.id, channel.name, channel.streamUrl, false, 0L)
                                    },
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(10.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(54.dp)
                                            .clip(CircleShape)
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!channel.logoUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = channel.logoUrl,
                                                contentDescription = channel.name,
                                                contentScale = ContentScale.Fit,
                                                modifier = Modifier.size(42.dp)
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.LiveTv,
                                                contentDescription = null,
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(24.dp)
                                            )
                                        }
                                    }
                                    Spacer(modifier = Modifier.height(8.dp))
                                    Text(
                                        text = channel.name,
                                        style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                        color = Color.White,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis,
                                        textAlign = TextAlign.Center
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Seção: Mídias Baixadas Prontas para Reprodução Offline
        if (completedDownloads.isNotEmpty()) {
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = null,
                            tint = StatusDownloadedGreen,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pronto para Assistir Offline",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(completedDownloads.take(10), key = { it.id }) { dl ->
                            Card(
                                modifier = Modifier
                                    .width(180.dp)
                                    .clickable {
                                        onPlayItem(dl.id, dl.title, dl.sourceUrl, true, 0L)
                                    },
                                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Column {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .aspectRatio(16f / 9f)
                                            .background(DarkSurfaceVariant),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        if (!dl.posterUrl.isNullOrBlank()) {
                                            AsyncImage(
                                                model = dl.posterUrl,
                                                contentDescription = dl.title,
                                                contentScale = ContentScale.Crop,
                                                modifier = Modifier.fillMaxSize()
                                            )
                                        } else {
                                            Icon(
                                                imageVector = Icons.Default.Movie,
                                                contentDescription = null,
                                                tint = StatusDownloadedGreen,
                                                modifier = Modifier.size(32.dp)
                                            )
                                        }
                                        Icon(
                                            imageVector = Icons.Default.PlayArrow,
                                            contentDescription = "Reproduzir",
                                            tint = Color.White,
                                            modifier = Modifier
                                                .size(32.dp)
                                                .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                                                .padding(4.dp)
                                        )
                                    }
                                    Column(modifier = Modifier.padding(8.dp)) {
                                        Text(
                                            text = dl.title,
                                            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Bold),
                                            color = Color.White,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis
                                        )
                                        Text(
                                            text = "Download Concluído",
                                            fontSize = 11.sp,
                                            color = StatusDownloadedGreen
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Player minimalista e compacto na tela inicial para continuar assistindo.
 * Altura compacta (~66dp), miniatura 16:9 com barra vermelha do YouTube,
 * tempo e botões diretos de Continuar (Play), Reiniciar e Apagar do histórico.
 */
@Composable
private fun MinimalContinueWatchingCard(
    item: ContinueWatchingItem,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onRemoveClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable { onResumeClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.06f)),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Miniatura 16:9 compacta com Barra de Progresso do YouTube
            Box(
                modifier = Modifier
                    .width(82.dp)
                    .height(48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(DarkSurfaceVariant)
            ) {
                if (!item.posterUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = item.posterUrl,
                        contentDescription = item.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = when (item.contentType) {
                                ContentType.SERIES -> Icons.Default.VideoLibrary
                                ContentType.LIVE -> Icons.Default.LiveTv
                                else -> Icons.Default.Movie
                            },
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // Overlay escuro suave
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.25f))
                )

                // Ícone play sutil no centro
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .align(Alignment.Center)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(12.dp)
                    )
                }

                // Barra Vermelha do YouTube na base da miniatura
                YouTubeProgressBar(
                    progressFraction = item.progressFraction,
                    modifier = Modifier.align(Alignment.BottomCenter)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            // 2. Informações de Título, Categoria e Progresso
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 8.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = item.subtitle,
                        style = MaterialTheme.typography.bodySmall.copy(
                            color = TextMutedDark,
                            fontSize = 11.sp
                        ),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    val timeText = if (item.durationMs > 0) {
                        "${formatTime(item.positionMs)} / ${formatTime(item.durationMs)}"
                    } else {
                        formatTime(item.positionMs)
                    }
                    Text(
                        text = timeText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            color = Color(0xFFFF5252),
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 10.sp
                        )
                    )
                }
            }

            // 3. Botões de Ação: Reiniciar do começo e Apagar do histórico
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.padding(start = 4.dp, end = 2.dp)
            ) {
                // Reiniciar do começo
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .clickable { onRestartClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Replay,
                        contentDescription = "Reiniciar do começo",
                        tint = Color.White.copy(alpha = 0.85f),
                        modifier = Modifier.size(16.dp)
                    )
                }

                // Apagar do histórico
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(DarkSurfaceVariant)
                        .clickable { onRemoveClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Apagar do histórico",
                        tint = TextMutedDark,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCategoryCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    tint: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable { onClick() },
        colors = CardDefaults.cardColors(containerColor = DarkSurface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(tint.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark),
                    fontSize = 11.sp
                )
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    val totalSeconds = (ms / 1000).coerceAtLeast(0)
    val seconds = totalSeconds % 60
    val minutes = (totalSeconds / 60) % 60
    val hours = totalSeconds / 3600
    return if (hours > 0) {
        String.format("%d:%02d:%02d", hours, minutes, seconds)
    } else {
        String.format("%02d:%02d", minutes, seconds)
    }
}
