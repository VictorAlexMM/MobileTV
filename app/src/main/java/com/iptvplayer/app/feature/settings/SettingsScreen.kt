package com.iptvplayer.app.feature.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.SmartDisplay
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvplayer.app.core.datastore.SettingsRepository
import com.iptvplayer.app.core.designsystem.theme.DarkSurfaceVariant
import com.iptvplayer.app.core.designsystem.theme.PrimaryIndigo
import com.iptvplayer.app.core.designsystem.theme.StatusWarningAmber
import com.iptvplayer.app.core.designsystem.theme.TextMutedDark
import com.iptvplayer.app.feature.downloads.DownloadController
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    settingsRepository: SettingsRepository,
    downloadController: DownloadController,
    modifier: Modifier = Modifier
) {
    val offlineMode by settingsRepository.offlineModeEnabled.collectAsState(initial = false)
    val wifiOnly by settingsRepository.wifiOnlyDownloads.collectAsState(initial = true)
    val continueOffline by settingsRepository.continueDownloadsOffline.collectAsState(initial = false)
    val externalPlayer by settingsRepository.externalPlayerEnabled.collectAsState(initial = false)
    val scope = rememberCoroutineScope()

    var showTermsDialog by remember { mutableStateOf(false) }
    var showClearConfirmDialog by remember { mutableStateOf(false) }

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

        // Seção: Armazenamento
        item {
            Text(
                text = "Armazenamento",
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
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Diretório privado do app (Downloads)",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 14.sp
                    )
                    Text(
                        text = "Arquivos armazenados com segurança, acessíveis sem necessidade de internet.",
                        fontSize = 12.sp,
                        color = TextMutedDark
                    )

                    OutlinedButton(
                        onClick = { showClearConfirmDialog = true },
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(imageVector = Icons.Default.DeleteSweep, contentDescription = null)
                        Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                        Text("Pausar todos os downloads")
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
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
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
}
