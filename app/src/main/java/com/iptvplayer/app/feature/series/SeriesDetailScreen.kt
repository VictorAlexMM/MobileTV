package com.iptvplayer.app.feature.series

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.WatchProgressEntity
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.components.YouTubeProgressBar
import com.iptvplayer.app.core.designsystem.theme.DarkBackground
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.downloads.DownloadController
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesDetailScreen(
    seriesDetails: SeriesDetails,
    database: AppDatabase,
    downloadController: DownloadController,
    onBackClick: () -> Unit,
    onPlayEpisode: (ChannelEntity, isDownloaded: Boolean) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val scope = rememberCoroutineScope()
    var selectedSeasonIndex by remember { mutableIntStateOf(0) }

    // Observa todos os progressos salvos para renderizar a barra do YouTube e o status "Visto"
    val allProgress by database.watchProgressDao().getAllProgress().collectAsState(initial = emptyList())
    val progressMap = remember(allProgress) { allProgress.associateBy { it.contentId } }

    val currentSeason = seriesDetails.seasons.getOrNull(selectedSeasonIndex)
        ?: seriesDetails.seasons.firstOrNull()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Barra Superior com Voltar e Título
        TopAppBar(
            title = {
                Text(
                    text = seriesDetails.seriesTitle,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header: Pôster, Gênero e Biografia / Sinopse
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(width = 110.dp, height = 160.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(DarkSurfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        if (!seriesDetails.posterUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = seriesDetails.posterUrl,
                                contentDescription = seriesDetails.seriesTitle,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.matchParentSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Tv,
                                contentDescription = null,
                                tint = TextMutedDark,
                                modifier = Modifier.size(40.dp)
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = seriesDetails.seriesTitle,
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.2f))
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = seriesDetails.genre,
                                color = PrimaryIndigo,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Text(
                            text = "${seriesDetails.seasons.size} Temporada(s) disponíveis",
                            color = TextMutedDark,
                            fontSize = 12.sp
                        )
                    }
                }
            }

            // Biografia & Sinopse
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurface)
                        .padding(14.dp)
                ) {
                    Text(
                        text = "Sinopse & Biografia",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = seriesDetails.plot,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMutedDark,
                        lineHeight = 20.sp
                    )
                }
            }

            // Seletor de Temporadas (Abas horizontais)
            if (seriesDetails.seasons.isNotEmpty()) {
                item {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Temporadas",
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                color = Color.White
                            )

                            // Botão Baixar Temporada Inteira
                            if (currentSeason != null) {
                                Button(
                                    onClick = {
                                        scope.launch {
                                            try {
                                                val channels = currentSeason.episodes.map { it.channel }
                                                val count = downloadController.startBatchDownload(channels)
                                                snackbarHostState.showSnackbar("Baixando $count episódios da ${currentSeason.seasonName}!")
                                            } catch (e: Exception) {
                                                snackbarHostState.showSnackbar("Erro ao baixar: ${e.localizedMessage}")
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                    shape = RoundedCornerShape(8.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Download,
                                        contentDescription = null,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Baixar Temporada", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                }
                            }
                        }

                        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            items(seriesDetails.seasons.indices.toList()) { index ->
                                val season = seriesDetails.seasons[index]
                                val isSelected = index == selectedSeasonIndex
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { selectedSeasonIndex = index },
                                    label = { Text(season.seasonName) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = PrimaryIndigo,
                                        selectedLabelColor = Color.White,
                                        containerColor = DarkSurface,
                                        labelColor = TextMutedDark
                                    ),
                                    shape = RoundedCornerShape(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            // Lista de Episódios da Temporada Selecionada
            if (currentSeason != null) {
                items(currentSeason.episodes, key = { it.channel.id }) { episode ->
                    val watchProgress = progressMap[episode.channel.id]
                    val progressFraction = if (watchProgress != null && watchProgress.durationMs > 0) {
                        (watchProgress.positionMs.toFloat() / watchProgress.durationMs.toFloat()).coerceIn(0f, 1f)
                    } else 0f
                    val isWatched = progressFraction >= 0.90f

                    val downloadEntity by database.downloadDao().observeDownloadByChannelId(episode.channel.id).collectAsState(initial = null)
                    val isDownloaded = downloadEntity?.status == DownloadStatus.COMPLETED

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onPlayEpisode(episode.channel, isDownloaded) },
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Thumbnail com Barra de Progresso Vermelha do YouTube
                            Box(
                                modifier = Modifier
                                    .size(width = 110.dp, height = 66.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant),
                                contentAlignment = Alignment.Center
                            ) {
                                if (!episode.channel.logoUrl.isNullOrBlank()) {
                                    AsyncImage(
                                        model = episode.channel.logoUrl,
                                        contentDescription = episode.episodeTitle,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.matchParentSize()
                                    )
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = null,
                                        tint = Color.White.copy(alpha = 0.7f),
                                        modifier = Modifier.size(28.dp)
                                    )
                                }

                                // Badge de Visto no topo
                                if (isWatched) {
                                    Box(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(4.dp)
                                            .clip(RoundedCornerShape(4.dp))
                                            .background(StatusDownloadedGreen)
                                            .padding(horizontal = 4.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            text = "Visto",
                                            color = Color.Black,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }

                                // Barra de Progresso Idêntica à do YouTube no rodapé da thumbnail
                                if (progressFraction > 0f) {
                                    YouTubeProgressBar(
                                        progressFraction = progressFraction,
                                        modifier = Modifier.align(Alignment.BottomCenter)
                                    )
                                }
                            }

                            // Detalhes do Episódio
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(
                                    text = episode.episodeTitle,
                                    style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                    color = Color.White,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Text(
                                    text = "Episódio ${episode.episodeNumber}" + if (isWatched) " • Assistido" else "",
                                    color = if (isWatched) StatusDownloadedGreen else TextMutedDark,
                                    fontSize = 12.sp
                                )
                            }

                            // Ação: Marcar como Visto (Toggle)
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        if (isWatched) {
                                            database.watchProgressDao().deleteProgress(episode.channel.id)
                                            snackbarHostState.showSnackbar("Episódio desmarcado.")
                                        } else {
                                            database.watchProgressDao().saveProgress(
                                                WatchProgressEntity(
                                                    contentId = episode.channel.id,
                                                    positionMs = 1000L,
                                                    durationMs = 1000L,
                                                    updatedAt = System.currentTimeMillis()
                                                )
                                            )
                                            snackbarHostState.showSnackbar("Marcado como visto!")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = if (isWatched) Icons.Filled.CheckCircle else Icons.Outlined.CheckCircle,
                                    contentDescription = "Marcar como visto",
                                    tint = if (isWatched) StatusDownloadedGreen else TextMutedDark,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            // Ação: Download
                            IconButton(
                                onClick = {
                                    scope.launch {
                                        try {
                                            downloadController.startDownload(
                                                contentId = episode.channel.id,
                                                title = "${seriesDetails.seriesTitle} - ${episode.episodeTitle}",
                                                sourceUrl = episode.channel.streamUrl,
                                                posterUrl = episode.channel.logoUrl
                                            )
                                            snackbarHostState.showSnackbar("Baixando: ${episode.episodeTitle}")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Erro: ${e.localizedMessage}")
                                        }
                                    }
                                }
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Download,
                                    contentDescription = "Baixar episódio",
                                    tint = if (isDownloaded) StatusDownloadedGreen else Color.White,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
