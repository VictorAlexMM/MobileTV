package com.iptvplayer.app.feature.vod

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.WatchProgressEntity
import com.iptvplayer.app.core.designsystem.components.YouTubeProgressBar
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.player.ExternalPlayerHelper

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MovieDetailsSheet(
    movie: ChannelEntity,
    watchProgress: WatchProgressEntity?,
    isDownloaded: Boolean,
    onDismiss: () -> Unit,
    onPlayClick: () -> Unit,
    onDownloadClick: () -> Unit,
    onFavoriteToggle: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val details = remember(movie) { MovieMetadataHelper.extract(movie) }
    val context = LocalContext.current

    val progressFraction = if (watchProgress != null && watchProgress.durationMs > 0) {
        (watchProgress.positionMs.toFloat() / watchProgress.durationMs.toFloat()).coerceIn(0f, 1f)
    } else 0f

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkSurface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp)
                .padding(bottom = 28.dp)
                .verticalScroll(rememberScrollState())
        ) {
            // Cabeçalho com Pôster e Informações Rápidas
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Pôster com YouTubeProgressBar embutida
                Box(
                    modifier = Modifier
                        .size(width = 110.dp, height = 160.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkSurfaceVariant),
                    contentAlignment = Alignment.Center
                ) {
                    if (!details.posterUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = details.posterUrl,
                            contentDescription = details.cleanTitle,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.matchParentSize()
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Movie,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(42.dp)
                        )
                    }

                    // Barra vermelha do YouTube no rodapé do pôster
                    if (progressFraction > 0f) {
                        YouTubeProgressBar(
                            progressFraction = progressFraction,
                            modifier = Modifier.align(Alignment.BottomCenter)
                        )
                    }
                }

                // Informações Básicas
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = details.cleanTitle,
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )

                    // Estilo / Gênero com Badge
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(PrimaryIndigo.copy(alpha = 0.2f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = details.genre,
                            color = PrimaryIndigo,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    // Ano de Lançamento e Avaliação
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.CalendarToday,
                                contentDescription = null,
                                tint = TextMutedDark,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = details.releaseYear,
                                color = TextMutedDark,
                                fontSize = 13.sp
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Star,
                                contentDescription = null,
                                tint = StatusWarningAmber,
                                modifier = Modifier.size(15.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = details.rating,
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Botão Favorito
                    IconButton(
                        onClick = onFavoriteToggle,
                        modifier = Modifier.size(36.dp)
                    ) {
                        Icon(
                            imageVector = if (movie.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Favoritar",
                            tint = if (movie.isFavorite) Color.Red else Color.White
                        )
                    }
                }
            }

            // Progresso do YouTube com indicação textual
            if (progressFraction > 0f) {
                Spacer(modifier = Modifier.height(14.dp))
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceVariant)
                        .padding(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Continuar assistindo",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color.White
                        )
                        Text(
                            text = "${(progressFraction * 100).toInt()}% concluído",
                            fontSize = 12.sp,
                            color = Color(0xFFFF0000),
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    YouTubeProgressBar(progressFraction = progressFraction)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Seção de Biografia / Sinopse
            Text(
                text = "Sinopse & Biografia",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = Color.White
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = details.plot,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMutedDark,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Botões de Ação
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = onPlayClick,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (progressFraction > 0f) "Continuar" else "Assistir",
                        fontWeight = FontWeight.Bold
                    )
                }

                OutlinedButton(
                    onClick = onDownloadClick,
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = if (isDownloaded) "Baixado" else "Baixar")
                }

                IconButton(
                    onClick = {
                        ExternalPlayerHelper.launchExternalPlayer(
                            context = context,
                            streamUrl = movie.streamUrl,
                            title = details.cleanTitle
                        )
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(DarkSurfaceVariant)
                ) {
                    Icon(
                        imageVector = Icons.Default.OpenInNew,
                        contentDescription = "Player Externo",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
