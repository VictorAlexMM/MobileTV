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





