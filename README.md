# MobileTV - MobileTV Android Offline

> Player IPTV nativo para Android com suporte a listas M3U/M3U8 e Xtream Codes, com o grande diferencial de **download e reprodução 100% offline via AndroidX Media3 (ExoPlayer)**.

---

## 📱 Destaques e Funcionalidades

- **Reprodução Híbrida Inteligente (Online & Offline):**
  - Reprodução em streaming contínuo quando online.
  - Reprodução instantânea a partir do cache local quando o item estiver baixado (economizando dados).
  - **Toggle manual "Ficar offline":** Permite suspender todo o tráfego de rede do app a qualquer momento, sem precisar colocar o dispositivo em Modo Avião.
- **Isolamento Estrito de Rede:**
  - `NetworkGate` e `OfflineModeInterceptor` no OkHttp garantem **0 requisições remotas** quando em modo offline manual.
  - Diálogo de aviso inteligente caso o usuário tente reproduzir um item não baixado em modo offline.
- **Motor de Downloads Resiliente:**
  - Baseado em `androidx.media3.exoplayer.offline.DownloadManager` + `DownloadService`.
  - Downloads em primeiro plano com notificações de progresso, pausa e retomada automática após queda de rede.
  - Verificação de espaço livre antes do download e restrição configurável de "Somente Wi-Fi".
- **Desempenho com Listas Grandes (50.000+ canais):**
  - `M3UParser` em streaming linha a linha com inserção em lotes (batching) no Room Database.
  - Paginação assíncrona fluida com **Jetpack Paging 3**.
- **Guia de Programação (EPG XMLTV):**
  - Parser streaming com `XmlPullParser`.
  - Exibição de programas atuais e próximos por canal.
- **Integração Xtream Codes:**
  - Autenticação e sincronização de categorias Live e VOD.
- **Design Moderno:**
  - Desenvolvido 100% em **Jetpack Compose + Material 3** com paleta Dark Mode refinada.

---

## 🛠️ Stack Tecnológica

| Camada | Tecnologia |
|---|---|
| **Linguagem** | Kotlin 2.3 |
| **Interface (UI)** | Jetpack Compose + Material 3 |
| **Player de Mídia** | AndroidX Media3 (ExoPlayer, HLS, DASH, UI) |
| **Motor de Download** | Media3 `DownloadManager` + `DownloadService` + `SimpleCache` |
| **Banco de Dados** | Room Database (SQLite com índices otimizados) |
| **Paginação** | Paging 3 (Compose) |
| **Rede & HTTP** | Retrofit 2 + OkHttp 4 + Kotlinx Serialization |
| **Carregamento de Imagens** | Coil Compose |
| **Preferências** | Jetpack DataStore Preferences |
| **Background Work** | WorkManager |
| **Testes** | JUnit 4 + Coroutines Test |

---

## 📚 Documentação e Governança

Todos os detalhes técnicos, especificações e acompanhamento do projeto estão disponíveis na pasta `docs/`:

- [docs/IPTV_Android_Offline_Spec.md](docs/IPTV_Android_Offline_Spec.md): Especificação técnica original completa.
- [docs/TASKS.md](docs/TASKS.md): Quadro detalhado de tarefas por fase e status de execução.
- [docs/DECISIONS.md](docs/DECISIONS.md): Registros de Decisões de Arquitetura (ADR-001 a ADR-007).
- [docs/CHECKPOINTS.md](docs/CHECKPOINTS.md): Critérios de aceitação e histórico de validações formais.

---

## 🚀 Como Compilar e Executar

### Pré-requisitos
- JDK 17 ou JDK 21 (o projeto já utiliza automaticamente o JBR bundled do Android Studio).
- Android SDK instalado com plataforma API 36 e build-tools 36.

### Linha de Comando (Gradle)
```bash
# Compilar o APK de depuração
./gradlew assembleDebug

# Executar a suíte de testes unitários
./gradlew testDebugUnitTest
```

O APK gerado estará disponível em:
`app/build/outputs/apk/debug/app-debug.apk`

---

## ⚖️ Aviso Legal
Este aplicativo é estritamente um reprodutor de mídia e **não fornece nem hospeda** listas de reprodução, canais ou conteúdos. O usuário é o único responsável por carregar fontes legítimas para reprodução e download.
