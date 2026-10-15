package com.iptvplayer.app.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Tv
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark

@Composable
fun ChannelCard(
    channel: ChannelEntity,
    isOfflineMode: Boolean,
    downloadStatus: DownloadStatus?,
    onCardClick: () -> Unit,
    onFavoriteToggle: () -> Unit,
    onDownloadClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDownloaded = downloadStatus == DownloadStatus.COMPLETED
    val isBlockedOffline = isOfflineMode && !isDownloaded && channel.contentType == ContentType.LIVE
    val alpha = if (isOfflineMode && !isDownloaded) 0.55f else 1f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onCardClick),
        colors = CardDefaults.cardColors(
            containerColor = DarkSurfaceVariant.copy(alpha = alpha)
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Logo / Ícone
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.Black.copy(alpha = 0.35f)),
                contentAlignment = Alignment.Center
            ) {
                if (!channel.logoUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = channel.logoUrl,
                        contentDescription = channel.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.size(46.dp)
                    )
                } else {
                    Icon(
                        imageVector = when (channel.contentType) {
                            ContentType.LIVE -> Icons.Default.Tv
                            ContentType.MOVIE -> Icons.Default.PlayArrow
                            ContentType.SERIES, ContentType.EPISODE -> Icons.Default.Tv
                        },
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(26.dp)
                    )
                }

                if (isBlockedOffline) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .background(Color.Black.copy(alpha = 0.6f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = "Bloqueado offline",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            // Informações do Canal
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    ),
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    // Badge de Tipo de Conteúdo
                    val (badgeText, badgeColor) = when (channel.contentType) {
                        ContentType.LIVE -> Pair("AO VIVO", Color(0xFFEF4444))
                        ContentType.MOVIE -> Pair("FILME", PrimaryIndigo)
                        ContentType.SERIES, ContentType.EPISODE -> Pair("SÉRIE", Color(0xFF8B5CF6))
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(badgeColor.copy(alpha = 0.15f))
                            .padding(horizontal = 5.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = badgeColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    channel.groupTitle?.let { group ->
                        Text(
                            text = group,
                            style = MaterialTheme.typography.bodySmall.copy(
                                color = TextMutedDark,
                                fontSize = 11.sp
                            ),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    if (downloadStatus != null) {
                        DownloadBadge(status = downloadStatus)
                    }
                }
            }

            // Ações: Download (para VOD/séries) e Favorito
            Row(verticalAlignment = Alignment.CenterVertically) {
                if (channel.contentType != ContentType.LIVE && !isDownloaded) {
                    IconButton(onClick = onDownloadClick) {
                        Icon(
                            imageVector = Icons.Default.Download,
                            contentDescription = "Baixar",
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                IconButton(onClick = onFavoriteToggle) {
                    Icon(
                        imageVector = if (channel.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Favoritar",
                        tint = if (channel.isFavorite) Color.Red else TextMutedDark,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}
