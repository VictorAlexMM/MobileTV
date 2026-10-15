# Registro de Decisões de Arquitetura (ADRs) - IPTV Player Android Offline

> Documento vivo registrando decisões técnicas, padrões arquiteturais, regras de projeto e trade-offs adotados no desenvolvimento do aplicativo.

---

## Índice de Decisões
- [ADR-001: Padrão Arquitetural MVVM + Clean Architecture](#adr-001-padrão-arquitetural-mvvm--clean-architecture)
- [ADR-002: Framework de Interface (Jetpack Compose + Material 3)](#adr-002-framework-de-interface-jetpack-compose--material-3)
- [ADR-003: Player e Mecanismo de Download (Media3 ExoPlayer + DownloadService)](#adr-003-player-e-mecanismo-de-download-media3-exoplayer--downloadservice)
- [ADR-004: Arquitetura de Conectividade Híbrida e Toggle "Ficar Offline"](#adr-004-arquitetura-de-conectividade-híbrida-e-toggle-ficar-offline)
- [ADR-005: Estratégia de Persistência e Otimização para Listas Grandes (Room + Paging 3)](#adr-005-estratégia-de-persistência-e-otimização-para-listas-grandes-room--paging-3)
- [ADR-006: Parsing em Streaming para M3U e XMLTV (EPG)](#adr-006-parsing-em-streaming-para-m3u-e-xmltv-epg)
- [ADR-007: Segurança de Credenciais e Privacidade](#adr-007-segurança-de-credenciais-e-privacidade)

---

### ADR-001: Padrão Arquitetural MVVM + Clean Architecture
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: O aplicativo manipula fontes de dados volumosas (dezenas de milhares de canais em playlists M3U), estados assíncronos de rede, filas de download em background e playback de mídia com estados complexos.
- **Decisão**: 
  - Adotar Clean Architecture estruturada em três camadas essenciais:
    1. **UI Layer**: Composable screens + ViewModels observando StateFlow imutável (UDF - Unidirectional Data Flow).
    2. **Domain Layer**: UseCases puros encapsulando regras de negócio específicas (ex: `GetEffectivePlaybackSourceUseCase`, `ToggleOfflineModeUseCase`).
    3. **Data Layer**: Repositórios desacoplados que orquestram fontes locais (Room / Cache) e remotas (Retrofit / OkHttp), expondo APIs baseadas em Kotlin `Flow` e `Result`.
- **Consequências**: Facilita testes unitários desacoplados da plataforma Android, melhora manutenibilidade e isola a lógica de download/player do ciclo de vida da interface.

---

### ADR-002: Framework de Interface (Jetpack Compose + Material 3)
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: A interface precisa ser dinâmica, com troca instantânea de estados (online/offline), badges de download em tempo real, suporte fluido a temas claro/escuro e adaptação para tablets e telas amplas.
- **Decisão**: 
  - Utilizar **Jetpack Compose** exclusivamente (sem layouts XML clássicos), com tokens e componentes do **Material 3**.
  - Criar um módulo/pacote `core/designsystem` com cores personalizadas (paleta premium dark/light), tipografia moderna e componentes reutilizáveis (Card de Canal, Barra de Progresso de Download, Badge Offline, Banner de Status).
- **Consequências**: Elimina overhead de Views legadas, oferece renderização declarativa fluida e controle direto sobre animações e micro-interações.

---

### ADR-003: Player e Mecanismo de Download (Media3 ExoPlayer + DownloadService)
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: É necessário suportar HLS (`.m3u8` com chunks `.ts`/`.m4s`), DASH e formatos progressivos (MP4, MKV), tanto para streaming quanto para download e reprodução local sem rede.
- **Decisão**: 
  - Adotar a biblioteca oficial `androidx.media3` (`media3-exoplayer`, `media3-exoplayer-hls`, `media3-datasource-okhttp`, `media3-ui`).
  - Utilizar `media3.exoplayer.offline.DownloadManager` combinado com `DownloadService` para download em primeiro plano com notificações resilientes a encerramento do app.
  - Utilizar uma única instância de `SimpleCache` compartilhada entre o serviço de downloads e o player através de um `CacheDataSource.Factory`.
  - Ao reproduzir um item, se ele estiver totalmente em cache, o `CacheDataSource` lê direto do disco local sem realizar requisições à internet.
- **Consequências**: Evita reinventar downloads de HLS segmentados; garante reprodução instantânea com zero consumo de dados para mídias já baixadas.

---

### ADR-004: Arquitetura de Conectividade Híbrida e Toggle "Ficar Offline"
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: O usuário precisa poder assistir offline sem depender do Modo Avião do aparelho e sem gastar franquia de dados quando estiver em redes móveis indesejadas.
- **Decisão**:
  - Criar o componente central `NetworkGate`, responsável por calcular o estado efetivo de permissão de rede:
    $$\text{isOnlineAllowed} = \text{isConnected}(\text{NetworkCallback}) \land \neg(\text{manualOfflineToggle})$$
  - Inserir um `OfflineModeInterceptor` no `OkHttpClient` do app que lança `OfflineModeException` imediatamente se `isOnlineAllowed == false`, bloqueando qualquer chamada remota no nível HTTP.
  - Para o ExoPlayer: quando `isOnlineAllowed == false`, o `upstreamDataSourceFactory` do `CacheDataSource.Factory` é nulo, forçando a leitura estrita do cache local.
  - O estado do toggle manual é salvo em `DataStore` e persiste entre inicializações.
- **Consequências**: Garante conformidade com o critério de aceitação de 0 requisições de rede com toggle ativado, além de transição suave entre modos.

---

### ADR-005: Estratégia de Persistência e Otimização para Listas Grandes (Room + Paging 3)
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: Playlists IPTV comumente contêm 20.000 a 100.000 canais. Carregar tudo em memória causa `OutOfMemoryError` e congela a UI.
- **Decisão**:
  - Persistir entidades com `Room` usando SQLite com índices otimizados:
    - `indices = [Index("playlistId"), Index("contentType"), Index("groupTitle"), Index("isFavorite")]`
  - Utilizar paginação assíncrona com `Paging 3` (`PagingSource` gerado nativamente pelo Room) integrado ao Jetpack Compose (`collectAsLazyPagingItems`).
  - Na importação, realizar inserções em lotes (chunks de 500 a 1000 canais) envolvidos em transações `@Transaction` com `withTransaction`.
- **Consequências**: Abertura de listas em menos de 100ms, consumo estável de RAM mesmo com centenas de milhares de entradas.

---

### ADR-006: Parsing em Streaming para M3U e XMLTV (EPG)
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: Arquivos M3U e arquivos XMLTV podem ter centenas de megabytes.
- **Decisão**:
  - `M3UParser`: Leitura linha a linha via `BufferedReader.useLines` emitindo canais em um fluxo sequencial `Flow<ChannelEntity>` ou processando em buffer contínuo sem reter a lista inteira na memória RAM.
  - `XMLTVParser`: Uso estrito de `XmlPullParser` de streaming nativo do Android, processando tags `<channel>` e `<programme>` sob demanda e descartando nós já processados.
- **Consequências**: Baixíssimo footprint de memória e processamento ininterrupto mesmo em aparelhos com especificações modestas.

---

### ADR-007: Segurança de Credenciais e Privacidade
- **Data**: 2026-10-14
- **Status**: Aceito
- **Contexto**: Credenciais de servidores Xtream Codes (usuário e senha) não devem ficar expostas em texto puro. O app não deve transmitir metadados ou hábitos de visualização do usuário para servidores externos.
- **Decisão**:
  - Credenciais de autenticação são criptografadas com `EncryptedSharedPreferences` / Android Keystore antes de serem gravadas no banco de dados.
  - Telemetria de consumo de conteúdo é estritamente proibida.
  - Arquivos baixados são alocados no diretório seguro do aplicativo (`context.getExternalFilesDir("downloads")`), protegendo-os contra varreduras por apps terceiros sem permissão do usuário.
- **Consequências**: Proteção dos dados do usuário e plena aderência às boas práticas de segurança do Android.

---

### ADR-008: Picture-in-Picture (PiP) e Integração de Ciclo de Vida
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Usuários assistindo canais ao vivo ou filmes precisam manter o vídeo visível em janela flutuante ao navegar em outros aplicativos ou alternar para a tela inicial.
- **Decisão**:
  - Habilitar `android:supportsPictureInPicture="true"` e `android:configChanges="screenSize|smallestScreenSize|screenLayout|orientation"` na `MainActivity`.
  - Configurar `PictureInPictureParams.Builder` com aspecto 16:9 (`Rational(16, 9)`) em dispositivos Android 8.0+ (API 26+).
  - Tratar `onUserLeaveHint()` na Activity para acionar PiP automaticamente quando o player estiver ativo e o usuário pressionar Home.
  - No Jetpack Compose (`PlayerScreen`), ocultar automaticamente controles de overlay (top bar, seekbar, botões de ação) quando `isInPipMode == true` para exibir o stream limpo sem interferência visual.
- **Consequências**: Experiência multitarefa fluida e nativa sem interrupção de áudio/vídeo.

---

### ADR-009: Integração com Player Externo (VLC / MX Player) via Android Intent System
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Alguns codecs legados ou streams exóticos podem se beneficiar da aceleração por hardware e bibliotecas especializadas presentes em players externos populares instalados pelo usuário.
- **Decisão**:
  - Criar o utilitário `ExternalPlayerHelper` responsável por despachar `Intent.ACTION_VIEW` com MIME type `video/*` e flag `FLAG_GRANT_READ_URI_PERMISSION`.
  - Suportar tanto URLs remotas quanto arquivos locais (downloads/gravações) através do `FileProvider` do Android quando aplicável.
  - Adicionar configuração "Usar Player Externo Padrão" nas configurações do app e botão dedicado de acesso rápido na barra do player interno.
  - Implementar fallback resiliente: caso nenhum aplicativo externo compatível seja encontrado, o app notifica o usuário via Toast e abre o player interno automaticamente.
- **Consequências**: Flexibilidade total para o usuário sem comprometer a estabilidade do player interno padrão.

---

### ADR-010: Gravação em Streaming de Canais Ao Vivo e Agendamento EPG com WorkManager
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Canais ao vivo não possuem VOD retroativo nativo no M3U; os usuários precisam gravar transmissões ao vivo imediatas ou agendar programas futuros com base na grade EPG.
- **Decisão**:
  - Implementar `LiveStreamRecordingWorker` estendendo `CoroutineWorker` do Jetpack WorkManager.
  - O worker estabelece conexão HTTP com timeout longo com o stream do canal e escreve pacotes `.ts` diretamente no armazenamento seguro da aplicação (`getExternalFilesDir("recordings")`).
  - O worker respeita cancelamentos de trabalho, atualiza periodicamente o tamanho em bytes no `RecordingDao` e finaliza com status `COMPLETED` ou `FAILED`.
  - Para agendamentos futuros via EPG, o `RecordingScheduler` calcula o atraso inicial (`initialDelay = max(0, startEpoch - now)`) e enfileira uma `OneTimeWorkRequest` garantindo execução mesmo com o app em segundo plano.
  - Gravações concluídas são listadas na aba "Gravações" da tela de Downloads e podem ser reproduzidas offline diretamente pelo Media3 ExoPlayer.
- **Consequências**: Solução robusta e desacoplada do ciclo de vida da interface, garantindo integridade das gravações mesmo se o usuário fechar o aplicativo.

---

### ADR-011: Download em Lote de Temporadas Completas de Séries
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Fazer download de episódios individuais de uma temporada inteira de série demanda muitos cliques e fricção.
- **Decisão**:
  - Adicionar query no Room `ChannelDao.getChannelsByGroup` para recuperar todos os episódios pertencentes a uma temporada/grupo (`groupTitle`).
  - Adicionar método `startBatchDownload` no `DownloadController` que enfileira sequencialmente cada episódio no `AppDownloadService` via `DownloadRequest`.
  - Disponibilizar botão de ação "Baixar Temporada" na tela de Séries (`SeriesScreen`) quando uma categoria de temporada estiver selecionada.
- **Consequências**: Experiência ágil de consumo offline para maratonar séries completas com um único toque.

---

### ADR-012: Experiência de Player Híbrido (Compacto por Padrão com Expansão para Tela Cheia) e Mute
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Iniciar reproduções diretamente em tela cheia obrigatória bloqueia a navegação do usuário e causa desorientação visual. Além disso, muitos usuários necessitam iniciar ou alternar para áudio mudo rapidamente.
- **Decisão**:
  - Criar o componente `CompactPlayerView` que aloca a superfície ExoPlayer no topo da tela em proporção 16:9 (~210dp de altura) preservando os detalhes do conteúdo e a navegação abaixo dele.
  - Implementar o controle `isMuted` e métodos `toggleMute()` / `setMuted()` no `PlayerEngine`, aplicando `volume = 0f` ou `1f` ao ExoPlayer com ícone dinâmico nos controles.
  - Disponibilizar botão explícito de "Tela Cheia" (`Fullscreen`) no player compacto que expande a visualização para `PlayerScreen` em tela cheia imersiva, com botão de "Sair da Tela Cheia" (`FullscreenExit`) e botão de voltar que retornam ao player compacto sem interromper o fluxo do vídeo.
- **Consequências**: Usabilidade comparável ao YouTube e Netflix móvel, dando total controle ao usuário sobre a experiência de visualização.

---

### ADR-013: Metadados Ricos e Estruturação de Temporadas/Episódios para Séries e Filmes
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Playlists IPTV contêm filmes e episódios com nomenclaturas variadas (ex: "S01 E01", "Temporada 2 Ep 05") e falta de campos padronizados para sinopse, ano e gênero.
- **Decisão**:
  - Implementar `MovieMetadataHelper` que isola o ano de lançamento (`\b(19\d{2}|20\d{2})\b`), detecta estilos cinematográficos (Terror, Ação, Comédia, Ficção, etc.) a partir do título e grupo, e gera sinopse descritiva apresentada via `MovieDetailsSheet`.
  - Implementar `SeriesMetadataHelper` que analisa padrões regex para títulos de séries, numeração de temporada e número de episódio, agrupando automaticamente itens em temporadas (`SeriesSeasonGroup`).
  - Apresentar a tela dedicada `SeriesDetailScreen` ao tocar em uma série, carregando tabs de temporadas, lista de episódios, sinopse geral e ações de download da temporada inteira.
- **Consequências**: Catálogo organizado e visualmente convidativo mesmo a partir de fontes M3U brutas.

---

### ADR-014: Indicador de Progresso Idêntico ao YouTube e Toggle de "Marcado como Visto"
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: O usuário precisa saber visualmente de relance quais episódios ou filmes já começou a assistir ou já concluiu, além de poder marcar manualmente episódios como assistidos.
- **Decisão**:
  - Desenvolver o componente `YouTubeProgressBar` posicionado na base inferior das thumbnails com fundo escuro e preenchimento proporcional em vermelho vibrante (`#FF0000`).
  - Adicionar o método `getAllProgress()` no `WatchProgressDao` permitindo observar o estado de progresso de todo o catálogo de forma reativa.
  - Fornecer botão de ação rápida "Marcado como visto" (`CheckCircle`) em cada episódio:
    - Ao marcar: grava `positionMs = durationMs` no Room, preenchendo 100% da barra vermelha do YouTube e exibindo o badge "Visto".
    - Ao desmarcar: remove o registro do Room, limpando a barra e o status.
- **Consequências**: Familiaridade imediata com o padrão visual consagrado pelo YouTube e controle preciso sobre o histórico de exibição.

---

### ADR-015: Resolução Precisa de Séries e Isolamento Estrito de Episódios (Xtream Codes API & M3U)
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Ao abrir uma série, o app agrupava previamente todos os canais do mesmo `groupTitle` genérico (ex: "SÉRIES NETFLIX"), fazendo com que episódios de dezenas de séries diferentes fossem exibidos misturados como se fossem da mesma temporada. Ademais, em playlists Xtream Codes, o Room armazena apenas a série como cabeçalho e os episódios requerem requisição dedicada à API `get_series_info`.
- **Decisão**:
  - Implementar o método `getSeriesInfo` em `XtreamApiService` e parser nativo `SeriesMetadataHelper.parseXtreamSeriesInfo` para extrair sob demanda as temporadas, sinopses e URLs de streaming diretas da API Xtream Codes (`/series/username/password/id.ext`).
  - Implementar query Room `ChannelDao.getSeriesEpisodes` para mídias M3U e aplicar filtragem estrita em Kotlin (`isMatchingSeries`) que descarta com rigor qualquer item cujo título normalizado não corresponda à série aberta.
  - Utilizar limites de palavras (`\b`) nas regexes de limpeza de metadados para tokens como `S01`, `T01`, `1x01` e `Temp 1`, prevenindo falsos positivos em títulos como "Stranger Things" (onde a terminação em "s" era equivocadamente capturada como marcador de temporada).
  - Unificar a resolução no método `ChannelRepository.loadSeriesDetails(item)` com feedback de carregamento suave na interface (`isLoadingSeriesDetails`).
- **Consequências**: Resolução exata de episódios sem vazamento de outras séries, compatibilidade total tanto com playlists M3U brutas quanto servidores Xtream Codes, e integridade blindada por testes unitários.

---

### ADR-016: Busca Contextual por Categoria, Progressão no Mini Player, Rotação Automática Fullscreen e Filtro Hamburger
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: A busca textual anteriormente ignorava a categoria ativa; o mini player não fornecia seekbar interativa com tempos; o player em tela cheia não rotacionava automaticamente para landscape; o carrossel horizontal de categorias ocupava área vertical excessiva; e o botão de download de séries aparecia no card exterior fora do detalhe de episódios.
- **Decisão**:
  - Empregar `getChannelsFilteredPaged` do `ChannelRepository` nas telas de Ao Vivo, Filmes e Séries para filtrar estritamente a pesquisa textual dentro da categoria selecionada.
  - Integrar seekbar interativa com timestamps (`formatCompactDuration`) nos controles do `CompactPlayerView`, além da barra vermelha permanente (`YouTubeProgressBar`) no rodapé do mini player.
  - Aplicar `ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE` via `DisposableEffect` no `PlayerScreen`, assegurando que o player entre automaticamente em modo horizontal e retorne à orientação padrão ao sair.
  - Consolidar a filtragem de categorias exclusivamente no botão hambúrguer (`Icons.Default.Menu`) adjacente à busca, com destaque visual ativo e placeholder contextual (`Buscar em "Categoria"...`), eliminando a barra redundante de chips.
  - Remover a ação de download dos cards externos e banners na tela de séries, centralizando o download de episódios e temporadas completas estritamente dentro da tela de detalhes da série (`SeriesDetailScreen`).
- **Consequências**: Ergonomia e usabilidade móvel aprimoradas, busca precisa sem ruído de categorias alheias, rotação imersiva automática e interface mais limpa.

---

### ADR-017: Filtro Unificado de Favoritos e Tratamento Hierárquico do Botão de Voltar do Sistema
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Anteriormente, não havia como filtrar somente itens favoritados nas seções principais de Ao Vivo, Filmes e Séries. Além disso, o botão de voltar físico/gesto do Android (`onBackPressed`) fechava o aplicativo abruptamente quando o usuário estava assistindo em tela cheia, consultando episódios de uma série, visualizando detalhes de um filme ou navegando entre abas.
- **Decisão**:
  - Estender `ChannelDao.getChannelsFilteredPaged` com a cláusula SQL `(:onlyFavorites = 0 OR isFavorite = 1)` e expor `getFavoriteCount(playlistId, contentType)` no `ChannelRepository`, garantindo que a filtragem por favoritos ocorra diretamente no SQLite com suporte a paginação e busca textual.
  - Implementar o botão de favoritos (ícone `Icons.Default.Favorite` com feedback visual de ativação e cor de destaque) na barra superior de Canais (`LiveScreen`), Filmes (`VodScreen`) e Séries (`SeriesScreen`), além de incluir a opção "Somente Favoritos" no menu hambúrguer de categorias (`CategoryBottomSheet`).
  - Estabelecer uma arquitetura de `BackHandler`s do Jetpack Compose com precedência em camadas:
    1. Player em tela cheia: sai do fullscreen e restaura a visualização compacta sem fechar o app (`PlayerScreen.kt`).
    2. Detalhes de séries: fecha o modal de temporadas/episódios e retorna à lista de séries (`SeriesScreen.kt`).
    3. Detalhes de filmes: fecha o sheet de biografia/metadados e retorna à grade de filmes (`VodScreen.kt`).
    4. Guia EPG e sheets de categorias: fecha o sheet ativo.
    5. Filtro de favoritos e busca: desativa o filtro de favoritos ou limpa a query de pesquisa.
    6. Abas secundárias: retorna à aba inicial de Canais (`AppDestination.LIVE`) em vez de fechar o app (`MainApp.kt`).
    7. Raiz do aplicativo: requer toque duplo no botão voltar dentro de 2 segundos com Toast de aviso ("Pressione voltar novamente para sair") antes de finalizar a Activity.
- **Consequências**: Experiência de usuário fluida e natural no Android, prevenção total contra fechamentos acidentais do app e acesso rápido e consistente aos itens favoritos em qualquer seção.

---

### ADR-018: Resolução do Fluxo de Downloads Congelados em 0%, Polling Ativo de Progresso e Botão Animado Estilo Netflix
- **Data**: 2026-10-15
- **Status**: Aceito
- **Contexto**: Ao colocar séries ou episódios para baixar, o indicador de progresso permanecia indefinidamente em 0%. Além disso, o botão de download era estático e sem feedback visual atrativo, carecendo da micro-animação fluida característica do aplicativo Netflix no momento em que o download é acionado e progride.
- **Decisão**:
  - **Diagnóstico e Correção da Causa Raiz do 0%**:
    1. **Serviço em Foreground (Android 12+)**: `DownloadService.sendAddDownload` utilizava `foreground = false`. No Android 12+, serviços em background sem canal foreground sofrem restrições severas de execução, fazendo com que o OS congele as threads de download. Alterado para `foreground = true`.
    2. **Ausência de Emissão de Bytes pelo Listener**: A arquitetura do Media3 `DownloadManager.Listener.onDownloadChanged` foi concebida exclusivamente para disparar em **mudanças de estado** (ex: transição para `STATE_DOWNLOADING`), instante no qual os bytes transferidos ainda são zero. Ele intencionalmente **não** dispara eventos contínuos conforme os pacotes de dados chegam. Implementou-se um mecanismo de polling ativo via Coroutine (`startProgressPolling`) no `DownloadController` que consulta `downloadManager.currentDownloads` a cada 500ms, calcula a porcentagem exata (`bytesDownloaded / contentLength * 100f` ou `download.percentDownloaded`) e persiste em tempo real no Room via `downloadDao.updateProgress`.
    3. **Bypass de 403 Forbidden e Redirecionamentos em Servidores IPTV**: Configurado interceptor com `User-Agent: IPTVSmartPlayer/1.0 (Linux; Android) ExoPlayerLib/Media3` e habilitado `followRedirects(true)` e `followSslRedirects(true)` tanto no `NetworkClient` quanto no `DownloadModule.getHttpDataSourceFactory`.
    4. **Mapeamento Exato de MIME Types**: Inserida resolução explícita de `MimeTypes.VIDEO_MP4`, `VIDEO_MATROSKA`, `VIDEO_MP2T`, `APPLICATION_M3U8` e `APPLICATION_MPD` com base na extensão da URL no `DownloadRequest.Builder`.
    5. **Relaxamento de Requisitos Restritivos**: Empregado `Requirements(Requirements.NETWORK)` e acionamento garantido de `downloadManager.resumeDownloads()`.
  - **Componente Visual Netflix (`NetflixDownloadButton`)**:
    - Criado componente Jetpack Compose com suporte a micro-animações estilo Netflix:
      - **Estado QUEUED**: Início de download com arco contínuo girando em 360° em loop e seta deslizando suavemente para dentro da bandeja.
      - **Estado DOWNLOADING**: Anel circular de progresso em tempo real preenchendo a circunferência com ícone central quadrado de parada/pausa (estilo Netflix) e indicação visual precisa.
      - **Estado COMPLETED**: Transição com pop spring bouncy e checkmark circular verde esmeralda.
      - **Estado PAUSED / FAILED**: Estados visuais interativos permitindo retomar ou reiniciar o download com clique.
    - Totalmente integrado na lista de episódios em `SeriesDetailScreen.kt` e nos cards de filmes em `ChannelCard.kt`.
- **Consequências**: Downloads de séries e episódios agora fluem continuamente com barra e percentual em tempo real, suporte total a background downloading no Android 12+ sem interrupção do sistema, e feedback estético idêntico à Netflix ao toque do usuário.





