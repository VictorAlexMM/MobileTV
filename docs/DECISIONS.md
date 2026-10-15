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
