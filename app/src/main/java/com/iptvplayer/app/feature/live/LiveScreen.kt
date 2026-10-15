package com.iptvplayer.app.feature.live

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.LiveTv
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.entity.EpgProgramEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.components.ChannelCard
import com.iptvplayer.app.core.designsystem.components.GoOnlineDialog
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.recordings.RecordingScheduler
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun LiveScreen(
    repository: ChannelRepository,
    activePlaylistId: Long?,
    isOfflineMode: Boolean,
    onPlayChannel: (ChannelEntity) -> Unit,
    onEnableOnlineMode: () -> Unit,
    database: AppDatabase? = null,
    snackbarHostState: SnackbarHostState? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showGoOnlineDialog by remember { mutableStateOf(false) }
    var pendingChannelToPlay by remember { mutableStateOf<ChannelEntity?>(null) }
    var selectedChannelForEpg by remember { mutableStateOf<ChannelEntity?>(null) }
    val scope = rememberCoroutineScope()

    val categories by if (activePlaylistId != null) {
        repository.getCategories(activePlaylistId, ContentType.LIVE).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }

    val channelsPaging = remember(activePlaylistId, selectedCategory, searchQuery) {
        repository.getChannelsFilteredPaged(
            playlistId = activePlaylistId,
            contentType = ContentType.LIVE,
            category = selectedCategory,
            query = searchQuery.trim()
        )
    }.collectAsLazyPagingItems()

    var showCategorySheet by remember { mutableStateOf(false) }

    Column(modifier = modifier.fillMaxSize()) {
        // Campo de busca
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = {
                    Text(
                        text = if (selectedCategory != null) "Buscar em \"$selectedCategory\"..." else "Buscar canais ao vivo...",
                        color = com.iptvplayer.app.core.designsystem.theme.TextMutedDark,
                        fontSize = 14.sp
                    )
                },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = com.iptvplayer.app.core.designsystem.theme.TextMutedDark) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Default.Clear, contentDescription = "Limpar busca", tint = com.iptvplayer.app.core.designsystem.theme.TextMutedDark)
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.weight(1f)
            )

            if (categories.isNotEmpty()) {
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.padding(start = 8.dp))
                IconButton(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory != null) PrimaryIndigo.copy(alpha = 0.25f) else com.iptvplayer.app.core.designsystem.theme.DarkSurface)
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Filtro de Categorias",
                        tint = if (selectedCategory != null) PrimaryIndigo else Color.White
                    )
                }
            }
        }

        // Lista de Canais Paginada
        Box(modifier = Modifier.fillMaxSize()) {
            if (activePlaylistId == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = androidx.compose.material.icons.Icons.Default.LiveTv,
                        contentDescription = null,
                        tint = com.iptvplayer.app.core.designsystem.theme.TextMutedDark,
                        modifier = Modifier.size(56.dp)
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma playlist ativa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Adicione ou selecione uma playlist na aba Playlists para carregar os canais.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = com.iptvplayer.app.core.designsystem.theme.TextMutedDark),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        count = channelsPaging.itemCount,
                        key = { index -> channelsPaging[index]?.id ?: index }
                    ) { index ->
                        val channel = channelsPaging[index]
                        if (channel != null) {
                            ChannelCard(
                                channel = channel,
                                isOfflineMode = isOfflineMode,
                                downloadStatus = null,
                                onCardClick = {
                                    if (isOfflineMode) {
                                        pendingChannelToPlay = channel
                                        showGoOnlineDialog = true
                                    } else {
                                        onPlayChannel(channel)
                                    }
                                },
                                onFavoriteToggle = {
                                    scope.launch {
                                        repository.toggleFavorite(channel.id, channel.isFavorite)
                                    }
                                },
                                onDownloadClick = {},
                                onScheduleClick = if (database != null) { { selectedChannelForEpg = channel } } else null
                            )
                        }
                    }

                    if (channelsPaging.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryIndigo)
                            }
                        }
                    }
                }

                if (channelsPaging.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (channelsPaging.itemCount == 0) {
                    Text(
                        text = if (searchQuery.isNotBlank()) "Nenhum canal encontrado para \"$searchQuery\"." else "Nenhum canal ao vivo encontrado nesta categoria.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = com.iptvplayer.app.core.designsystem.theme.TextMutedDark),
                        modifier = Modifier.align(Alignment.Center),
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        }
    }

    if (showCategorySheet) {
        val sheetState = androidx.compose.material3.rememberModalBottomSheetState()
        androidx.compose.material3.ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = com.iptvplayer.app.core.designsystem.theme.DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Categorias de Canais (${categories.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                androidx.compose.foundation.layout.Spacer(modifier = Modifier.height(16.dp))

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(350.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == null) PrimaryIndigo.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    selectedCategory = null
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Todos os Canais",
                                color = if (selectedCategory == null) PrimaryIndigo else Color.White,
                                fontWeight = if (selectedCategory == null) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    items(categories) { cat ->
                        val isSelected = selectedCategory == cat
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) PrimaryIndigo.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    selectedCategory = cat
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = cat,
                                color = if (isSelected) PrimaryIndigo else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }

    GoOnlineDialog(
        showDialog = showGoOnlineDialog,
        onDismiss = {
            showGoOnlineDialog = false
            pendingChannelToPlay = null
        },
        onGoOnlineConfirmed = {
            onEnableOnlineMode()
            pendingChannelToPlay?.let { onPlayChannel(it) }
        }
    )

    // Modal Sheet de Guia de Programação (EPG) e Agendamento de Gravação
    selectedChannelForEpg?.let { channel ->
        val epgSheetState = rememberModalBottomSheetState()
        val tvgId = channel.tvgId ?: ""
        val now = System.currentTimeMillis()
        val programs by if (tvgId.isNotBlank() && database != null) {
            database.epgDao().getSchedule(tvgId, now - 1800000L, now + 86400000L).collectAsState(initial = emptyList())
        } else {
            remember { mutableStateOf(emptyList<EpgProgramEntity>()) }
        }
        val timeFormat = remember { SimpleDateFormat("HH:mm", Locale.getDefault()) }

        ModalBottomSheet(
            onDismissRequest = { selectedChannelForEpg = null },
            sheetState = epgSheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Guia & Gravação: ${channel.name}",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))

                if (programs.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Nenhum guia de programação (EPG) encontrado para este canal.",
                            color = TextMutedDark,
                            fontSize = 13.sp
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Agendar gravação rápida:",
                            color = Color.White,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 14.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(30, 60, 120).forEach { mins ->
                                Button(
                                    onClick = {
                                        if (database != null) {
                                            scope.launch {
                                                RecordingScheduler.startImmediateRecording(
                                                    context = context,
                                                    database = database,
                                                    channelId = channel.id,
                                                    title = "${channel.name} (${mins}m)",
                                                    streamUrl = channel.streamUrl,
                                                    durationMinutes = mins
                                                )
                                                snackbarHostState?.showSnackbar("Gravação de $mins min iniciada para ${channel.name}!")
                                                selectedChannelForEpg = null
                                            }
                                        }
                                    },
                                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                                ) {
                                    Text("${mins}m")
                                }
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.height(350.dp)
                    ) {
                        items(programs) { prog ->
                            val isFuture = prog.start > now
                            val isCurrent = prog.start <= now && prog.end > now
                            val startStr = timeFormat.format(Date(prog.start))
                            val endStr = timeFormat.format(Date(prog.end))
                            val durationMins = ((prog.end - prog.start) / 60000).toInt().coerceAtLeast(10)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(DarkSurfaceVariant)
                                    .padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            text = "$startStr - $endStr",
                                            color = if (isCurrent) PrimaryIndigo else TextMutedDark,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 12.sp
                                        )
                                        if (isCurrent) {
                                            Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                                            Text(
                                                text = "(AO VIVO)",
                                                color = Color(0xFFEF4444),
                                                fontWeight = FontWeight.Bold,
                                                fontSize = 10.sp
                                            )
                                        }
                                    }
                                    Text(
                                        text = prog.title,
                                        color = Color.White,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 14.sp
                                    )
                                    prog.description?.let { desc ->
                                        Text(
                                            text = desc,
                                            color = TextMutedDark,
                                            fontSize = 11.sp,
                                            maxLines = 2
                                        )
                                    }
                                }

                                if (database != null) {
                                    Button(
                                        onClick = {
                                            scope.launch {
                                                if (isFuture) {
                                                    RecordingScheduler.scheduleEpgRecording(
                                                        context = context,
                                                        database = database,
                                                        channelId = channel.id,
                                                        title = "${channel.name}: ${prog.title}",
                                                        streamUrl = channel.streamUrl,
                                                        startEpochMs = prog.start,
                                                        durationMinutes = durationMins
                                                    )
                                                    snackbarHostState?.showSnackbar("Gravação agendada: ${prog.title}")
                                                } else {
                                                    RecordingScheduler.startImmediateRecording(
                                                        context = context,
                                                        database = database,
                                                        channelId = channel.id,
                                                        title = "${channel.name}: ${prog.title}",
                                                        streamUrl = channel.streamUrl,
                                                        durationMinutes = durationMins
                                                    )
                                                    snackbarHostState?.showSnackbar("Gravação iniciada: ${prog.title}")
                                                }
                                                selectedChannelForEpg = null
                                            }
                                        },
                                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                                        contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp)
                                    ) {
                                        Text(
                                            text = if (isFuture) "Agendar" else "Gravar",
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold
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
