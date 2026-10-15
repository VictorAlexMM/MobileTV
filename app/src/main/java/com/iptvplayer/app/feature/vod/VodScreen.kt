package com.iptvplayer.app.feature.vod

import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.paging.LoadState
import androidx.paging.compose.collectAsLazyPagingItems
import com.iptvplayer.app.core.database.entity.ChannelEntity
import com.iptvplayer.app.core.database.model.ContentType
import com.iptvplayer.app.core.database.model.DownloadStatus
import com.iptvplayer.app.core.designsystem.components.ChannelCard
import com.iptvplayer.app.core.designsystem.components.GoOnlineDialog
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.live.ChannelRepository
import kotlinx.coroutines.launch

@OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)
@Composable
fun VodScreen(
    repository: ChannelRepository,
    downloadController: DownloadController,
    activePlaylistId: Long?,
    isOfflineMode: Boolean,
    onPlayMovie: (ChannelEntity, isDownloaded: Boolean) -> Unit,
    onEnableOnlineMode: () -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf<String?>(null) }
    var showFavoritesOnly by remember { mutableStateOf(false) }
    var showGoOnlineDialog by remember { mutableStateOf(false) }
    var pendingMovieToPlay by remember { mutableStateOf<Pair<ChannelEntity, Boolean>?>(null) }
    var selectedMovieForDetails by remember { mutableStateOf<ChannelEntity?>(null) }
    val scope = rememberCoroutineScope()

    // BackHandler hierárquico para o botão de voltar do celular
    BackHandler(enabled = selectedMovieForDetails != null) {
        selectedMovieForDetails = null
    }
    BackHandler(enabled = selectedMovieForDetails == null && showFavoritesOnly) {
        showFavoritesOnly = false
    }
    BackHandler(enabled = selectedMovieForDetails == null && !showFavoritesOnly && searchQuery.isNotEmpty()) {
        searchQuery = ""
    }
    BackHandler(enabled = selectedMovieForDetails == null && !showFavoritesOnly && searchQuery.isEmpty() && selectedCategory != null) {
        selectedCategory = null
    }

    val categories by if (activePlaylistId != null) {
        repository.getCategories(activePlaylistId, ContentType.MOVIE).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }

    val moviesPaging = remember(activePlaylistId, selectedCategory, searchQuery, showFavoritesOnly) {
        repository.getChannelsFilteredPaged(
            playlistId = activePlaylistId,
            contentType = ContentType.MOVIE,
            category = selectedCategory,
            query = searchQuery.trim(),
            onlyFavorites = showFavoritesOnly
        )
    }.collectAsLazyPagingItems()

    val allProgress by repository.getAllWatchProgress().collectAsState(initial = emptyList())
    val progressMap = remember(allProgress) { allProgress.associateBy { it.contentId } }

    var showCategorySheet by remember { mutableStateOf(false) }

    val searchPlaceholder = when {
        showFavoritesOnly && selectedCategory != null -> "Buscar favoritos em \"$selectedCategory\"..."
        showFavoritesOnly -> "Buscar em filmes favoritos..."
        selectedCategory != null -> "Buscar em \"$selectedCategory\"..."
        else -> "Buscar filmes (VOD)..."
    }

    Column(modifier = modifier.fillMaxSize()) {
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

            // Botão Ver Somente Favoritos
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(
                onClick = { showFavoritesOnly = !showFavoritesOnly },
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (showFavoritesOnly) Color(0xFFFF2A55).copy(alpha = 0.25f)
                        else com.iptvplayer.app.core.designsystem.theme.DarkSurface
                    )
            ) {
                Icon(
                    imageVector = if (showFavoritesOnly) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = "Ver somente favoritos",
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
                        imageVector = androidx.compose.material.icons.Icons.Default.Movie,
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
                        text = "Adicione ou selecione uma playlist na aba Playlists para carregar o catálogo de filmes.",
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
                        count = moviesPaging.itemCount,
                        key = { index -> moviesPaging[index]?.id ?: index }
                    ) { index ->
                        val movie = moviesPaging[index]
                        if (movie != null) {
                            val downloadEntity by repository.observeDownload(movie.id).collectAsState(initial = null)
                            val isDownloaded = downloadEntity?.status == DownloadStatus.COMPLETED

                            val watchProgress = progressMap[movie.id]
                            val progressFraction = if (watchProgress != null && watchProgress.durationMs > 0) {
                                (watchProgress.positionMs.toFloat() / watchProgress.durationMs.toFloat()).coerceIn(0f, 1f)
                            } else 0f

                            ChannelCard(
                                channel = movie,
                                isOfflineMode = isOfflineMode,
                                downloadStatus = downloadEntity?.status,
                                watchProgress = progressFraction,
                                onCardClick = {
                                    selectedMovieForDetails = movie
                                },
                                onFavoriteToggle = {
                                    scope.launch {
                                        repository.toggleFavorite(movie.id, movie.isFavorite)
                                    }
                                },
                                onDownloadClick = {
                                    scope.launch {
                                        try {
                                            downloadController.startDownload(
                                                contentId = movie.id,
                                                title = movie.name,
                                                sourceUrl = movie.streamUrl,
                                                posterUrl = movie.logoUrl
                                            )
                                            snackbarHostState.showSnackbar("Download iniciado: ${movie.name}")
                                        } catch (e: Exception) {
                                            snackbarHostState.showSnackbar("Erro: ${e.localizedMessage}")
                                        }
                                    }
                                }
                            )
                        }
                    }

                    if (moviesPaging.loadState.append is LoadState.Loading) {
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

                if (moviesPaging.loadState.refresh is LoadState.Loading) {
                    CircularProgressIndicator(
                        color = PrimaryIndigo,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else if (moviesPaging.itemCount == 0) {
                    val emptyMessage = when {
                        showFavoritesOnly && searchQuery.isNotBlank() -> "Nenhum filme favorito encontrado para \"$searchQuery\"."
                        showFavoritesOnly -> "Nenhum filme favorito marcado ainda.\nToque no ícone de coração em qualquer filme para favoritá-lo."
                        searchQuery.isNotBlank() -> "Nenhum filme encontrado para \"$searchQuery\"."
                        else -> "Nenhum filme encontrado nesta categoria."
                    }
                    Text(
                        text = emptyMessage,
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
                    text = "Categorias de Filmes (${categories.size})",
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
                                text = "Todos os Filmes",
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
                                text = "Somente Favoritos",
                                color = if (showFavoritesOnly) Color(0xFFFF2A55) else Color.White,
                                fontWeight = if (showFavoritesOnly) FontWeight.Bold else FontWeight.Normal
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

    // Modal Sheet de Detalhes do Filme com Biografia, Data de Lançamento e Estilo
    selectedMovieForDetails?.let { movie ->
        val downloadEntity by repository.observeDownload(movie.id).collectAsState(initial = null)
        val isDownloaded = downloadEntity?.status == DownloadStatus.COMPLETED
        val watchProgress by repository.observeWatchProgress(movie.id).collectAsState(initial = null)

        MovieDetailsSheet(
            movie = movie,
            watchProgress = watchProgress,
            isDownloaded = isDownloaded,
            onDismiss = { selectedMovieForDetails = null },
            onPlayClick = {
                selectedMovieForDetails = null
                if (isOfflineMode && !isDownloaded) {
                    pendingMovieToPlay = Pair(movie, false)
                    showGoOnlineDialog = true
                } else {
                    onPlayMovie(movie, isDownloaded)
                }
            },
            onDownloadClick = {
                scope.launch {
                    try {
                        downloadController.startDownload(
                            contentId = movie.id,
                            title = movie.name,
                            sourceUrl = movie.streamUrl,
                            posterUrl = movie.logoUrl
                        )
                        snackbarHostState.showSnackbar("Download iniciado: ${movie.name}")
                    } catch (e: Exception) {
                        snackbarHostState.showSnackbar("Erro: ${e.localizedMessage}")
                    }
                }
            },
            onFavoriteToggle = {
                scope.launch {
                    repository.toggleFavorite(movie.id, movie.isFavorite)
                    selectedMovieForDetails = movie.copy(isFavorite = !movie.isFavorite)
                }
            }
        )
    }

    GoOnlineDialog(
        showDialog = showGoOnlineDialog,
        onDismiss = {
            showGoOnlineDialog = false
            pendingMovieToPlay = null
        },
        onGoOnlineConfirmed = {
            onEnableOnlineMode()
            pendingMovieToPlay?.let { (movie, isDownloaded) ->
                onPlayMovie(movie, isDownloaded)
            }
        }
    )
}
