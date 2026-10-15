# Pontos de Verificação (Checkpoints) - MobileTV Android Offline

> Registro de marcos e verificações formais de cada etapa de implementação, garantindo qualidade, aderência às regras do projeto e critérios de aceitação.

---

## Tabela Resumo de Checkpoints

| Checkpoint | Descrição | Status | Data de Validação |
|---|---|---|---|
| **CP-00** | Configuração do Ambiente, CLI e Documentos de Governança | **Aprovado** | 2026-10-14 |
| **CP-01** | Inicialização do Projeto Android, Gradle & Catálogo de Dependências | **Aprovado** | 2026-10-14 |
| **CP-02** | Camada de Dados (Entidades Room, DAOs, Banco e Migrações) | **Aprovado** | 2026-10-14 |
| **CP-03** | Camada de Rede & NetworkGate (Toggle "Ficar Offline" + Interceptor) | **Aprovado** | 2026-10-14 |
| **CP-04** | Parsers em Streaming (M3U / M3U8, Xtream Codes API, XMLTV EPG) | **Aprovado** | 2026-10-14 |
| **CP-05** | Motor de Download e Cache Offline (Media3 DownloadService + CacheDataSource) | **Aprovado** | 2026-10-14 |
| **CP-06** | Interface Compose & Navegação (Playlists, Player, VOD, Downloads, Configs) | **Aprovado** | 2026-10-14 |
| **CP-07** | Validação dos Critérios de Aceitação da Especificação (Online & Offline) | **Aprovado** | 2026-10-14 |
| **CP-08** | Ícone Oficial, Reinstalação Contínua ADB e Compatibilidade Room KSP | **Aprovado** | 2026-10-14 |
| **CP-09** | Separação de Conteúdo (Ao Vivo, Filmes, Séries) & Base IPTVnator | **Em Andamento** | 2026-10-14 |

---

### CP-00: Configuração do Ambiente, CLI e Governança
- **Objetivo**: Garantir que as especificações técnicas foram compreendidas, as ferramentas necessárias (Android CLI, JDK 21, Android SDK) estão disponíveis e os arquivos de rastreamento de tarefas, decisões e checkpoints foram estabelecidos.
- **Critérios de Validação**:
  - [x] Especificação `IPTV_Android_Offline_Spec.md` lida e mapeada.
  - [x] Android CLI instalado e funcional (`android.exe info`).
  - [x] Java JDK 21 disponível no Android Studio (`C:\Program Files\Android\Android Studio\jbr`).
  - [x] Android SDK localizado e verificado (`build-tools`, `platforms`).
  - [x] Documentos `TASKS.md`, `DECISIONS.md` e `CHECKPOINTS.md` criados.
- **Resultado**: APROVADO.

---

### CP-01: Inicialização do Projeto Android, Gradle & Dependências
- **Objetivo**: Inicializar o projeto Android nativo com Jetpack Compose, Material 3, minSdk 24, e configurar o catálogo `libs.versions.toml` com todas as bibliotecas requeridas.
- **Critérios de Validação**:
  - [x] Projeto criado com estrutura Gradle funcional (Gradle Wrapper, `settings.gradle.kts`, `build.gradle.kts`).
  - [x] `gradle/libs.versions.toml` configurado com versões compatíveis:
    - AndroidX Core, Lifecycle, Activity Compose
    - Compose BOM, Material 3, Foundation, Icons Extended
    - AndroidX Media3 (ExoPlayer, HLS, DASH, Ui, DataSource-OkHttp, Session)
    - Room (Runtime, KTX, Paging)
    - Paging 3 (Runtime, Compose)
    - Retrofit, OkHttp, Kotlinx Serialization
    - Coil Compose
    - WorkManager KTX
    - DataStore Preferences
    - Security Crypto
  - [x] Compilação de base bem-sucedida (`gradlew assembleDebug` gerando o APK com sucesso em 36 tasks).
- **Resultado**: APROVADO.

---

### CP-02: Camada de Dados (Room Database)
- **Objetivo**: Garantir que o esquema de dados do Room atenda fielmente à seção 4 da especificação com suporte a grandes volumes e persistência confiável.
- **Critérios de Validação**:
  - [x] `PlaylistEntity`, `ChannelEntity`, `DownloadEntity`, `RecordingEntity`, `EpgProgramEntity`, `WatchProgressEntity` implementadas com tipos corretos.
  - [x] Índices declarados para buscas rápidas (`playlistId`, `contentType`, `groupTitle`, `isFavorite`).
  - [x] DAOs criados com suporte a Coroutines `Flow` e `PagingSource`.
  - [x] Transações em lote (`withTransaction`) para inserção sem travamento da UI.
- **Resultado**: APROVADO.

---

### CP-03: Camada de Rede & NetworkGate (Toggle "Ficar Offline")
- **Objetivo**: Implementar o isolamento de rede manual e automático para o app funcionar 100% offline quando desejado.
- **Critérios de Validação**:
  - [x] `ConnectivityObserver` escutando eventos de rede via `ConnectivityManager.NetworkCallback`.
  - [x] `SettingsRepository` persistindo `OFFLINE_MODE` via DataStore.
  - [x] `NetworkGate` expondo `isOnlineAllowed: StateFlow<Boolean> = isConnected && !manualOffline`.
  - [x] `OfflineModeInterceptor` no OkHttpClient lançando exceção se `!isOnlineAllowed`.
  - [x] Validação: Testes unitários comprovam bloqueio de requisições quando em modo offline manual.
- **Resultado**: APROVADO.

---

### CP-04: Parsers em Streaming (M3U, Xtream, XMLTV)
- **Objetivo**: Processar listas massivas (50k+ canais) e guias EPG com baixo consumo de memória.
- **Critérios de Validação**:
  - [x] `M3UParser` processando arquivos linha a linha via streams sem carregar a lista completa na memória.
  - [x] `XtreamCodesClient` com endpoints de categorias, streams e autenticação.
  - [x] `XmltvParser` utilizando `XmlPullParser` para streaming de canais e programas.
  - [x] Testes unitários (`M3UParserTest`) validando parsing de canais e inferência de tipos.
- **Resultado**: APROVADO.

---

### CP-05: Motor de Download & Cache Offline (Media3)
- **Objetivo**: Implementar o serviço de download resiliente e reprodução unificada via `CacheDataSource`.
- **Critérios de Validação**:
  - [x] `SimpleCache` compartilhado configurado no diretório de downloads do app.
  - [x] `AppDownloadService` registrado no `AndroidManifest.xml` com foreground service type `dataSync`.
  - [x] Notificação de progresso com ações de pausar/cancelar.
  - [x] `CacheDataSource.Factory` configurado para ler direto do disco quando offline.
  - [x] Retomada automática após reinício do app ou reconexão de rede.
- **Resultado**: APROVADO.

---

### CP-06: Interface Compose & Telas
- **Objetivo**: Criar interface Material 3 moderna, dinâmica e responsiva.
- **Critérios de Validação**:
  - [x] NavigationBar com as seções principais: Ao Vivo, Filmes, Downloads, Playlists, Ajustes.
  - [x] Barra superior com Toggle "Ficar Offline" e `OfflineBanner` persistente quando ativo.
  - [x] Player Compose integrado com controles overlay, seekbar, seleção de faixas e botões de 10s.
  - [x] Diálogo `GoOnlineDialog` ("Deseja ficar online?") ao tentar reproduzir item não baixado em modo offline.
  - [x] Tema escuro e claro consistentes.
- **Resultado**: APROVADO.

---

### CP-07: Validação dos Critérios de Aceitação
- **Objetivo**: Validar todos os 6 critérios de aceitação listados na Seção 1.3.1 da especificação.
- **Critérios de Validação**:
  - [x] Com Wi-Fi/dados ligados, qualquer item toca em streaming sem precisar baixar.
  - [x] Ao ligar o toggle "Ficar offline", item baixado toca normalmente via `CacheDataSource`.
  - [x] Com o toggle ligado, zero requisições remotas são disparadas (bloqueadas pelo `OfflineModeInterceptor`).
  - [x] Ao desligar o toggle, o app reativa a rede sem reiniciar.
  - [x] Queda de conexão física ativa modo offline automático.
  - [x] Progresso compartilhado via `WatchProgressEntity` entre online e offline.
- **Resultado**: APROVADO.

---

### CP-08: Ícone Oficial, Reinstalação Contínua ADB e Compatibilidade Room KSP
- **Objetivo**: Fornecer identidade visual oficial via `docs/icon.jpg`, resolver crash de inicialização no Android Studio e habilitar reinstalação contínua nos testes físicos.
- **Critérios de Validação**:
  - [x] Ícone processado em todas as resoluções mipmap (`mdpi`, `hdpi`, `xhdpi`, `xxhdpi`, `xxxhdpi`) com ícone adaptativo e ícone redondo.
  - [x] Configuração no Android Studio (`.idea/runConfigurations/app.xml`) garantindo atualização automática a cada clique em Run.
  - [x] Resolução do crash de geração do Room KSP para Kotlin 2.3+.
  - [x] Instalação e execução validadas no Samsung Galaxy S23 Ultra (`samsung SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-09: Separação de Conteúdo (Ao Vivo, Filmes, Séries) & Base IPTVnator
- **Objetivo**: Isolar completamente fluxos de Canais Ao Vivo, Filmes (VOD) e Séries, com extração de categorias completas e busca contextual inspirada no IPTVnator.
- **Critérios de Validação**:
  - [x] Adição do tipo `ContentType.SERIES` e criação da tela dedicada `SeriesScreen.kt`.
  - [x] Extração de categorias em M3U (`group-title`, `tvg-group`, `#EXTGRP:`) e Xtream API (`getLiveCategories`, `getVodCategories`, `getSeriesCategories`).
  - [x] DAO `ChannelDao` estruturado com queries que isolam `contentType`, associam contadores por categoria e evitam vazamento de canais entre abas.
  - [x] Diálogo de playlists aprimorado com validação, indicador inline e confirmação visual.
- **Resultado**: APROVADO.

---

### CP-10: Recursos Avançados (PiP, Player Externo, Gravações Ao Vivo & Downloads de Temporadas)
- **Objetivo**: Expandir o player nativo com suporte a Picture-in-Picture e reprodutor externo, permitir gravação de fluxos ao vivo e agendamento de programas via EPG com WorkManager, e possibilitar download de temporadas em lote.
- **Critérios de Validação**:
  - [x] Picture-in-Picture (`supportsPictureInPicture="true"`, `enterPictureInPictureMode`, `onUserLeaveHint`, controles limpos no overlay).
  - [x] Despacho para reprodutor externo (VLC / MX Player) via `ExternalPlayerHelper` com toggle em Ajustes e botão na barra do player interno.
  - [x] Gravação de live stream com duração configurável (`15`, `30`, `60`, `120` min) via `LiveStreamRecordingWorker` em segundo plano salvando pacotes `.ts`.
  - [x] Agendamento de gravações futuras com base nos horários do EPG via `RecordingScheduler` e WorkManager.
  - [x] Reprodução e exclusão de gravações locais salvas na aba Downloads -> Gravações.
  - [x] Download em lote de temporadas inteiras de séries na fila de downloads com um clique.
  - [x] Testes unitários atualizados e 100% passando (`BUILD SUCCESSFUL`).
- **Resultado**: APROVADO.

---

### CP-11: Detalhes Ricos (Séries/Filmes), YouTube Progress Bar, Mute e Deploy no Celular Físico
- **Objetivo**: Integrar tela rica de séries com temporadas e episódios, modal de filmes com biografia e estilo, barra de progresso do YouTube, toggle "Marcado como visto", player compacto não-intrusivo com mute e deploy no dispositivo físico.
- **Critérios de Validação**:
  - [x] Tela de séries com carregamento de biografia, sinopse, seletor de temporadas e episódios agrupados.
  - [x] Detalhes de filmes com biografia, ano de lançamento, detecção de estilo/gênero (terror, ação, comédia, etc.).
  - [x] Barra de progresso visual estilo YouTube (linha vermelha `#FF0000`) nas thumbnails e cards.
  - [x] Opção "Marcado como visto" individual por episódio com gravação imediata no Room.
  - [x] Player compacto embutido no topo (16:9) que não força tela cheia ao iniciar e botão de expansão.
  - [x] Controle de reproduzir em mudo (`isMuted`, toggle) no player compacto e no fullscreen.
  - [x] Build bem-sucedido e APK instalado com sucesso no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-12: Resolução Estrita de Séries e Isolamento de Episódios (M3U & Xtream Codes)
- **Objetivo**: Corrigir a abertura de séries para carregar rigorosamente e exclusivamente os episódios pertencentes à série clicada, sem misturar episódios aleatórios da mesma categoria ou outras séries, suportando nativamente servidores Xtream Codes (`get_series_info`) e listas M3U.
- **Critérios de Validação**:
  - [x] Endpoint `XtreamApiService.getSeriesInfo` adicionado para consultar `action=get_series_info` da API Xtream Codes.
  - [x] Query Room `ChannelDao.getSeriesEpisodes` com busca canônica por título/grupo.
  - [x] Motor avançado de regex em `SeriesMetadataHelper` com limites de palavra (`\b`) prevenindo falsos positivos (ex: `Stranger Things`), suporte a `1x01` e filtro estrito anti-mistura (`isMatchingSeries`).
  - [x] Parser JSON `parseXtreamSeriesInfo` montando episódios com URLs de streaming reproduzíveis (`/series/user/pass/id.ext`).
  - [x] Resolução unificada em `ChannelRepository.loadSeriesDetails` com indicador de loading animado em `SeriesScreen` (`isLoadingSeriesDetails`).
  - [x] Testes unitários dedicados em `SeriesMetadataHelperTest` com 100% de sucesso.
  - [x] Compilação e instalação direta no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-13: Busca Contextual por Categoria, Progressão no Mini Player, Rotação Automática Fullscreen e Filtro Hamburger
- **Objetivo**: Confinar a busca textual estritamente dentro da categoria selecionada, enriquecer o mini player com barra de progresso interativa e timestamps, forçar rotação landscape automática no player em tela cheia, unificar o seletor de categorias no botão hambúrguer ao lado da busca e restringir os downloads de séries para o interior da série.
- **Critérios de Validação**:
  - [x] Busca textual em Ao Vivo, Filmes e Séries filtra estritamente pela categoria selecionada via `getChannelsFilteredPaged`.
  - [x] Placeholder da busca indica claramente o contexto (`Buscar em "$selectedCategory"...` ou `Buscar...`).
  - [x] Mini player (`CompactPlayerView`) com slider de progresso interativo, timestamps de tempo atual e duração total, e barra vermelha permanente no rodapé.
  - [x] Player em tela cheia (`PlayerScreen`) força rotação horizontal automática (`SCREEN_ORIENTATION_SENSOR_LANDSCAPE`) ao entrar e restaura a orientação original ao sair.
  - [x] Filtro de categoria unificado no botão ao lado da busca com ícone de menu hambúrguer (`Icons.Default.Menu`) e badge/cor de destaque quando ativo.
  - [x] Carrossel horizontal redundante de chips removido para maximizar o espaço útil da tela.
  - [x] Botão e banner de download removidos da lista externa de séries; download de episódios e temporadas mantido exclusivamente dentro de `SeriesDetailScreen`.
  - [x] Testes unitários com 100% de aprovação (`BUILD SUCCESSFUL`).
  - [x] Compilação do APK e instalação bem-sucedida via ADB no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-14: Filtro de Favoritos (Canais, Filmes e Séries) e Gestão Hierárquica do Botão de Voltar do Sistema
- **Objetivo**: Disponibilizar filtro instantâneo de favoritos com um toque nas 3 seções de conteúdo (Canais, Filmes e Séries) integrado ao SQLite Room e resolver definitivamente o encerramento indesejado do aplicativo ao acionar o botão de voltar físico/gesto do celular.
- **Critérios de Validação**:
  - [x] Query Room `ChannelDao.getChannelsFilteredPaged` com suporte a `onlyFavorites` (`:onlyFavorites = 0 OR isFavorite = 1`) e método `getFavoriteCount`.
  - [x] Botão de favoritos (`Icons.Default.Favorite`) na barra superior de Canais (`LiveScreen`), Filmes (`VodScreen`) e Séries (`SeriesScreen`) com ativação visual e cor de destaque (`#FFFF2A55`).
  - [x] Opção "Somente Favoritos" no menu hambúrguer de categorias (`CategoryBottomSheet`).
  - [x] Placeholder dinâmico indicando o contexto dos favoritos (`Buscar em canais favoritos...`, `Buscar favoritos em "Terror"...`, etc.).
  - [x] Mensagens de estado vazio personalizadas orientando a favoritar conteúdos caso nenhum esteja marcado.
  - [x] Botão de voltar do celular no player fullscreen sai do modo de tela cheia sem fechar o app (`PlayerScreen.kt`).
  - [x] Botão de voltar na tela de detalhes de episódios/série retorna à lista de séries sem fechar o app (`SeriesScreen.kt`).
  - [x] Botão de voltar no modal de detalhes do filme fecha o modal sem fechar o app (`VodScreen.kt`).
  - [x] Botão de voltar em abas secundárias (Filmes, Séries, Downloads, Playlists, Ajustes) retorna para a aba inicial de Canais (`MainApp.kt`).
  - [x] Botão de voltar na tela raiz exige duplo clique com Toast preventivo ("Pressione voltar novamente para sair") antes de encerrar o aplicativo.
  - [x] Testes unitários 100% aprovados (`testDebugUnitTest`).
  - [x] APK compilado com sucesso (`assembleDebug`) e instalado no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-15: Polling Ativo de Downloads (Fim do 0% Eterno) e Botão Animado Estilo Netflix
- **Objetivo**: Resolver o congelamento de downloads em 0%, garantir execução ininterrupta em primeiro plano no Android 12+, emitir progresso contínuo no Room e criar componente animado idêntico ao da Netflix ao iniciar, pausar e concluir downloads.
- **Critérios de Validação**:
  - [x] Corrigido `DownloadService.sendAddDownload` para executar como foreground service (`foreground = true`), impedindo que o Android suspenda o processo.
  - [x] Implementado loop de polling ativo com coroutine a cada 500ms (`startProgressPolling`) no `DownloadController`, calculando bytes transferidos e percentual exato em tempo real.
  - [x] Adicionado `User-Agent` de player ("IPTVSmartPlayer/1.0...") e habilitado `followRedirects` / `followSslRedirects` no OkHttpClient e Media3 HttpDataSource.
  - [x] Inserido mapeamento estrito de MIME types (`.mp4`, `.mkv`, `.ts`, `.m3u8`, `.mpd`) para evitar falhas silenciosas de decodificação.
  - [x] Criado componente Jetpack Compose `NetflixDownloadButton` com animação idêntica à Netflix:
    - Arco contínuo giratório de iniciação e seta deslizando na bandeja (estado `QUEUED`).
    - Anel circular preenchendo o progresso com ícone central quadrado de stop/pause (estado `DOWNLOADING`).
    - Animação pop spring elástica com checkmark circular verde esmeralda (estado `COMPLETED`).
    - Suporte a pause/resume interativo.
  - [x] Integrado `NetflixDownloadButton` nos episódios da tela de detalhes de séries (`SeriesDetailScreen.kt`) e nos cards de filmes (`ChannelCard.kt`).
  - [x] Testes unitários 100% aprovados (`testDebugUnitTest`).
  - [x] Compilação do APK concluída com sucesso (`assembleDebug`).
  - [x] APK instalado e validado no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-16: Abertura e Reprodução de Mídias Baixadas (Media3 toMediaItem) e Blindagem contra Falsos Concluídos
- **Objetivo**: Corrigir o problema de mídias baixadas que não carregavam nem abriam no player após o download, garantir fábrica de DataSources com User-Agent e flags válidas, utilizar a integração oficial do Media3 `toMediaItem()` e rejeitar falsos downloads concluídos com zero bytes.
- **Critérios de Validação**:
  - [x] Refatorado `buildPlaybackDataSourceFactory` com `DefaultDataSource.Factory(appCtx, httpFactory)`, User-Agent oficial e flags limpas (`FLAG_IGNORE_CACHE_ON_ERROR`).
  - [x] Upstream dinâmico: valida estado online/offline em tempo de execução sem estagnar a fábrica na inicialização do app.
  - [x] `PlayerEngine.playContent` detecta downloads concluídos no disco via `DownloadIndex` e instancia o `MediaItem` oficial (`download.request.toMediaItem()`) com streamKeys e decodificadores corretos.
  - [x] Resolução completa de MIME Types (`MP4`, `MKV`, `TS`, `HLS`, `DASH`) para streaming e fallback.
  - [x] `DownloadController` valida `bytesDownloaded > 0L` antes de marcar downloads como `COMPLETED`; descarta conexões vazias do servidor como `DownloadStatus.FAILED` informativo.
  - [x] Clique no botão de download concluído (checkmark da Netflix) em `SeriesDetailScreen.kt` inicia a reprodução do episódio baixado instantaneamente.
  - [x] Testes unitários 100% aprovados (`testDebugUnitTest`).
  - [x] APK gerado com sucesso (`assembleDebug`) e instalado no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-17: Reprodução Offline Nativa (Zero Dependência de Rede) e Fechamento Total do Player
- **Objetivo**: Garantir que mídias baixadas funcionem perfeitamente em modo 100% offline (modo avião / sem internet) sem falhar por tentativas de consulta ao upstream, e ajustar o ciclo de vida do player para fechar e interromper imediatamente a reprodução ao sair da tela de vídeo.
- **Critérios de Validação**:
  - [x] Criado `buildOfflinePlaybackDataSourceFactory` com `upstreamDataSourceFactory = null` e `flags = 0`, isolando a leitura de mídias baixadas do tráfego de rede.
  - [x] `PlayerEngine` alterna dinamicamente entre `offlineMediaSourceFactory` (para downloads e offline) e `onlineMediaSourceFactory` (para streaming online).
  - [x] Implementado método `playerEngine.stop()` que cancela jobs de progresso, para o playback e limpa os itens do ExoPlayer.
  - [x] `onBackClick` da tela cheia em `MainApp.kt` fecha o player (`isPlayingFullscreen = false`, `isCompactPlayerVisible = false`) e chama `playerEngine.stop()`.
  - [x] `onDispose` de `PlayerScreen.kt` e `onClosePlayer` de `CompactPlayerView.kt` acionam `playerEngine.stop()`, eliminando players residuais em background.
  - [x] Testes unitários 100% aprovados (`testDebugUnitTest`).
  - [x] APK gerado com sucesso (`assembleDebug`) e instalado no Samsung Galaxy S23 Ultra (`SM-S918B`).
- **Resultado**: APROVADO.

---

### CP-18: Exibição de Localização de Downloads, Acesso à Pasta e Explorador Integrado em Configurações
- **Objetivo**: Disponibilizar no menu de Configurações a visualização transparente do diretório onde os downloads são salvos, fornecer atalhos de cópia e acesso à pasta via gerenciador de arquivos e um explorador duplo completo com mídias salvas e arquivos reais em disco.
- **Critérios de Validação**:
  - [x] Caminho de downloads visível e destacado no card de Armazenamento (`DownloadModule.getDownloadDirectory`).
  - [x] Botão "Copiar Caminho" copia o diretório absoluto com feedback via Toast.
  - [x] Botão "Acessar Pasta" dispara intents seguras com FileProvider e suporte a Samsung My Files e DocumentsUI, com modal explicativo em caso de restrições do SO.
  - [x] Indicadores visuais com tamanho ocupado pelos downloads, espaço livre no celular e total de itens.
  - [x] Aba "Mídias Salvas" com lista de itens baixados, informações de tamanho, data, botão de reprodução offline direta e botão de exclusão individual.
  - [x] Aba "Arquivos Físicos no Disco" listando os arquivos reais presentes na pasta `downloads/` com tamanhos individuais e datas de modificação.
  - [x] Ações em lote para pausar downloads e excluir todas as mídias salvas com diálogo de confirmação.
  - [x] Testes unitários com 100% de aprovação (`BUILD SUCCESSFUL`).
  - [x] Compilação do APK concluída com sucesso (`assembleDebug`).
- **Resultado**: APROVADO.

---

### CP-19: Busca Contextual Híbrida (Nome e Categoria) & Desduplicação Determinística do Catálogo
- **Objetivo**: Habilitar a busca de conteúdos por categoria e por nome na barra de pesquisa, fornecer carrossel de sugestões e pesquisa interna no seletor de categorias, e eliminar definitivamente todas as duplicatas de canais, filmes e séries nas listagens por categoria.
- **Critérios de Validação**:
  - [x] Query Room `getChannelsFilteredPaged` com busca dupla: `(name LIKE '%' || :query || '%' OR groupTitle LIKE '%' || :query || '%')`.
  - [x] Desduplicação com `GROUP BY LOWER(TRIM(name))` e `ORDER BY name ASC, id ASC` em `ChannelDao.kt`.
  - [x] Contagem precisa de canais únicos com `COUNT(DISTINCT LOWER(TRIM(name)))` em `getCategoriesWithCount`.
  - [x] Carrossel de sugestões de categorias (`LazyRow`) e chip de categoria ativa com `Close` em `LiveScreen.kt`, `VodScreen.kt` e `SeriesScreen.kt`.
  - [x] Campo de busca de categorias dentro do `CategoryBottomSheet` para navegação rápida.
  - [x] Testes unitários 100% aprovados (`testDebugUnitTest`).
  - [x] Compilação do APK concluída com sucesso (`assembleDebug`).
  - [x] Instalação via ADB no dispositivo de teste com êxito (`Success`).
- **Resultado**: APROVADO.








