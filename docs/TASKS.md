# Plano de Tarefas (Tasks) - MobileTV Android Offline

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
- [x] **TASK-304**: Opção "Baixar temporada inteira" na fila de downloads.
- [x] **TASK-305**: Parser XMLTV streaming com [XmltvParser.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/epg/XmltvParser.kt) mapeado por `tvg-id`.
- [x] **TASK-306**: Exibição "Agora / Próximo" nos canais e grade horária completa.
- [x] **TASK-307**: Limpeza automática de dados de EPG expirados (>24h).

---

## Fase 4 — Gravações de Lives & Recursos Avançados

- [x] **TASK-401**: Gravação imediata de canal ao vivo com duração definida.
- [x] **TASK-402**: Agendamento de gravação via EPG com WorkManager.
- [x] **TASK-403**: Gravador de streams com persistência de `RecordingEntity` e listagem na aba Downloads.
- [x] **TASK-404**: Suporte a Picture-in-Picture (PiP).
- [x] **TASK-405**: Suporte a player externo (VLC / MX Player via Intent).
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
- [x] **TASK-606**: Refinamento da busca global e sincronização automática da playlist ativa no primeiro carregamento.

---

## Fase 7 — Detalhes Ricos de Mídia, Visualização YouTube, Mute e Player Compacto Não-Intrusivo

- [x] **TASK-701**: Tela de detalhes de séries ([SeriesDetailScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesDetailScreen.kt)) com carregamento de temporadas, episódios, biografia/sinopse e agrupamento inteligente por [SeriesMetadataHelper.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesMetadataHelper.kt).
- [x] **TASK-702**: Barra de progressão estilo YouTube ([YouTubeProgressBar.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/YouTubeProgressBar.kt)) vermelha e proporcional no rodapé das thumbnails de episódios e pôsteres de filmes.
- [x] **TASK-703**: Opção "Marcado como visto" com toggle individual por episódio, persistência imediata no Room (`WatchProgressEntity`) e feedback instantâneo via Snackbar.
- [x] **TASK-704**: Carregamento de metadados ricos em filmes (VOD) com [MovieMetadataHelper.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/MovieMetadataHelper.kt) e [MovieDetailsSheet.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/MovieDetailsSheet.kt): biografia, data de lançamento, gênero/estilo (terror, ação, comédia, ficção, etc.).
- [x] **TASK-705**: Player compacto não-intrusivo ([CompactPlayerView.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/CompactPlayerView.kt)) que inicia no topo da tela em proporção 16:9 sem cobrir o conteúdo e botão dedicado para expansão para tela cheia.
- [x] **TASK-706**: Controle de áudio mudo/desmudo (`isMuted`, `toggleMute`) integrado no [PlayerEngine.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerEngine.kt), [CompactPlayerView.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/CompactPlayerView.kt) e [PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt).
- [x] **TASK-707**: Compilação e deploy direto no smartphone conectado (`SM-S918B`) via `adb install -r -t` para testes físicos autônomos sem necessidade de Android Studio.

---

## Fase 8 — Resolução Precisa de Séries e Isolamento Estrito de Episódios (M3U & Xtream Codes)

- [x] **TASK-801**: Implementação de endpoint `getSeriesInfo` em [XtreamApiService.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/parser/xtream/XtreamApiService.kt) para consulta da action `get_series_info` da API Xtream Codes retornando detalhes completos de temporadas e episódios.
- [x] **TASK-802**: Adição de query Room especializada em [ChannelDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/ChannelDao.kt) (`getSeriesEpisodes`) para buscar episódios por correspondência no nome da série ou grupo da playlist.
- [x] **TASK-803**: Motor aprimorado de correspondência e limpeza em [SeriesMetadataHelper.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesMetadataHelper.kt):
  - Limpeza de tags e prefixos `[BR]`, `(DUB)`, `SÉRIES |`, etc.
  - Limites de palavra (`\b`) para tokens de temporada/episódio (`S01`, `T01`, `1x01`, `Temp 1`, `Episódio 1`), evitando cortes em títulos como `Stranger Things`.
  - Comparação estrita anti-mistura (`isMatchingSeries`) que descarta automaticamente episódios de séries aleatórias da mesma categoria.
- [x] **TASK-804**: Parser JSON robusto e resiliente [SeriesMetadataHelper.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesMetadataHelper.kt)`::parseXtreamSeriesInfo` para extrair sinopses, capas, temporadas e URLs de stream individuais reproduzíveis (`/series/user/pass/id.ext`).
- [x] **TASK-805**: Método unificado `loadSeriesDetails` em [ChannelRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/ChannelRepository.kt) e overlay de carregamento elegante em [SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt) (`isLoadingSeriesDetails`).
- [x] **TASK-806**: Cobertura completa de testes unitários em [SeriesMetadataHelperTest.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/test/java/com/iptvplayer/app/feature/series/SeriesMetadataHelperTest.kt) (100% de aprovação).
- [x] **TASK-807**: Compilação e instalação direta no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 9 — Busca Contextual por Categoria, Barra de Progresso no Mini Player, Rotação Horizontal Automática e Filtro Hamburger

- [x] **TASK-901**: Integração de busca contextual estrita via [ChannelRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/ChannelRepository.kt)`::getChannelsFilteredPaged` em [SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt), [LiveScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/LiveScreen.kt) e [VodScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/VodScreen.kt) permitindo buscar estritamente dentro da categoria selecionada.
- [x] **TASK-902**: Barra de progresso interativa no mini player ([CompactPlayerView.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/CompactPlayerView.kt)) com timestamps formatados (`formatCompactDuration`), seekbar funcional e linha vermelha permanente no rodapé ([YouTubeProgressBar.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/YouTubeProgressBar.kt)).
- [x] **TASK-903**: Rotação horizontal automática ao entrar em tela cheia via `DisposableEffect` e `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` em [PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt), restaurando a orientação original portrait ao sair.
- [x] **TASK-904**: Botão de filtro por categoria posicionado exclusivamente ao lado da barra de busca com ícone de menu hambúrguer (`Icons.Default.Menu`) e placeholder dinâmico, removendo o carrossel horizontal redundante de chips.
- [x] **TASK-905**: Remoção do botão e banner de download externo na lista de séries, mantendo o download exclusivamente dentro da tela de detalhes da série ([SeriesDetailScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesDetailScreen.kt)).
- [x] **TASK-906**: Testes unitários 100% aprovados, compilação do APK e deploy no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 10 — Filtro de Favoritos (Canais, Filmes e Séries) e Prevenção de Saída Indesejada via Botão de Voltar do Celular

- [x] **TASK-1001**: Atualização do Room DAO ([ChannelDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/ChannelDao.kt)) e do repositório ([ChannelRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/ChannelRepository.kt)) para suportar `onlyFavorites: Boolean` em `getChannelsFilteredPaged` (`:onlyFavorites = 0 OR isFavorite = 1`) e contagem via `getFavoriteCount`.
- [x] **TASK-1002**: Adição de botão e filtro de favoritos nas 3 seções do app ([LiveScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/LiveScreen.kt), [VodScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/VodScreen.kt) e [SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt)):
  - Botão de coração dedicado ao lado do campo de busca com destaque visual ativo.
  - Opção "Somente Favoritos" no menu hambúrguer de categorias (`CategoryBottomSheet`).
  - Placeholder dinâmico e mensagem de estado vazio personalizada para favoritos.
- [x] **TASK-1003**: Resolução definitiva do bug do botão de voltar físico/gesto do celular (`BackHandler`):
  - Saída do player fullscreen de volta para a tela principal em vez de fechar o app ([PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt)).
  - Fechamento da tela de episódios/temporadas em vez de fechar o app ([SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt)).
  - Fechamento do modal de detalhes do filme em vez de fechar o app ([VodScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/VodScreen.kt)).
  - Fechamento de sheets de categorias, limpeza de busca e retorno à categoria padrão em camadas lógicas.
  - Retorno à aba inicial de Canais a partir de abas secundárias em vez de fechar o app ([MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt)).
  - Confirmação de saída com duplo clique e Toast protetivo ("Pressione voltar novamente para sair") na tela raiz.
- [x] **TASK-1004**: Validação de testes unitários (`testDebugUnitTest`), compilação do APK e deploy direto no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 11 — Correção do Download Travado em 0% e Animação de Download Estilo Netflix

- [x] **TASK-1101**: Diagnóstico e resolução do congelamento de downloads em 0%:
  - Ativação de serviço de primeiro plano estrito (`foreground = true`) em `DownloadService.sendAddDownload` no [DownloadController.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadController.kt).
  - Implementação de loop ativo de polling (`startProgressPolling`) a cada 500ms lendo bytes e percentuais do `DownloadManager` e sincronizando com o Room em tempo real.
  - Liberação de requisitos de rede restritivos (`Requirements(Requirements.NETWORK)`) e chamada a `resumeDownloads()` em [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt).
  - Configuração de `followRedirects(true)`, `followSslRedirects(true)` e header `User-Agent` de player em [NetworkClient.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/network/NetworkClient.kt) e [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt) prevenindo bloqueios 403 de provedores IPTV.
  - Mapeamento explícito de MIME Types (`VIDEO_MP4`, `VIDEO_MATROSKA`, `VIDEO_MP2T`, `APPLICATION_M3U8`, `APPLICATION_MPD`).
- [x] **TASK-1102**: Criação do componente [NetflixDownloadButton.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/NetflixDownloadButton.kt):
  - Animação de início de download inspirada na Netflix: spinner contínuo de rotação suave e seta deslizando para dentro da bandeja.
  - Anel de progresso circular dinâmico desenhado via Canvas com preenchimento em tempo real e ícone central de parada/pausa.
  - Animação elástica (pop spring) para o estado concluído com checkmark verde esmeralda (`#10B981`).
  - Tratamento interativo com suporte a pausar, retomar e feedback de status.
- [x] **TASK-1103**: Integração do `NetflixDownloadButton` nos episódios de séries ([SeriesDetailScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesDetailScreen.kt)) e nos cards de filmes ([ChannelCard.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/designsystem/components/ChannelCard.kt)).
- [x] **TASK-1104**: Testes unitários 100% aprovados, compilação do APK e deploy no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 12 — Correção da Abertura/Reprodução de Mídias Baixadas e Validação Estrita de Conclusão

- [x] **TASK-1201**: Refatoração do `CacheDataSource.Factory` em [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt):
  - Injeção obrigatória do `User-Agent` de player no upstream factory para evitar HTTP 403 Forbidden em servidores IPTV.
  - Implementação de upstream dinâmico que valida permissão de rede em runtime (`networkGate.canAccessNetwork()`) sem travar estaticamente no startup.
  - Eliminação de flags negativas (`inv()`) inválidas que quebravam o `CacheDataSource`.
  - Fallback resiliente para diretório de cache (`appCtx.filesDir`) caso `getExternalFilesDir(null)` seja nulo.
- [x] **TASK-1202**: Reprodução oficial de mídias baixadas via `toMediaItem()` em [PlayerEngine.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerEngine.kt):
  - Uso de `download.request.toMediaItem()` para downloads concluídos com bytes válidos no disco, herdando `streamKeys`, MIME type e cache keys exatas.
  - Vinculação de `DefaultMediaSourceFactory(context.applicationContext)` para detecção de codecs locais.
  - Resolução precisa de MIME types (`MP4`, `MKV`, `TS`, `HLS`, `DASH`) para streaming online e fallback.
- [x] **TASK-1203**: Validação estrita de bytes no [DownloadController.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadController.kt):
  - Prevenção contra falsos downloads concluídos com 0 bytes (quando servidores encerram a conexão prematuramente sem dados).
  - Marcação desses casos como `DownloadStatus.FAILED` com feedback informativo em vez de `COMPLETED`.
  - Configuração de `customCacheKey = sourceUrl` no `DownloadRequest.Builder`.
- [x] **TASK-1204**: Ação imediata de reprodução no botão da Netflix em [SeriesDetailScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesDetailScreen.kt) ao clicar em downloads já concluídos (`DownloadStatus.COMPLETED`).
- [x] **TASK-1205**: Validação com testes unitários 100% aprovados, compilação do APK (`assembleDebug`) e instalação bem-sucedida no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 13 — Reprodução 100% Offline Garantida e Encerramento Imediato do Reprodutor ao Sair

- [x] **TASK-1301**: Implementação de `buildOfflinePlaybackDataSourceFactory` com `upstream = null` em [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt) garantindo que mídias baixadas não façam nenhuma checagem ou requisição de rede.
- [x] **TASK-1302**: Seleção de fonte de mídia inteligente no [PlayerEngine.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerEngine.kt):
  - Uso de `offlineMediaSourceFactory` quando `isOfflinePlayback == true`, lendo estritamente do cache local.
  - Uso de `onlineMediaSourceFactory` apenas quando streaming online.
  - Implementação de `playerEngine.stop()` descarregando completamente mídias e parando os loops de progresso.
- [x] **TASK-1303**: Encerramento e fechamento total do reprodutor ao sair da tela de reprodução em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt) e [PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt):
  - `onBackClick` na tela cheia fecha o player (`isPlayingFullscreen = false`, `isCompactPlayerVisible = false`) e chama `playerEngine.stop()`.
  - `onDispose` de `PlayerScreen` chama `playerEngine.stop()`.
  - `onClosePlayer` do `CompactPlayerView` descarrega a mídia com `stop()`.
- [x] **TASK-1304**: Validação completa com testes unitários aprovados (`testDebugUnitTest`), compilação do APK (`assembleDebug`) e instalação via ADB no Samsung Galaxy S23 Ultra (`SM-S918B`).

---

## Fase 14 — Exibição e Acesso ao Local de Armazenamento de Downloads & Explorador de Itens em Configurações

- [x] **TASK-1401**: Exposição da infraestrutura de arquivos e diretórios de downloads em [DownloadModule.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/downloads/DownloadModule.kt):
  - Implementação de `getDownloadDirectory(context)` retornando o diretório seguro (`getExternalFilesDir(null)/downloads`).
  - Implementação de `getDownloadDirSizeBytes(context)` calculando recursivamente o espaço total ocupado pelos fragmentos de mídia baixados.
  - Implementação de `getDownloadedFilesList(context)` listando os arquivos reais presentes em disco com datas de modificação e tamanhos.
- [x] **TASK-1402**: Criação do utilitário de armazenamento [StorageUtils.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/common/StorageUtils.kt):
  - Formatação precisa de bytes (`formatBytes`) em B, KB, MB, GB, TB.
  - Formatação de data/hora (`formatDate`).
  - Cálculo de espaço livre e total em disco (`getAvailableStorageBytes`).
  - Cópia segura de caminho para o `ClipboardManager` com Toast de confirmação.
  - Disparo de intents de gerenciador de arquivos nativo (`openFolderInFileManager`) com suporte ao Samsung My Files, DocumentsUI e FileProvider.
- [x] **TASK-1403**: Configuração de segurança com `FileProvider` e [file_paths.xml](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/res/xml/file_paths.xml):
  - Declaração de `androidx.core.content.FileProvider` em [AndroidManifest.xml](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/AndroidManifest.xml) com rotas para diretórios internos e externos.
- [x] **TASK-1404**: Reformulação da seção de Armazenamento em [SettingsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/settings/SettingsScreen.kt):
  - Painel com caminho absoluto visível em tipografia monoespaçada com borda escura.
  - Botão "Copiar Caminho" (`ContentCopy`) para área de transferência.
  - Botão "Acessar Pasta" (`FolderOpen`) para abrir o gerenciador de arquivos nativo do aparelho (com modal explicativo caso haja restrições do Android).
  - Cards de métricas com espaço ocupado pelos downloads, espaço livre no celular e total de itens.
  - Explorador integrado com abas "Mídias Salvas" (com poster, título, data, tamanho, botão reproduzir offline direto e botão excluir) e "Arquivos Físicos" (listando fragmentos e arquivos reais do disco).
  - Diálogos para confirmação de exclusão em massa e ajuda de acesso à pasta.
- [x] **TASK-1405**: Conexão da navegação em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt):
  - Injeção de `database` e callback `onPlayOfflineItem` permitindo tocar mídias offline diretamente da tela de Configurações.
- [x] **TASK-1406**: Validação rigorosa com testes unitários (`testDebugUnitTest` 100% aprovados) e compilação do APK (`assembleDebug` bem-sucedido).

---

## Fase 15 — Busca Contextual por Categoria & Eliminação Definitiva de Duplicatas no Catálogo

- [x] **TASK-1501**: Expansão da query de busca em [ChannelDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/ChannelDao.kt):
  - Inclusão da cláusula `OR groupTitle LIKE '%' || :query || '%'` na query `getChannelsFilteredPaged` para buscar termos tanto no nome do canal quanto na categoria associada.
- [x] **TASK-1502**: Eliminação de itens duplicados na listagem em [ChannelDao.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/core/database/dao/ChannelDao.kt):
  - Inclusão de `GROUP BY LOWER(TRIM(name))` nas queries `getChannelsFilteredPaged`, `getChannelsPaged`, `getChannelsByCategoryPaged` e `searchChannelsPaged`.
  - Desempate determinístico com `ORDER BY name ASC, id ASC` para estabilizar o `PagingSource` e erradicar duplicatas resultantes de paginação.
  - Ajuste nas contagens de categorias em `getCategoriesWithCount` utilizando `COUNT(DISTINCT LOWER(TRIM(name)))`.
- [x] **TASK-1503**: Experiência de busca inteligente e atalhos de categoria em [LiveScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/live/LiveScreen.kt):
  - Atualização do placeholder para `"Buscar canais ou categorias..."`.
  - Exibição de chip ativo com botão `Close` ao selecionar uma categoria.
  - Carrossel horizontal de sugestões rápidas de categorias (`LazyRow`) que combinam com o termo digitado.
  - Campo de busca interna dentro do `CategoryBottomSheet` para filtragem ágil entre centenas de categorias.
  - Chaves estáveis no `LazyColumn` protegidas contra colisões.
- [x] **TASK-1504**: Paridade de recursos de busca e filtros em [VodScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/vod/VodScreen.kt):
  - Atualização de placeholder, chip de categoria ativa, carrossel de sugestões e busca de categorias no BottomSheet de Filmes.
- [x] **TASK-1505**: Paridade de recursos de busca e filtros em [SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt):
  - Implementação idêntica para o catálogo de Séries (chips, busca de categorias no modal e carrossel de sugestões).
- [x] **TASK-1506**: Validação rigorosa com testes unitários (`testDebugUnitTest` 100% aprovados), compilação do APK (`assembleDebug` bem-sucedido) e instalação via ADB no dispositivo de teste (`Success`).

---

## Fase 16 — Preservação da Página de Séries & Redução Contínua do Player (Modo Compacto)

- [x] **TASK-1601**: Reestruturação arquitetural de sobreposição em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt):
  - Substituição da alternância excludente (`if (isPlayingFullscreen) PlayerScreen else Scaffold`) por sobreposição em camada (`Box(Modifier.fillMaxSize())`). O `Scaffold` permanece montado sob a tela cheia, preservando intactos o estado da tela, listas, scroll e seleções.
- [x] **TASK-1602**: Hoisting de estado de detalhes de série em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt) e [SeriesScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/series/SeriesScreen.kt):
  - Elevação de `currentSeriesDetails` para o nível de `MainApp`, impedindo que `SeriesDetailScreen` seja descartada ou resetada para a lista geral de séries ao iniciar o player, sair da tela cheia ou reduzir para o modo compacto.
- [x] **TASK-1603**: Ajuste do fluxo de redução e fechamento em [PlayerScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/player/PlayerScreen.kt):
  - Remoção de `playerEngine.stop()` do `DisposableEffect(Unit)` de `PlayerScreen`, permitindo que o vídeo continue reproduzindo sem cortes ao reduzir para o `CompactPlayerView`.
  - Diferenciação nítida entre "Reduzir Reprodutor" (`FullscreenExit` / `onBackClick`, mantendo reprodução no mini player) e "Fechar Reprodutor" (`Close` / `onClosePlayer`, que descarrega a mídia).
- [x] **TASK-1604**: Hierarquia de `BackHandler`s do sistema em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt):
  - Primeiro toque no botão voltar em fullscreen sai para o player compacto na página da série (`isCompactPlayerVisible = true`).
  - Segundo toque no botão voltar na página da série retorna para o catálogo geral de séries.
  - Terceiro toque retorna para a aba de Canais.
- [x] **TASK-1605**: Validação completa com testes unitários (`testDebugUnitTest` 100% aprovados), compilação do APK (`assembleDebug` bem-sucedido) e instalação via ADB no Samsung Galaxy S23 Ultra (`RQCW1067EEB` - `Success`).

---

## Fase 17 — Tela de Início (Continuar Assistindo), Retomada Inteligente de Posição & Playlists em Configurações

- [x] **TASK-1701**: Criação da tela inicial dinâmica em [HomeScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/home/HomeScreen.kt):
  - Observação reativa de `watchProgressDao.getRecentlyWatched(30)` e resolução paralela de canais/mídias (`ChannelEntity`) e downloads offline (`DownloadEntity`).
  - Carrossel horizontal "Continuar Assistindo" com cards 16:9, miniatura, título, categoria/tipo, porcentagem de progresso e barra vermelha proporcional estilo YouTube (`YouTubeProgressBar`).
  - Botão de ação primária "Continuar" disparando o player exatamente em `item.positionMs`.
  - Botão de ação secundária "Reiniciar do Começo" (`startPositionMs = 0L`).
  - Opção rápida para "Remover do Histórico" via `database.watchProgressDao().deleteProgress(contentId)`.
  - Seções de apoio: "Acesso Rápido" (Canais, Filmes, Séries, Downloads), "Canais Favoritos" e "Pronto para Assistir Offline".
- [x] **TASK-1702**: Migração e centralização do gerenciador de playlists em [SettingsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/settings/SettingsScreen.kt):
  - Adição de card de destaque "Playlists & Provedores IPTV" no topo das configurações, exibindo o status da playlist ativa, total de listas cadastradas e botão "Gerenciar Playlists".
  - Implementação de sub-tela interna em overlay com cabeçalho de retorno e `BackHandler` integrado, encapsulando [PlaylistsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/playlists/PlaylistsScreen.kt) sem perda de funcionalidades (M3U, Xtream, Stalker, upload de arquivo local).
- [x] **TASK-1703**: Reestruturação da barra de navegação e suporte a `startPositionMs` em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt):
  - Substituição da aba `PLAYLISTS` por `HOME` (`Icons.Default.Home`), posicionando a tela de Início como o primeiro destino padrão do app.
  - Atualização de `handlePlay` para aceitar `startPositionMs: Long = 0L`, repassando para `playerEngine.playContent(..., startPositionMs = startPositionMs)`.
  - Ajuste de `BackHandler`: Toque no botão voltar em qualquer aba secundária direciona para `HOME`; toque duplo em `HOME` confirma a saída da aplicação.
- [x] **TASK-1704**: Validação e verificação:
  - Testes unitários com `./gradlew testDebugUnitTest` 100% aprovados.
  - Compilação do APK de release/debug via `./gradlew assembleDebug`.
  - Instalação no Samsung Galaxy S23 Ultra (`RQCW1067EEB`) via ADB.

---

## Fase 18 — Sincronização Automática ao Abrir o App Sem Apagar o Cache (Últimos Lançamentos)

- [x] **TASK-1801**: Criação do método de sincronização incremental e não-destrutiva em [PlaylistRepository.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/playlists/PlaylistRepository.kt):
  - Implementação de `syncPlaylist(playlistId: Long)`: busca novos canais, filmes VOD e séries sem executar `deleteChannelsByPlaylist` ou `replacePlaylistChannels`.
  - Preservação integral do cache existente e funcionamento ininterrupto da interface enquanto o sync processa em background.
  - Preservação estrita dos canais favoritos do usuário via `getFavoriteChannelIds(playlistId)`.
  - Inserção em lotes seguros de 500 itens (`chunked(500)`) via `insertChannels(batch)` com `OnConflictStrategy.REPLACE`, inserindo novos lançamentos e atualizando itens existentes.
  - Atualização do timestamp de sincronização `updateSyncTime` em `PlaylistDao`.
  - Exposição de estados reativos `isSyncing: StateFlow<Boolean>` e `syncStatus: StateFlow<String?>`.
- [x] **TASK-1802**: Disparo automático na inicialização da aplicação em [MainApp.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/ui/MainApp.kt):
  - Adição de `LaunchedEffect(activePlaylistId, isAppOffline)` com flag persistente de ciclo de vida `rememberSaveable { mutableStateOf(false) }`, disparando o auto-sync em `Dispatchers.IO` assim que o app é aberto e a playlist ativa é identificada.
  - Blindagem contra falhas: se estiver offline, cancela silenciosamente sem erros; se a conexão oscilar, o cache existente continua 100% acessível.
- [x] **TASK-1803**: Feedback visual e controle manual em [HomeScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/home/HomeScreen.kt) e [SettingsScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/settings/SettingsScreen.kt):
  - Botão de sincronização manual com rotação contínua no banner de boas-vindas da tela inicial.
  - Barra de progresso elegante `AnimatedVisibility(isSyncing)` mostrando o status da busca de últimos lançamentos.
  - Botão `OutlinedButton` "Atualizar Lançamentos (Última: dd/MM HH:mm)" no card de playlists das Configurações.
- [x] **TASK-1804**: Validação, build e deploy:
  - Testes unitários com `./gradlew testDebugUnitTest` 100% aprovados.
  - Compilação do APK de release/debug via `./gradlew assembleDebug`.
  - Instalação via ADB no dispositivo Samsung Galaxy S23 Ultra (`RQCW1067EEB`).

---

## [Fase 19] Player Minimalista na Tela Inicial e Otimização Visual
- [x] **TASK-1901**: Remoção do estado vazio volumoso e do card gigante na tela inicial (`HomeScreen.kt`):
  - Eliminação do card cinza gigante de estado vazio com ícone de 48dp e múltiplos botões quando não houver itens em andamento, permitindo que a tela inicial se mantenha limpa e foque diretamente nos atalhos de categorias e favoritos.
  - Ocultação inteligente da seção "Continuar Assistindo" quando a lista estiver vazia.
- [x] **TASK-1902**: Criação do componente `MinimalContinueWatchingCard` (~66dp de altura):
  - Card horizontal compacto ocupando a largura útil com bordas arredondadas e contorno sutil (`Color.White.copy(alpha = 0.06f)`).
  - Miniatura 16:9 compacta (82dp x 48dp) com overlay escuro suave, ícone play centralizado e a barra vermelha do YouTube (`YouTubeProgressBar`) no rodapé da imagem.
  - Informações de título, subtítulo da categoria e progresso/duração formatados de maneira limpa e hierárquica.
  - Exibição vertical limpa e organizada dos itens mais recentes (`take(3)`), evitando quebras de rolagem horizontal e mantendo os botões sempre à mão.
- [x] **TASK-1903**: Preservação estrita de todas as ações de reprodução e histórico:
  - **Continuar de onde parou**: Botão circular de destaque em `PrimaryIndigo` com `Icons.Default.PlayArrow`, iniciando o player com `startPositionMs = item.positionMs`. Tocar no próprio card também retoma a reprodução de onde parou.
  - **Reiniciar do começo**: Botão circular compacto com `Icons.Default.Replay` (`onRestartClick`), iniciando a reprodução a partir de `0L`.
  - **Apagar do histórico**: Botão circular compacto com `Icons.Default.Close` (`onRemoveClick`), invocando `database.watchProgressDao().deleteProgress(item.contentId)` e exibindo feedback via snackbar.
- [x] **TASK-1904**: Modernização do banner de boas-vindas:
  - Redução de padding excessivo e harmonização das cores para um estilo minimalista e integrado com o tema escuro.
- [x] **TASK-1905**: Validação com testes unitários (`testDebugUnitTest` 100% aprovados) e compilação do APK de depuração (`assembleDebug` concluído com sucesso).
- [x] **TASK-1906**: Ajuste fino de espaçamento dos botões em [HomeScreen.kt](file:///c:/Users/victor.mota/Desktop/New/app/app/src/main/java/com/iptvplayer/app/feature/home/HomeScreen.kt):
  - Remoção do botão de play redundante na lateral direita do card, pois o toque em qualquer área do card já dispara a retomada da reprodução.
  - Redesenho dos botões de Reiniciar (`Replay`) e Apagar (`Close`) utilizando containers circulares de 32dp sem padding fantasma de 48dp, liberando espaço amplo para título, categoria e tempo restante.
  - Verificação e aprovação visual direta no Samsung Galaxy S23 Ultra via captura de tela ADB.










