package com.iptvplayer.app.feature.downloads

import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DownloadDone
import androidx.compose.material.icons.filled.FolderZip
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
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
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.database.entity.RecordingEntity
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.database.model.RecordingStatus
import com.iptvplayer.app.core.designsystem.components.DownloadBadge
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.recordings.RecordingScheduler
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch

@Composable
fun DownloadsScreen(
    database: AppDatabase,
    downloadController: DownloadController,
    onPlayOfflineItem: (DownloadEntity) -> Unit,
    onPlayRecording: (filePath: String, title: String) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableIntStateOf(0) }
    val allDownloads by database.downloadDao().getAllDownloads().collectAsState(initial = emptyList())
    val recordings by database.recordingDao().getAllRecordings().collectAsState(initial = emptyList())

    val inProgress = allDownloads.filter { it.status != DownloadStatus.COMPLETED }
    val completed = allDownloads.filter { it.status == DownloadStatus.COMPLETED }

    Column(modifier = modifier.fillMaxSize()) {
        TabRow(selectedTabIndex = selectedTab) {
            Tab(
                selected = selectedTab == 0,
                onClick = { selectedTab = 0 },
                text = { Text("Concluídos (${completed.size})") }
            )
            Tab(
                selected = selectedTab == 1,
                onClick = { selectedTab = 1 },
                text = { Text("Em Fila (${inProgress.size})") }
            )
            Tab(
                selected = selectedTab == 2,
                onClick = { selectedTab = 2 },
                text = { Text("Gravações (${recordings.size})") }
            )
        }

        when (selectedTab) {
            0 -> CompletedDownloadsList(
                downloads = completed,
                onPlay = onPlayOfflineItem,
                onDelete = { downloadController.removeDownload(it.id) }
            )
            1 -> InProgressDownloadsList(
                downloads = inProgress,
                onPause = { downloadController.pauseDownload(it.id) },
                onResume = { downloadController.resumeDownload(it.id) },
                onCancel = { downloadController.removeDownload(it.id) }
            )
            2 -> RecordingsList(
                recordings = recordings,
                onPlay = onPlayRecording,
                onDelete = { rec ->
                    scope.launch {
                        RecordingScheduler.deleteRecording(context, database, rec.id, rec.filePath)
                    }
                },
                onCancel = { rec ->
                    scope.launch {
                        RecordingScheduler.cancelRecording(context, database, rec.id)
                    }
                }
            )
        }
    }
}

@Composable
private fun CompletedDownloadsList(
    downloads: List<DownloadEntity>,
    onPlay: (DownloadEntity) -> Unit,
    onDelete: (DownloadEntity) -> Unit
) {
    if (downloads.isEmpty()) {
        EmptyState(
            icon = Icons.Default.DownloadDone,
            title = "Nenhum download concluído",
            subtitle = "Filmes e episódios baixados aparecerão aqui para reprodução 100% offline."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(downloads, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color.Black.copy(alpha = 0.3f)),
                            contentAlignment = Alignment.Center
                        ) {
                            if (!item.posterUrl.isNullOrBlank()) {
                                AsyncImage(
                                    model = item.posterUrl,
                                    contentDescription = item.title,
                                    contentScale = ContentScale.Fit,
                                    modifier = Modifier.size(46.dp)
                                )
                            } else {
                                Icon(
                                    imageVector = Icons.Default.FolderZip,
                                    contentDescription = null,
                                    tint = StatusDownloadedGreen,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                DownloadBadge(status = item.status)
                                val mb = if (item.bytesDownloaded > 0) item.bytesDownloaded / (1024 * 1024) else 0L
                                Text(
                                    text = "$mb MB baixados",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = { onPlay(item) }) {
                                Icon(
                                    imageVector = Icons.Default.PlayArrow,
                                    contentDescription = "Assistir Offline",
                                    tint = StatusDownloadedGreen,
                                    modifier = Modifier.size(28.dp)
                                )
                            }
                            IconButton(onClick = { onDelete(item) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Excluir",
                                    tint = Color.Red.copy(alpha = 0.8f),
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

@Composable
private fun InProgressDownloadsList(
    downloads: List<DownloadEntity>,
    onPause: (DownloadEntity) -> Unit,
    onResume: (DownloadEntity) -> Unit,
    onCancel: (DownloadEntity) -> Unit
) {
    if (downloads.isEmpty()) {
        EmptyState(
            icon = Icons.Default.FolderZip,
            title = "Fila de downloads vazia",
            subtitle = "Não há downloads em andamento no momento."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(downloads, key = { it.id }) { item ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = item.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                modifier = Modifier.weight(1f)
                            )
                            DownloadBadge(status = item.status, percent = item.percent)
                        }

                        LinearProgressIndicator(
                            progress = { if (item.percent > 0) item.percent / 100f else 0f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = PrimaryIndigo,
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            val downloadedMb = item.bytesDownloaded / (1024 * 1024)
                            Text(
                                text = "$downloadedMb MB",
                                style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark)
                            )

                            Row {
                                if (item.status == DownloadStatus.PAUSED) {
                                    IconButton(onClick = { onResume(item) }) {
                                        Icon(Icons.Default.PlayArrow, contentDescription = "Retomar", tint = PrimaryIndigo)
                                    }
                                } else {
                                    IconButton(onClick = { onPause(item) }) {
                                        Icon(Icons.Default.Pause, contentDescription = "Pausar", tint = PrimaryIndigo)
                                    }
                                }
                                IconButton(onClick = { onCancel(item) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Cancelar", tint = Color.Red.copy(alpha = 0.8f))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun RecordingsList(
    recordings: List<RecordingEntity>,
    onPlay: (filePath: String, title: String) -> Unit,
    onDelete: (RecordingEntity) -> Unit,
    onCancel: (RecordingEntity) -> Unit
) {
    if (recordings.isEmpty()) {
        EmptyState(
            icon = Icons.Default.Videocam,
            title = "Nenhuma gravação",
            subtitle = "Gravações imediatas ou agendadas pelo EPG aparecerão nesta aba."
        )
    } else {
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(recordings, key = { it.id }) { rec ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = rec.title,
                                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                val (statusColor, statusLabel) = when (rec.status) {
                                    RecordingStatus.RECORDING -> Pair(Color.Red, "Gravando...")
                                    RecordingStatus.SCHEDULED -> Pair(StatusWarningAmber, "Agendado")
                                    RecordingStatus.DONE -> Pair(StatusDownloadedGreen, "Gravado")
                                    RecordingStatus.FAILED -> Pair(Color.Red, "Falhou")
                                }

                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(statusColor.copy(alpha = 0.2f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = statusLabel,
                                        color = statusColor,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }

                                Text(
                                    text = "${rec.durationMinutes} min",
                                    style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark)
                                )
                            }
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            if (rec.status == RecordingStatus.DONE && !rec.filePath.isNullOrBlank()) {
                                IconButton(onClick = { onPlay(rec.filePath, rec.title) }) {
                                    Icon(
                                        imageVector = Icons.Default.PlayArrow,
                                        contentDescription = "Reproduzir",
                                        tint = PrimaryIndigo
                                    )
                                }
                            }

                            if (rec.status == RecordingStatus.RECORDING || rec.status == RecordingStatus.SCHEDULED) {
                                IconButton(onClick = { onCancel(rec) }) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Cancelar Gravação",
                                        tint = StatusWarningAmber
                                    )
                                }
                            } else {
                                IconButton(onClick = { onDelete(rec) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Excluir Gravação",
                                        tint = TextMutedDark
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

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    subtitle: String
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = TextMutedDark,
            modifier = Modifier.size(56.dp)
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}
