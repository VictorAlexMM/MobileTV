package com.iptvplayer.app.feature.settings

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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.iptvplayer.app.core.common.StorageUtils
import com.iptvplayer.app.core.database.AppDatabase
import com.iptvplayer.app.core.database.entity.DownloadEntity
import com.iptvplayer.app.core.datastore.SettingsRepository
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusDownloadedGreen
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.downloads.DownloadController
import com.iptvplayer.app.feature.downloads.DownloadModule
import kotlinx.coroutines.launch
import java.io.File

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    downloadController: DownloadController,
    database: AppDatabase? = null,
    onPlayOfflineItem: ((DownloadEntity) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val offlineMode by settingsRepository.offlineModeEnabled.collectAsState(initial = false)
    val wifiOnly by settingsRepository.wifiOnlyDownloads.collectAsState(initial = true)
    val externalPlayer by settingsRepository.externalPlayerEnabled.collectAsState(initial = false)

    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }
    var showDeleteAllDownloadsDialog by remember { mutableStateOf(false) }
    var showFolderHelpDialog by remember { mutableStateOf(false) }

    var selectedStorageTab by remember { mutableIntStateOf(0) }
    var diskRefreshTrigger by remember { mutableIntStateOf(0) }

    val completedDownloads by (database?.downloadDao()?.getCompletedDownloads()?.collectAsState(initial = emptyList())
        ?: remember { mutableStateOf(emptyList()) })

    val downloadDir = remember(context) { DownloadModule.getDownloadDirectory(context) }
    val physicalFiles = remember(context, diskRefreshTrigger, completedDownloads) {
        DownloadModule.getDownloadedFilesList(context)
    }
    val downloadsDirSize = remember(context, diskRefreshTrigger, completedDownloads) {
        DownloadModule.getDownloadDirSizeBytes(context)
    }
    val availableBytes = remember(context, diskRefreshTrigger) {
        StorageUtils.getAvailableStorageBytes(context)
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Seção: Rede e Modo Offline
        item {
            Text(
                text = "Rede & Modo Offline",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    // Toggle Ficar Offline
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CloudOff,
                                contentDescription = null,
                                tint = if (offlineMode) StatusWarningAmber else TextMutedDark
                            )
                            Column {
                                Text(
                                    text = "Ficar offline (Modo Manual)",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Bloqueia todo tráfego de rede do app e exibe apenas conteúdo local",
                                    fontSize = 12.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Switch(
                            checked = offlineMode,
                            onCheckedChange = { scope.launch { settingsRepository.setOfflineMode(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = StatusWarningAmber
                            )
                        )
                    }

                    // Baixar Somente em Wi-Fi
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Wifi,
                                contentDescription = null,
                                tint = TextMutedDark
                            )
                            Column {
                                Text(
                                    text = "Baixar somente em Wi-Fi",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Economiza a franquia de dados móveis ao realizar downloads",
                                    fontSize = 12.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Switch(
                            checked = wifiOnly,
                            onCheckedChange = { scope.launch { settingsRepository.setWifiOnlyDownloads(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }
        }

        // Seção: Player e Reprodução
        item {
            Text(
                text = "Player e Reprodução",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.SmartDisplay,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                            Column {
                                Text(
                                    text = "Usar Player Externo Padrão",
                                    fontWeight = FontWeight.SemiBold,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = "Abre canais e vídeos em reprodutores externos (VLC, MX Player, etc.)",
                                    fontSize = 12.sp,
                                    color = TextMutedDark
                                )
                            }
                        }

                        Switch(
                            checked = externalPlayer,
                            onCheckedChange = { scope.launch { settingsRepository.setExternalPlayer(it) } },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = Color.White,
                                checkedTrackColor = PrimaryIndigo
                            )
                        )
                    }
                }
            }
        }

        // Seção: Armazenamento e Localização de Downloads
        item {
            Text(
                text = "Armazenamento & Downloads",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    // Título e Ícone do Local
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = StatusWarningAmber,
                            modifier = Modifier.size(24.dp)
                        )
                        Column {
                            Text(
                                text = "Onde os downloads são salvos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                            Text(
                                text = "Diretório local seguro para acesso offline",
                                fontSize = 12.sp,
                                color = TextMutedDark
                            )
                        }
                    }

                    // Caixa exibindo o caminho absoluto
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.45f))
                            .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
                            .padding(12.dp)
                    ) {
                        Text(
                            text = downloadDir.absolutePath,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = Color(0xFFE2E8F0),
                            lineHeight = 16.sp
                        )
                    }

                    // Ações de Acesso ao Caminho
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = {
                                StorageUtils.copyToClipboard(context, downloadDir.absolutePath)
                            },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.ContentCopy,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Copiar Caminho", fontSize = 12.sp)
                        }

                        Button(
                            onClick = {
                                val opened = StorageUtils.openFolderInFileManager(context, downloadDir)
                                if (!opened) {
                                    StorageUtils.copyToClipboard(context, downloadDir.absolutePath)
                                    showFolderHelpDialog = true
                                }
                            },
                            modifier = Modifier.weight(1f),
                            colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo),
                            shape = RoundedCornerShape(8.dp),
                            contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(text = "Acessar Pasta", fontSize = 12.sp)
                        }
                    }

                    // Métricas de Armazenamento
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.Black.copy(alpha = 0.25f))
                            .padding(10.dp),
                        horizontalArrangement = Arrangement.SpaceAround,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StorageUtils.formatBytes(downloadsDirSize),
                                fontWeight = FontWeight.Bold,
                                color = StatusDownloadedGreen,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Downloads",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = StorageUtils.formatBytes(availableBytes),
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Livre no Celular",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }

                        Box(
                            modifier = Modifier
                                .width(1.dp)
                                .height(24.dp)
                                .background(Color.White.copy(alpha = 0.15f))
                        )

                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${completedDownloads.size} itens",
                                fontWeight = FontWeight.Bold,
                                color = PrimaryIndigo,
                                fontSize = 14.sp
                            )
                            Text(
                                text = "Mídias Salvas",
                                fontSize = 11.sp,
                                color = TextMutedDark
                            )
                        }
                    }
                }
            }
        }

        // Seção: Visualizador de Itens Baixados e Arquivos Físicos
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Storage,
                                contentDescription = null,
                                tint = PrimaryIndigo
                            )
                            Text(
                                text = "Itens Baixados & Arquivos",
                                fontWeight = FontWeight.Bold,
                                fontSize = 15.sp
                            )
                        }

                        IconButton(
                            onClick = { diskRefreshTrigger++ },
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Atualizar",
                                tint = TextMutedDark,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    TabRow(
                        selectedTabIndex = selectedStorageTab,
                        containerColor = Color.Transparent,
                        contentColor = PrimaryIndigo
                    ) {
                        Tab(
                            selected = selectedStorageTab == 0,
                            onClick = { selectedStorageTab = 0 },
                            text = { Text("Mídias (${completedDownloads.size})", fontSize = 13.sp) }
                        )
                        Tab(
                            selected = selectedStorageTab == 1,
                            onClick = { selectedStorageTab = 1 },
                            text = { Text("Arquivos Físicos (${physicalFiles.size})", fontSize = 13.sp) }
                        )
                    }

                    when (selectedStorageTab) {
                        0 -> {
                            if (completedDownloads.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nenhum download concluído salvo.",
                                        fontSize = 13.sp,
                                        color = TextMutedDark
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    completedDownloads.forEach { item ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(Color.Black.copy(alpha = 0.3f))
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                                        ) {
                                            Box(
                                                modifier = Modifier
                                                    .size(42.dp)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(Color.Black.copy(alpha = 0.4f)),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                if (!item.posterUrl.isNullOrBlank()) {
                                                    AsyncImage(
                                                        model = item.posterUrl,
                                                        contentDescription = item.title,
                                                        contentScale = ContentScale.Crop,
                                                        modifier = Modifier.size(42.dp)
                                                    )
                                                } else {
                                                    Icon(
                                                        imageVector = Icons.Default.Movie,
                                                        contentDescription = null,
                                                        tint = PrimaryIndigo,
                                                        modifier = Modifier.size(22.dp)
                                                    )
                                                }
                                            }

                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = item.title,
                                                    fontWeight = FontWeight.SemiBold,
                                                    fontSize = 13.sp,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                                Text(
                                                    text = "${StorageUtils.formatBytes(item.bytesDownloaded)} • ${StorageUtils.formatDate(item.completedAt ?: item.createdAt)}",
                                                    fontSize = 11.sp,
                                                    color = TextMutedDark
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                IconButton(
                                                    onClick = { onPlayOfflineItem?.invoke(item) },
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.PlayArrow,
                                                        contentDescription = "Assistir Offline",
                                                        tint = StatusDownloadedGreen,
                                                        modifier = Modifier.size(24.dp)
                                                    )
                                                }

                                                IconButton(
                                                    onClick = {
                                                        downloadController.removeDownload(item.id)
                                                        diskRefreshTrigger++
                                                    },
                                                    modifier = Modifier.size(34.dp)
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Delete,
                                                        contentDescription = "Excluir",
                                                        tint = Color.Red.copy(alpha = 0.8f),
                                                        modifier = Modifier.size(20.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (physicalFiles.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "Nenhum arquivo físico presente na pasta de downloads.",
                                        fontSize = 13.sp,
                                        color = TextMutedDark
                                    )
                                }
                            } else {
                                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                    physicalFiles.take(15).forEach { file ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(6.dp))
                                                .background(Color.Black.copy(alpha = 0.25f))
                                                .padding(horizontal = 8.dp, vertical = 6.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
                                                contentDescription = null,
                                                tint = PrimaryIndigo,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Column(modifier = Modifier.weight(1f)) {
                                                Text(
                                                    text = file.name,
                                                    fontFamily = FontFamily.Monospace,
                                                    fontSize = 11.sp,
                                                    fontWeight = FontWeight.Medium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis,
                                                    color = Color(0xFFCBD5E1)
                                                )
                                                Text(
                                                    text = "${StorageUtils.formatBytes(file.length())} • ${StorageUtils.formatDate(file.lastModified())}",
                                                    fontSize = 10.sp,
                                                    color = TextMutedDark
                                                )
                                            }
                                        }
                                    }
                                    if (physicalFiles.size > 15) {
                                        Text(
                                            text = "... e mais ${physicalFiles.size - 15} arquivos no disco",
                                            fontSize = 11.sp,
                                            color = TextMutedDark,
                                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    // Botões de Ação de Limpeza
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { showClearConfirmDialog = true },
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = StatusWarningAmber),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Text("Pausar Downloads", fontSize = 12.sp)
                        }

                        if (completedDownloads.isNotEmpty()) {
                            OutlinedButton(
                                onClick = { showDeleteAllDownloadsDialog = true },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(8.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteSweep,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Limpar Todos", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }

        // Seção: Termos e Sobre
        item {
            Text(
                text = "Informações Legais",
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = PrimaryIndigo
                ),
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = DarkSurfaceVariant),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.Info, contentDescription = null, tint = PrimaryIndigo)
                        Text(text = "Termos de Uso e Isenção de Responsabilidade", fontWeight = FontWeight.SemiBold)
                    }
                    Text(
                        text = "O aplicativo é estritamente um reprodutor de mídia e não inclui nem hospeda nenhum conteúdo.",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )
                    Button(
                        onClick = { showTermsDialog = true },
                        colors = ButtonDefaults.buttonColors(containerColor = PrimaryIndigo)
                    ) {
                        Text("Visualizar Termos Completos")
                    }
                }
            }
        }
    }

    if (showFolderHelpDialog) {
        AlertDialog(
            onDismissRequest = { showFolderHelpDialog = false },
            title = { Text("Acesso à Pasta de Downloads") },
            text = {
                Text(
                    "O caminho da pasta foi copiado para a sua área de transferência:\n\n" +
                    "${downloadDir.absolutePath}\n\n" +
                    "Devido às restrições de segurança do Android no armazenamento de dados privados do aplicativo (/Android/data/), abra o app Meus Arquivos / Files ou conecte o celular ao computador via cabo USB para navegar diretamente pelos arquivos baixados."
                )
            },
            confirmButton = {
                Button(onClick = { showFolderHelpDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }

    if (showClearConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showClearConfirmDialog = false },
            title = { Text("Pausar Downloads") },
            text = { Text("Deseja pausar todos os downloads em andamento?") },
            confirmButton = {
                Button(
                    onClick = {
                        downloadController.pauseAllDownloads()
                        showClearConfirmDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = StatusWarningAmber)
                ) {
                    Text("Pausar")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearConfirmDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showDeleteAllDownloadsDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteAllDownloadsDialog = false },
            title = { Text("Excluir Todos os Downloads") },
            text = { Text("Deseja realmente remover todos os ${completedDownloads.size} itens baixados e liberar espaço no dispositivo?") },
            confirmButton = {
                Button(
                    onClick = {
                        completedDownloads.forEach {
                            downloadController.removeDownload(it.id)
                        }
                        diskRefreshTrigger++
                        showDeleteAllDownloadsDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                ) {
                    Text("Excluir Todos")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteAllDownloadsDialog = false }) {
                    Text("Cancelar")
                }
            }
        )
    }

    if (showTermsDialog) {
        AlertDialog(
            onDismissRequest = { showTermsDialog = false },
            title = { Text("Aviso Legal e Termos de Uso") },
            text = {
                Text(
                    "1. Este aplicativo é um reprodutor de mídia (player) de propósito geral.\n\n" +
                    "2. Ele não fornece, distribui nem hospeda nenhuma lista de reprodução, canal, filme ou série.\n\n" +
                    "3. O usuário é única e exclusivamente responsável por carregar fontes e conteúdos que possua autorização legal para reproduzir ou baixar offline.\n\n" +
                    "4. O desenvolvedor não se responsabiliza pelo uso indevido do software ou de conteúdo protegido por direitos autorais."
                )
            },
            confirmButton = {
                Button(onClick = { showTermsDialog = false }) {
                    Text("Entendido")
                }
            }
        )
    }
}
