# Plano de Tarefas (Tasks) - IPTV Player Android Offline

> Acompanhamento de execução detalhado baseado na especificação [IPTV_Android_Offline_Spec.md](file:///c:/Users/victor.mota/Desktop/New/app/docs/IPTV_Android_Offline_Spec.md).

---

## Legenda de Status
- `[ ]` Não iniciado (Pending)
- `[/]` Em andamento (In Progress)
- `[x]` Concluído (Done)
- `[!]` Impedimento / Atenção (Blocked / Alert)

---

## Fase 1 — MVP (Projeto Base, Arquitetura, Playlists & Player Online)

### 1.1 Configuração do Ambiente e Inicialização do Projeto
- [x] **TASK-101**: Leitura e análise da especificação técnica completa (`IPTV_Android_Offline_Spec.md`).
- [x] **TASK-102**: Configuração da ferramenta Android CLI (`android.exe`) e identificação do JDK 21 / Android SDK 36.
- [x] **TASK-103**: Criação dos documentos de governança (`TASKS.md`, `DECISIONS.md`, `CHECKPOINTS.md`).
- [x] **TASK-104**: Inicialização do projeto Android nativo (Compose Material 3, minSdk 24, Kotlin).
- [x] **TASK-105**: Configuração do catálogo de versões `gradle/libs.versions.toml` com todas as dependências especificadas (Media3, Room, Retrofit, OkHttp, Coil, Paging 3, WorkManager, DataStore, Security Crypto).
- [x] **TASK-106**: Configuração dos plugins Gradle e compilação do APK de depuração validada com sucesso (`assembleDebug`).

### 1.2 Arquitetura Base & Estrutura de Módulos / Pacotes
- [x] **TASK-107**: Estruturação dos pacotes modulares e clean architecture:
  - `core/common` ([DispatcherProvider.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/common/DispatcherProvider.kt), [Result.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/common/Result.kt), AppException)
  - `core/database` ([AppDatabase.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/AppDatabase.kt), entities, DAOs, TypeConverters)
  - `core/network` ([NetworkGate.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/NetworkGate.kt), [OfflineModeInterceptor.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/OfflineModeInterceptor.kt), [ConnectivityObserver.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/ConnectivityObserver.kt), [NetworkClient.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/NetworkClient.kt))
  - `core/datastore` ([SettingsRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/datastore/SettingsRepository.kt))
  - `core/designsystem` ([Color.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/theme/Color.kt), [Theme.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/theme/Theme.kt), [OfflineBanner.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/OfflineBanner.kt), [ChannelCard.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/ChannelCard.kt), [DownloadBadge.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/DownloadBadge.kt), [GoOnlineDialog.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/GoOnlineDialog.kt))
  - `feature/playlists` ([PlaylistRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/playlists/PlaylistRepository.kt), [PlaylistsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/playlists/PlaylistsScreen.kt))
  - `feature/live` ([ChannelRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/ChannelRepository.kt), [LiveScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/LiveScreen.kt))
  - `feature/vod` ([VodScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/VodScreen.kt))
  - `feature/downloads` ([DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt), [AppDownloadService.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/AppDownloadService.kt), [DownloadController.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadController.kt), [DownloadsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadsScreen.kt))
  - `feature/player` ([PlayerEngine.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerEngine.kt), [PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt))
  - `feature/settings` ([SettingsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/settings/SettingsScreen.kt))
  - `parser/m3u` ([M3UParser.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/m3u/M3UParser.kt))
  - `parser/xtream` ([XtreamModels.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/xtream/XtreamModels.kt), [XtreamApiService.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/xtream/XtreamApiService.kt))
  - `parser/epg` ([XmltvParser.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/epg/XmltvParser.kt))
- [x] **TASK-108**: Configuração e injeção de dependência orquestrada no [MainActivity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/MainActivity.kt) e ciclo de vida com CoroutineScope.

### 1.3 Camada de Dados (Room Database)
- [x] **TASK-109**: Definição das entidades Room conforme especificação (Seção 4):
  - [PlaylistEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/PlaylistEntity.kt)
  - [ChannelEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/ChannelEntity.kt)
  - [DownloadEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/DownloadEntity.kt)
  - [RecordingEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/RecordingEntity.kt)
  - [EpgProgramEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/EpgProgramEntity.kt)
  - [WatchProgressEntity.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/entity/WatchProgressEntity.kt)
- [x] **TASK-110**: Criação dos DAOs ([PlaylistDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/PlaylistDao.kt), [ChannelDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/ChannelDao.kt), [DownloadDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/DownloadDao.kt), [RecordingDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/RecordingDao.kt), [EpgDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/EpgDao.kt), [WatchProgressDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/WatchProgressDao.kt)).
- [x] **TASK-111**: Configuração do [AppDatabase.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/AppDatabase.kt) com índices de alta performance para 50k+ canais e [AppConverters.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/AppConverters.kt).

### 1.4 Parser M3U & Gestão de Playlists
- [x] **TASK-112**: Implementação do `M3UParser` em streaming bufferizado linha por linha para suportar 50k+ canais sem estourar memória.
- [x] **TASK-113**: Repositório de playlists (`PlaylistRepository`) com inserção em lote (chunks de 500 canais) no Room.
- [x] **TASK-114**: Telas de gestão de playlists ([PlaylistsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/playlists/PlaylistsScreen.kt)) com adição via URL M3U e conta Xtream Codes.
- [x] **TASK-115**: Validação e tratamento de erros de importação com [AppException.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/common/Result.kt).

### 1.5 Navegação, Categorias, Busca e Favoritos
- [x] **TASK-116**: Estrutura de navegação principal ([MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt)) com Material 3 NavigationBar: Ao Vivo, Filmes, Downloads, Playlists, Ajustes.
- [x] **TASK-117**: Listagem de canais agrupados por categoria com paginação ([ChannelRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/ChannelRepository.kt) + Paging 3).
- [x] **TASK-118**: Busca global de canais e filmes com campo dedicado.
- [x] **TASK-119**: Sistema de favoritos persistidos no Room com toggle instantâneo.

### 1.6 Player Básico (Media3 ExoPlayer)
- [x] **TASK-120**: Integração do `androidx.media3.exoplayer` (HLS, DASH, MP4, TS).
- [x] **TASK-121**: Tela do Player Compose ([PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt)) com controles overlay (play/pause, seek, áudio, legendas, fullscreen, re-seek 10s).
- [x] **TASK-122**: Registro de progresso de reprodução (`WatchProgressEntity`) periódico a cada 10s compartilhado entre streaming e offline.

---

## Fase 2 — Offline & Downloads (Núcleo do Diferencial)

### 2.1 Media3 DownloadManager & Serviço
- [x] **TASK-201**: Implementação do `DownloadCache` (SimpleCache com NoOpCacheEvictor e DatabaseProvider em [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt)).
- [x] **TASK-202**: Configuração do `DownloadManager` com limite configurável de downloads paralelos (padrão 3).
- [x] **TASK-203**: Implementação do [AppDownloadService.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/AppDownloadService.kt) em primeiro plano com notificação de progresso e canal registrado no [AndroidManifest.xml](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/AndroidManifest.xml).
- [x] **TASK-204**: `CacheDataSourceFactory` configurado para reprodução automática via cache offline ou upstream (DownloadModule).

### 2.2 Toggle "Ficar Offline" & NetworkGate
- [x] **TASK-205**: Criação do [NetworkGate.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/NetworkGate.kt) combinando conectividade real e toggle manual persistido em DataStore.
- [x] **TASK-206**: [OfflineModeInterceptor.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/OfflineModeInterceptor.kt) no OkHttp bloqueando requisições remotas quando em modo offline manual.
- [x] **TASK-207**: Banner e indicador fixo de "Modo Offline" na Home, Player e TopBar com toggle rápido.
- [x] **TASK-208**: Interceptação no Player/Cards: tocar item não baixado em modo offline exibe [GoOnlineDialog.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/GoOnlineDialog.kt) ("Deseja ficar online?").
- [x] **TASK-209**: Pausar/retomar downloads e sincronizações automáticas com base no estado do `NetworkGate`.

### 2.3 Gerenciador de Downloads (UI & Lógica)
- [x] **TASK-210**: Botão "Baixar" em detalhes de VOD com indicador de status (baixando, baixado, pausado, erro).
- [x] **TASK-211**: Tela de Downloads ([DownloadsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadsScreen.kt)): abas (Em andamento, Concluídos, Gravações).
- [x] **TASK-212**: Ações de pausar, retomar, cancelar e excluir arquivos físicos e registros no Room via [DownloadController.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadController.kt).
- [x] **TASK-213**: Verificação de espaço livre antes de iniciar download (mínimo 200MB) e gestão de armazenamento.
- [x] **TASK-214**: Cache de capas e pôsteres para navegação offline via Coil.

---

## Fase 3 — Xtream Codes API & EPG

- [x] **TASK-301**: Implementação do cliente HTTP Xtream Codes (`player_api.php`, categorias, streams live/vod/series, xmltv).
- [x] **TASK-302**: Armazenamento seguro de credenciais Xtream usando Android Keystore / EncryptedSharedPreferences.
- [x] **TASK-303**: Telas e detalhes de Filmes (VOD) com navegação e reprodução direta por cache.
- [/] **TASK-304**: Opção "Baixar temporada inteira" na fila de downloads.
- [x] **TASK-305**: Parser XMLTV streaming com [XmltvParser.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/epg/XmltvParser.kt) mapeado por `tvg-id`.
- [x] **TASK-306**: Exibição "Agora / Próximo" nos canais e grade horária completa.
- [x] **TASK-307**: Limpeza automática de dados de EPG expirados (>24h).

---

## Fase 4 — Gravações de Lives & Recursos Avançados

- [/] **TASK-401**: Gravação imediata de canal ao vivo com duração definida.
- [/] **TASK-402**: Agendamento de gravação via EPG com WorkManager.
- [x] **TASK-403**: Gravador de streams com persistência de `RecordingEntity` e listagem na aba Downloads.
- [ ] **TASK-404**: Suporte a Picture-in-Picture (PiP).
- [ ] **TASK-405**: Suporte a player externo (VLC / MX Player via Intent).
- [x] **TASK-406**: Gestão avançada de disco com alerta de limite configurável.

---

## Fase 5 — Polimento, Auditoria, Testes & Conformidade

- [x] **TASK-501**: Termos de Uso e aviso legal no primeiro acesso (onboarding e configurações).
- [x] **TASK-502**: Internacionalização base (pt-BR em `strings.xml`).
- [x] **TASK-503**: Testes unitários para [M3UParserTest.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/test/java/com/iptvplayer/app/parser/M3UParserTest.kt) e [NetworkGateTest.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/test/java/com/iptvplayer/app/network/NetworkGateTest.kt) (100% passando).
- [x] **TASK-504**: Testes dos fluxos de offline manual e online permitidos.
- [x] **TASK-505**: Validação final de build (`assembleDebug` gerando `app-debug.apk` com 27MB).

---

## Fase 6 — Melhorias UI/UX, Ícone, Separação Estrita (Ao Vivo / Filmes / Séries) & Base IPTVnator

- [x] **TASK-601**: Novo Ícone oficial do aplicativo gerado a partir de `docs/icon.jpg` em todas as densidades mipmap (mdpi, hdpi, xhdpi, xxhdpi, xxxhdpi) com suporte a adaptive icon e round icon.
- [x] **TASK-602**: Correção do popup de adição de Playlist: validação de campos obrigatórios, loading animado inline, fechamento automático após conclusão e feedback instantâneo via Snackbar.
- [x] **TASK-603**: Configuração de Run no Android Studio (`.idea/runConfigurations/app.xml`) com `ALWAYS_INSTALL_WITH_PM` e `PM_INSTALL_OPTIONS = -r -t` para deploy automático no celular conectado.
- [x] **TASK-604**: Migração de compilação Room para KSP 2.3.12 compatível com Kotlin 2.3 e AGP 9.
- [x] **TASK-605**: Separação de conteúdo inspirada no IPTVnator (`4gray/iptvnator`):
  - Modelagem e categorização estrita em `ContentType.LIVE`, `ContentType.MOVIE` e `ContentType.SERIES`.
  - Queries Room (`ChannelDao`) com suporte a busca textual, filtro de categoria e playlist ativa sem vazamento de tipos entre abas.
  - Consulta de categorias com contadores por tipo de mídia (`CategoryCount`).
  - Nova aba e tela dedicada de Séries (`SeriesScreen.kt`).
  - Modal sheets minimalistas para seleção de categorias com visualização de quantidade de canais/mídias.
- [/] **TASK-606**: Refinamento da busca global e sincronização automática da playlist ativa no primeiro carregamento.
