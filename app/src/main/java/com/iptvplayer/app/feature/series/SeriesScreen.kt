package com.iptvplayer.app.feature.series

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
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
    database: com.iptvplayer.app.core.database.AppDatabase,
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
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showCategorySheet by remember { mutableStateOf(false) }
    var showGoOnlineDialog by remember { mutableStateOf(false) }
    var pendingSeriesToPlay by remember { mutableStateOf<Pair<ChannelEntity, Boolean>?>(null) }
    var selectedSeriesDetails by remember { mutableStateOf<SeriesDetails?>(null) }
    var isLoadingSeriesDetails by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    // BackHandler hierárquico para o botão de voltar do celular
    BackHandler(enabled = selectedSeriesDetails != null) {
        selectedSeriesDetails = null
    }
    BackHandler(enabled = selectedSeriesDetails == null && showFavoritesOnly) {
        showFavoritesOnly = false
    }
    BackHandler(enabled = selectedSeriesDetails == null && !showFavoritesOnly && searchQuery.isNotEmpty()) {
        searchQuery = ""
    }
    BackHandler(enabled = selectedSeriesDetails == null && !showFavoritesOnly && searchQuery.isEmpty() && selectedCategory != null) {
        selectedCategory = null
    }

    var categorySearchQuery by remember { mutableStateOf("") }

    val categories by if (activePlaylistId != null) {
        repository.getCategories(activePlaylistId, ContentType.SERIES).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }

    val matchingCategories = remember(categories, searchQuery) {
        val q = searchQuery.trim()
        if (q.length >= 2) {
            categories.filter { it.contains(q, ignoreCase = true) }.take(6)
        } else {
            emptyList()
        }
    }

    val seriesPaging = remember(activePlaylistId, selectedCategory, searchQuery, showFavoritesOnly) {
        repository.getChannelsFilteredPaged(
            playlistId = activePlaylistId,
            contentType = ContentType.SERIES,
            category = selectedCategory,
            query = searchQuery.trim(),
            onlyFavorites = showFavoritesOnly
        )
    }.collectAsLazyPagingItems()

    val searchPlaceholder = when {
        showFavoritesOnly && selectedCategory != null -> "Buscar favoritos em \"$selectedCategory\"..."
        showFavoritesOnly -> "Buscar em séries favoritas..."
        selectedCategory != null -> "Buscar em \"$selectedCategory\"..."
        else -> "Buscar séries ou categorias..."
    }

    if (selectedSeriesDetails != null) {
        SeriesDetailScreen(
            seriesDetails = selectedSeriesDetails!!,
            database = database,
            downloadController = downloadController,
            onBackClick = { selectedSeriesDetails = null },
            onPlayEpisode = { episode, isDownloaded ->
                if (isOfflineMode && !isDownloaded) {
                    pendingSeriesToPlay = Pair(episode, false)
                    showGoOnlineDialog = true
                } else {
                    onPlaySeries(episode, isDownloaded)
                }
            },
            snackbarHostState = snackbarHostState,
            modifier = modifier
        )
    } else {
        Column(
            modifier = modifier
                .fillMaxSize()
                .background(DarkBackground)
        ) {
        // Barra de Busca Minimalista com Favoritos e Categorias
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
                        text = searchPlaceholder,
                        color = TextMutedDark,
                        fontSize = 14.sp
                    )
                },
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

            // Botão Ver Somente Favoritos
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showFavoritesOnly = !showFavoritesOnly },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (showFavoritesOnly) Color(0xFFFF2A55).copy(alpha = 0.25f)
                        else DarkSurface
                    )
                    .border(
                        width = 1.dp,
                        color = if (showFavoritesOnly) Color(0xFFFF2A55) else DarkSurfaceVariant,
                        shape = RoundedCornerShape(12.dp)
                    )
            ) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Ver somente favoritas",
                    tint = if (showFavoritesOnly) Color(0xFFFF2A55) else Color.White
                )
            }

            if (categories.isNotEmpty()) {
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = { showCategorySheet = true },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (selectedCategory != null) PrimaryIndigo.copy(alpha = 0.25f) else DarkSurface)
                        .border(
                            width = 1.dp,
                            color = if (selectedCategory != null) PrimaryIndigo else DarkSurfaceVariant,
                            shape = RoundedCornerShape(12.dp)
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Filtro de Categorias",
                        tint = if (selectedCategory != null) PrimaryIndigo else Color.White
                    )
                }
            }
        }

        // Indicador visual de categoria ativa ou sugestões de categorias encontradas na busca
        if (selectedCategory != null) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.22f))
                        .border(1.dp, PrimaryIndigo.copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Category,
                            contentDescription = null,
                            tint = PrimaryIndigo,
                            modifier = Modifier.size(16.dp)
                        )
                        Text(
                            text = "Categoria: $selectedCategory",
                            color = Color.White,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Limpar filtro de categoria",
                            tint = TextMutedDark,
                            modifier = Modifier
                                .size(16.dp)
                                .clickable { selectedCategory = null }
                        )
                    }
                }
            }
        } else if (matchingCategories.isNotEmpty()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 2.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Categorias:",
                    color = TextMutedDark,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(matchingCategories) { cat ->
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(PrimaryIndigo.copy(alpha = 0.18f))
                                .border(1.dp, PrimaryIndigo.copy(alpha = 0.35f), RoundedCornerShape(6.dp))
                                .clickable {
                                    selectedCategory = cat
                                    searchQuery = ""
                                }
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Category,
                                    contentDescription = null,
                                    tint = PrimaryIndigo,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = cat,
                                    color = Color.White,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }
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
                        key = { index -> seriesPaging[index]?.id ?: "placeholder_$index" }
                    ) { index ->
                        val item = seriesPaging[index]
                        if (item != null) {
                            val downloadEntity by repository.observeDownload(item.id).collectAsState(initial = null)

                            ChannelCard(
                                channel = item,
                                isOfflineMode = isOfflineMode,
                                downloadStatus = downloadEntity?.status,
                                onCardClick = {
                                    scope.launch {
                                        try {
                                            isLoadingSeriesDetails = true
                                            selectedSeriesDetails = repository.loadSeriesDetails(item)
                                        } catch (e: Exception) {
                                            selectedSeriesDetails = SeriesMetadataHelper.groupEpisodes(listOf(item), fallbackSeriesTitle = item.name)
                                        } finally {
                                            isLoadingSeriesDetails = false
                                        }
                                    }
                                },
                                onFavoriteToggle = {
                                    scope.launch {
                                        repository.toggleFavorite(item.id, item.isFavorite)
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
                    val emptyMessage = when {
                        showFavoritesOnly && searchQuery.isNotBlank() -> "Nenhuma série favorita encontrada para \"$searchQuery\"."
                        showFavoritesOnly -> "Nenhuma série favorita marcada ainda.\nToque no ícone de coração em qualquer série para favoritá-la."
                        searchQuery.isNotBlank() -> "Nenhuma série para \"$searchQuery\""
                        else -> "Nenhuma série encontrada nesta categoria."
                    }
                    Column(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .padding(32.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            imageVector = if (showFavoritesOnly) Icons.Default.FavoriteBorder else Icons.Default.VideoLibrary,
                            contentDescription = null,
                            tint = if (showFavoritesOnly) Color(0xFFFF2A55) else TextMutedDark,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = emptyMessage,
                            style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            // Overlay de Carregamento dos Episódios da Série
            if (isLoadingSeriesDetails) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.75f)),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(color = PrimaryIndigo, modifier = Modifier.size(44.dp))
                        Spacer(modifier = Modifier.height(14.dp))
                        Text(
                            text = "Carregando temporadas e episódios...",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
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
                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = categorySearchQuery,
                    onValueChange = { categorySearchQuery = it },
                    placeholder = { Text("Pesquisar categoria...", fontSize = 13.sp, color = TextMutedDark) },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp), tint = TextMutedDark) },
                    trailingIcon = {
                        if (categorySearchQuery.isNotEmpty()) {
                            IconButton(onClick = { categorySearchQuery = "" }) {
                                Icon(Icons.Default.Clear, contentDescription = "Limpar", modifier = Modifier.size(16.dp), tint = TextMutedDark)
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrimaryIndigo,
                        unfocusedBorderColor = DarkSurfaceVariant,
                        focusedContainerColor = DarkSurface,
                        unfocusedContainerColor = DarkSurface
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                )

                val displayedCategories = if (categorySearchQuery.isBlank()) categories else categories.filter { it.contains(categorySearchQuery.trim(), ignoreCase = true) }

                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.height(350.dp)
                ) {
                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (selectedCategory == null && !showFavoritesOnly) PrimaryIndigo.copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    selectedCategory = null
                                    showFavoritesOnly = false
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Todas as Séries",
                                color = if (selectedCategory == null && !showFavoritesOnly) PrimaryIndigo else Color.White,
                                fontWeight = if (selectedCategory == null && !showFavoritesOnly) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (showFavoritesOnly) Color(0xFFFF2A55).copy(alpha = 0.2f) else Color.Transparent)
                                .clickable {
                                    showFavoritesOnly = !showFavoritesOnly
                                    showCategorySheet = false
                                }
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (showFavoritesOnly) Color(0xFFFF2A55) else Color.White,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Somente Favoritas",
                                color = if (showFavoritesOnly) Color(0xFFFF2A55) else Color.White,
                                fontWeight = if (showFavoritesOnly) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                    items(displayedCategories) { cat ->
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
