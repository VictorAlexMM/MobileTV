package com.iptvplayer.app.feature.series

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.components.ChannelCard
import com.iptvplayer.app.core.designsystem.components.GoOnlineDialog
import com.iptvplayer.app.core.designsystem.theme.DarkBackground
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.live.ChannelRepository
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesScreen(
    repository: ChannelRepository,
    downloadController: DownloadController,
    activePlaylistId: Long?,
    isOfflineMode: Boolean,
    onPlaySeries: (ChannelEntity, isDownloaded: Boolean) -> Unit,
    onEnableOnlineMode: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showGoOnlineDialog by remember { mutableStateOf(false) }
    var pendingSeriesToPlay by remember { mutableStateOf<Pair<ChannelEntity, Boolean>?>(null) }
    val scope = rememberCoroutineScope()

    val categories by if (activePlaylistId != null) {
        repository.getCategories(activePlaylistId, ContentType.SERIES).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }

    val seriesPaging = remember(activePlaylistId, selectedCategory, searchQuery) {
        when {
            searchQuery.isNotBlank() -> repository.searchChannelsPaged(searchQuery)
            activePlaylistId != null && selectedCategory != null -> {
                repository.getChannelsByCategoryPaged(activePlaylistId, ContentType.SERIES, selectedCategory!!)
            }
            activePlaylistId != null -> repository.getChannelsPaged(activePlaylistId, ContentType.SERIES)
            else -> repository.searchChannelsPaged("")
        }
    }.collectAsLazyPagingItems()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        // Barra de Busca Minimalista
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                placeholder = { Text("Buscar séries...", color = TextMutedDark, fontSize = 14.sp) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = "Buscar",
                        tint = TextMutedDark,
                        modifier = Modifier.size(20.dp)
                    )
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(
                                imageVector = Icons.Default.Clear,
                                contentDescription = "Limpar",
                                tint = TextMutedDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = PrimaryIndigo,
                    unfocusedBorderColor = DarkSurfaceVariant,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface
                ),
                modifier = Modifier.weight(1f)
            )

            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory != null) PrimaryIndigo.copy(alpha = 0.2f) else DarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (selectedCategory != null) PrimaryIndigo else DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Category,
                        contentDescription = "Categorias",
                        tint = if (selectedCategory != null) PrimaryIndigo else Color.White
                    )
                }
            }
        }

        // Carrossel de Categorias
        if (categories.isNotEmpty() && searchQuery.isBlank()) {
            LazyRow(
                contentPadding = PaddingValues(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                item {
                    FilterChip(
                        selected = selectedCategory == null,
                        onClick = { selectedCategory = null },
                        label = { Text("Todas as Séries") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurface,
                            labelColor = TextMutedDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == null,
                            borderColor = DarkSurfaceVariant,
                            selectedBorderColor = PrimaryIndigo
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo,
                            selectedLabelColor = Color.White,
                            containerColor = DarkSurface,
                            labelColor = TextMutedDark
                        ),
                        border = FilterChipDefaults.filterChipBorder(
                            enabled = true,
                            selected = selectedCategory == category,
                            borderColor = DarkSurfaceVariant,
                            selectedBorderColor = PrimaryIndigo
                        ),
                        shape = RoundedCornerShape(20.dp)
                    )
                }
            }
        }

        // Ação de download da temporada inteira quando uma categoria de série estiver selecionada
        if (selectedCategory != null && activePlaylistId != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(DarkSurfaceVariant)
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = selectedCategory!!,
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Baixar todos os episódios desta temporada",
                        fontSize = 11.sp,
                        color = TextMutedDark
                    )
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = {
                        scope.launch {
                            try {
                                val episodes = repository.getChannelsByGroup(activePlaylistId, ContentType.SERIES, selectedCategory!!)
                                if (episodes.isEmpty()) {
                                    snackbarHostState.showSnackbar("Nenhum episódio encontrado nesta temporada.")
                                } else {
                                    val count = downloadController.startBatchDownload(episodes)
                                    snackbarHostState.showSnackbar("Iniciando download de $count episódios da temporada!")
                                }
                            } catch (e: Exception) {
                                snackbarHostState.showSnackbar("Erro ao baixar temporada: ${e.localizedMessage}")
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(8.dp),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Icon(imageVector = Icons.Default.Download, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Baixar Temporada", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        // Conteúdo
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
                        imageVector = Icons.Default.VideoLibrary,
                        contentDescription = null,
                        tint = TextMutedDark,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Nenhuma playlist ativa",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selecione ou adicione uma playlist na aba Playlists para carregar as séries.",
                        style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
                        textAlign = TextAlign.Center
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(
                        count = seriesPaging.itemCount,
                        key = { index -> seriesPaging[index]?.id ?: index }
                    ) { index ->
                        val item = seriesPaging[index]
                        if (item != null) {
                            val downloadEntity by repository.observeDownload(item.id).collectAsState(initial = null)
                            val isDownloaded = downloadEntity?.status == DownloadStatus.COMPLETED

                            ChannelCard(
                                channel = item,
                                isOfflineMode = isOfflineMode,
                                downloadStatus = downloadEntity?.status,
                                onCardClick = {
                                    if (isOfflineMode && !isDownloaded) {
                                        pendingSeriesToPlay = Pair(item, false)
                                        showGoOnlineDialog = true
                                    } else {
                                        onPlaySeries(item, isDownloaded)
                                    }
                                },
                                onFavoriteToggle = {
                                    scope.launch {
                                        repository.toggleFavorite(item.id, item.isFavorite)
                                    }
                                },
                                onDownloadClick = {
                                    scope.launch {
                                        try {
                                            downloadController.startDownload(
                                                contentId = item.id,
                                                title = item.name,
                                                sourceUrl = item.streamUrl,
                                                posterUrl = item.logoUrl
                                            )
                                            snackbarHostState.showSnackbar("Download iniciado: ${item.name}")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Erro: ${e.localizedMessage}")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (seriesPaging.loadState.append is LoadState.Loading) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(32.dp))
                            }
                        }
                    }
                }

                if (seriesPaging.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (seriesPaging.itemCount == 0) {
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = TextMutedDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (searchQuery.isNotBlank()) "Nenhuma série para \"$searchQuery\"" else "Nenhuma série encontrada nesta categoria.",
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }
        }
    }

    // Modal Sheet de Todas as Categorias
    if (showCategorySheet) {
        val sheetState = rememberModalBottomSheetState()
        ModalBottomSheet(
            onDismissRequest = { showCategorySheet = false },
            sheetState = sheetState,
            containerColor = DarkSurface
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                Text(
                    text = "Categorias de Séries (${categories.size})",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(16.dp))

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
                                text = "Todas as Séries",
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
            pendingSeriesToPlay = null
        },
        onGoOnlineConfirmed = {
            onEnableOnlineMode()
            pendingSeriesToPlay?.let { (item, isDownloaded) ->
                onPlaySeries(item, isDownloaded)
            }
        }
    )
}
