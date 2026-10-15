package com.iptvplayer.app.feature.vod

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
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
import androidx.compose.ui.unit.dp
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
    var showGoOnlineDialog by remember { mutableStateOf(false) }
    var pendingMovieToPlay by remember { mutableStateOf<Pair<ChannelEntity, Boolean>?>(null) }
    val scope = rememberCoroutineScope()

    val categories by if (activePlaylistId != null) {
        repository.getCategories(activePlaylistId, ContentType.MOVIE).collectAsState(initial = emptyList())
    } else {
        remember { mutableStateOf(emptyList<String>()) }
    }

    val moviesPaging = remember(activePlaylistId, selectedCategory, searchQuery) {
        when {
            searchQuery.isNotBlank() -> repository.searchChannelsPaged(searchQuery)
            activePlaylistId != null && selectedCategory != null -> {
                repository.getChannelsByCategoryPaged(activePlaylistId, ContentType.MOVIE, selectedCategory!!)
            }
            activePlaylistId != null -> repository.getChannelsPaged(activePlaylistId, ContentType.MOVIE)
            else -> repository.searchChannelsPaged("")
        }
    }.collectAsLazyPagingItems()

    Column(modifier = modifier.fillMaxSize()) {
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("Buscar filmes (VOD)...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { searchQuery = "" }) {
                        Icon(Icons.Default.Clear, contentDescription = "Limpar busca")
                    }
                }
            },
            singleLine = true,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
        )

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
                        label = { Text("Todos") },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo
                        )
                    )
                }
                items(categories) { category ->
                    FilterChip(
                        selected = selectedCategory == category,
                        onClick = { selectedCategory = if (selectedCategory == category) null else category },
                        label = { Text(category) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = PrimaryIndigo
                        )
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
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

                        ChannelCard(
                            channel = movie,
                            isOfflineMode = isOfflineMode,
                            downloadStatus = downloadEntity?.status,
                            onCardClick = {
                                if (isOfflineMode && !isDownloaded) {
                                    pendingMovieToPlay = Pair(movie, false)
                                    showGoOnlineDialog = true
                                } else {
                                    onPlayMovie(movie, isDownloaded)
                                }
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
            } else if (moviesPaging.itemCount == 0 && activePlaylistId != null) {
                Text(
                    text = "Nenhum filme encontrado nesta playlist.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
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
