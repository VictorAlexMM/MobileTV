package com.iptvplayer.app.feature.playlists

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.automirrored.filled.PlaylistPlay
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.iptvplayer.app.core.database.entity.PlaylistEntity
import com.iptvplayer.app.core.designsystem.theme.DarkBackground
import com.iptvplayer.app.core.designsystem.theme.DarkSurface
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import kotlinx.coroutines.launch

@Composable
fun PlaylistsScreen(
    repository: PlaylistRepository,
    activePlaylistId: Long?,
    onSelectPlaylist: (Long) -> Unit,
    snackbarHostState: SnackbarHostState,
    modifier: Modifier = Modifier
) {
    val playlists by repository.getAllPlaylists().collectAsState(initial = emptyList())
    var showAddDialog by remember { mutableStateOf(false) }
    var playlistToDelete by remember { mutableStateOf<PlaylistEntity?>(null) }
    val scope = rememberCoroutineScope()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        if (playlists.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(32.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(80.dp)
                        .clip(RoundedCornerShape(24.dp))
                        .background(PrimaryIndigo.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.PlaylistPlay,
                        contentDescription = null,
                        tint = PrimaryIndigo,
                        modifier = Modifier.size(44.dp)
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = "Nenhuma playlist adicionada",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    ),
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Importe sua lista M3U ou acesse via Xtream Codes para sincronizar canais, filmes e séries.",
                    style = MaterialTheme.typography.bodyMedium.copy(color = TextMutedDark),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { showAddDialog = true },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(12.dp),
                    contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.size(8.dp))
                    Text(text = "Adicionar Playlist", fontWeight = FontWeight.SemiBold)
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        text = "Suas Playlists (${playlists.size})",
                        style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
                        color = TextMutedDark,
                        modifier = Modifier.padding(start = 4.dp, bottom = 4.dp)
                    )
                }
                items(playlists, key = { it.id }) { playlist ->
                    val isActive = playlist.id == activePlaylistId
                    PlaylistItemCard(
                        playlist = playlist,
                        isActive = isActive,
                        onSelect = {
                            onSelectPlaylist(playlist.id)
                            scope.launch {
                                snackbarHostState.showSnackbar("Playlist \"${playlist.name}\" selecionada como ativa.")
                            }
                        },
                        onDeleteClick = {
                            playlistToDelete = playlist
                        }
                    )
                }
            }
        }

        // Floating Action Button
        FloatingActionButton(
            onClick = { showAddDialog = true },
            containerColor = PrimaryIndigo,
            contentColor = Color.White,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(24.dp)
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Adicionar Playlist")
        }
    }

    // Dialogo de Adição com Feedback e Validação Completa
    if (showAddDialog) {
        AddPlaylistDialog(
            onDismiss = { showAddDialog = false },
            onImportM3U = { name, url, onComplete, onError ->
                scope.launch {
                    try {
                        val result = repository.importM3UFromUrl(name, url)
                        onSelectPlaylist(result.playlistId)
                        onComplete()
                        showAddDialog = false
                        snackbarHostState.showSnackbar("✓ Playlist \"${result.name}\" adicionada! ${result.totalCount} itens carregados.")
                    } catch (e: Exception) {
                        onError(e.localizedMessage ?: "Erro ao carregar a playlist. Verifique o link e tente novamente.")
                    }
                }
            },
            onImportXtream = { server, user, pass, onComplete, onError ->
                scope.launch {
                    try {
                        val result = repository.importXtreamAccount(server, user, pass)
                        onSelectPlaylist(result.playlistId)
                        onComplete()
                        showAddDialog = false
                        snackbarHostState.showSnackbar(
                            "✓ Xtream conectado! ${result.liveCount} canais, ${result.vodCount} filmes e ${result.seriesCount} séries."
                        )
                    } catch (e: Exception) {
                        onError(e.localizedMessage ?: "Erro na autenticação Xtream. Verifique os dados.")
                    }
                }
            }
        )
    }

    // Diálogo de Confirmação de Exclusão
    playlistToDelete?.let { pl ->
        AlertDialog(
            onDismissRequest = { playlistToDelete = null },
            title = {
                Text(text = "Excluir Playlist", color = Color.White, fontWeight = FontWeight.Bold)
            },
            text = {
                Text(
                    text = "Deseja realmente remover a playlist \"${pl.name}\" e todos os seus canais, filmes e séries?",
                    color = TextMutedDark
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val name = pl.name
                        scope.launch {
                            repository.deletePlaylist(pl.id)
                            snackbarHostState.showSnackbar("Playlist \"$name\" removida.")
                        }
                        playlistToDelete = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444))
                ) {
                    Text("Excluir", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { playlistToDelete = null }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
private fun PlaylistItemCard(
    playlist: PlaylistEntity,
    isActive: Boolean,
    onSelect: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) PrimaryIndigo else DarkSurfaceVariant,
                shape = RoundedCornerShape(16.dp)
            ),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) DarkSurface.copy(alpha = 0.95f) else DarkSurface
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = playlist.name,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp
                        ),
                        color = Color.White,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    if (isActive) {
                        Spacer(modifier = Modifier.size(8.dp))
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(StatusDownloadedGreen.copy(alpha = 0.15f))
                                .border(1.dp, StatusDownloadedGreen.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 8.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = "ATIVA",
                                color = StatusDownloadedGreen,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = playlist.type.name,
                            color = TextMutedDark,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = playlist.url ?: "Arquivo local",
                        style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isActive) {
                    Button(
                        onClick = onSelect,
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                        shape = RoundedCornerShape(10.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(text = "Ativar", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                } else {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Ativa",
                        tint = StatusDownloadedGreen,
                        modifier = Modifier
                            .size(28.dp)
                            .padding(end = 4.dp)
                    )
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Excluir playlist",
                        tint = Color.Red.copy(alpha = 0.7f),
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun AddPlaylistDialog(
    onDismiss: () -> Unit,
    onImportM3U: (name: String, url: String, onComplete: () -> Unit, onError: (String) -> Unit) -> Unit,
    onImportXtream: (server: String, user: String, pass: String, onComplete: () -> Unit, onError: (String) -> Unit) -> Unit
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    var playlistName by remember { mutableStateOf("") }
    var playlistUrl by remember { mutableStateOf("") }

    var xtreamServer by remember { mutableStateOf("") }
    var xtreamUser by remember { mutableStateOf("") }
    var xtreamPass by remember { mutableStateOf("") }

    var nameError by remember { mutableStateOf(false) }
    var urlError by remember { mutableStateOf(false) }
    var xtreamError by remember { mutableStateOf(false) }

    var isImporting by remember { mutableStateOf(false) }
    var importErrorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = {
            if (!isImporting) onDismiss()
        },
        title = {
            Text(
                text = "Adicionar Playlist",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                if (isImporting) {
                    // Estado de Carregamento Elegante dentro do Popup
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        CircularProgressIndicator(
                            color = PrimaryIndigo,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "Importando conteúdo...",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Buscando categorias, canais e metadados. Isso leva apenas alguns instantes.",
                            style = MaterialTheme.typography.bodySmall.copy(color = TextMutedDark),
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    // Abas de tipo de lista
                    PrimaryTabRow(
                        selectedTabIndex = selectedTab,
                        containerColor = DarkSurfaceVariant,
                        contentColor = Color.White
                    ) {
                        Tab(
                            selected = selectedTab == 0,
                            onClick = { selectedTab = 0 },
                            text = { Text("URL M3U", fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal) }
                        )
                        Tab(
                            selected = selectedTab == 1,
                            onClick = { selectedTab = 1 },
                            text = { Text("Xtream Codes", fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }

                    // Mensagem de Erro (se houver)
                    AnimatedVisibility(visible = importErrorMessage != null) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f))
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.3f), RoundedCornerShape(8.dp))
                                .padding(10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.ErrorOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = importErrorMessage ?: "",
                                color = Color(0xFFFCA5A5),
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (selectedTab == 0) {
                        OutlinedTextField(
                            value = playlistName,
                            onValueChange = {
                                playlistName = it
                                nameError = false
                            },
                            label = { Text("Nome da Playlist") },
                            placeholder = { Text("Ex: Canais TV, Esportes") },
                            singleLine = true,
                            isError = nameError,
                            supportingText = if (nameError) { { Text("Informe o nome da playlist", color = Color(0xFFEF4444)) } } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = playlistUrl,
                            onValueChange = {
                                playlistUrl = it
                                urlError = false
                            },
                            label = { Text("URL M3U / M3U8") },
                            placeholder = { Text("https://exemplo.com/lista.m3u") },
                            singleLine = true,
                            isError = urlError,
                            supportingText = if (urlError) { { Text("Informe uma URL válida (http:// ou https://)", color = Color(0xFFEF4444)) } } else null,
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        // Botão de preenchimento rápido para teste (IPTV Pública Aberta)
                        OutlinedButton(
                            onClick = {
                                playlistName = "Canais Abertos (Demo)"
                                playlistUrl = "https://iptv-org.github.io/iptv/countries/br.m3u"
                                nameError = false
                                urlError = false
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = PrimaryIndigo)
                        ) {
                            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Preencher com Lista de Teste (Brasil)", fontSize = 12.sp)
                        }
                    } else {
                        OutlinedTextField(
                            value = xtreamServer,
                            onValueChange = {
                                xtreamServer = it
                                xtreamError = false
                            },
                            label = { Text("Servidor") },
                            placeholder = { Text("http://servidor.com:8080") },
                            singleLine = true,
                            isError = xtreamError && xtreamServer.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = xtreamUser,
                            onValueChange = {
                                xtreamUser = it
                                xtreamError = false
                            },
                            label = { Text("Usuário") },
                            singleLine = true,
                            isError = xtreamError && xtreamUser.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = xtreamPass,
                            onValueChange = {
                                xtreamPass = it
                                xtreamError = false
                            },
                            label = { Text("Senha") },
                            singleLine = true,
                            isError = xtreamError && xtreamPass.isBlank(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = PrimaryIndigo,
                                unfocusedBorderColor = DarkSurfaceVariant
                            ),
                            shape = RoundedCornerShape(10.dp),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isImporting) {
                Button(
                    onClick = {
                        importErrorMessage = null
                        if (selectedTab == 0) {
                            var valid = true
                            if (playlistName.isBlank()) {
                                nameError = true
                                valid = false
                            }
                            if (playlistUrl.isBlank() || (!playlistUrl.trim().startsWith("http://") && !playlistUrl.trim().startsWith("https://"))) {
                                urlError = true
                                valid = false
                            }
                            if (valid) {
                                isImporting = true
                                onImportM3U(
                                    playlistName.trim(),
                                    playlistUrl.trim(),
                                    { isImporting = false },
                                    { err ->
                                        isImporting = false
                                        importErrorMessage = err
                                    }
                                )
                            }
                        } else {
                            if (xtreamServer.isBlank() || xtreamUser.isBlank() || xtreamPass.isBlank()) {
                                xtreamError = true
                            } else {
                                isImporting = true
                                onImportXtream(
                                    xtreamServer.trim(),
                                    xtreamUser.trim(),
                                    xtreamPass.trim(),
                                    { isImporting = false },
                                    { err ->
                                        isImporting = false
                                        importErrorMessage = err
                                    }
                                )
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Adicionar Playlist", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isImporting) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = TextMutedDark)
                }
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(18.dp)
    )
}
