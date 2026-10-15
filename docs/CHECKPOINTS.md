# Pontos de Verificação (Checkpoints) - IPTV Player Android Offline

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
