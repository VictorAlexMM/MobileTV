# MobileTV — Versão Android TV / Google TV / Smart TV

Esta pasta contém o pacote APK compilado do **MobileTV** otimizado especificamente para **Android TV**, **Google TV**, **Fire TV Stick**, **Mi Box**, **Chromecast com Google TV** e caixas TV Box.

## Arquivo APK
- **`MobileTV-TV-debug.apk`**: APK universal com suporte completo a Leanback Launcher e controle remoto (D-Pad).

---

## Funcionalidades para TV
1. **Navegação 100% por Controle Remoto (D-Pad):**
   - Destaque visual nítido (zoom suave e borda iluminada) em todos os cards, categorias, botões e canais.
   - Navegação pelas setas (Cima, Baixo, Esquerda, Direita) e botão central (OK / Enter).
2. **Controles de Reprodução na Smart TV:**
   - **OK / Play/Pause:** Pausa e retoma a reprodução instantaneamente.
   - **Seta Direita / Fast-Forward:** Avança 10 segundos.
   - **Seta Esquerda / Rewind:** Volta 10 segundos.
   - **Seta Cima / Seta Baixo:** Exibe ou oculta a barra de controles e seekbar.
   - **Voltar (Back):** Retorna à tela da série/canal sem fechar o aplicativo.
3. **Banner Oficial para o Menu da TV (`tv_banner.xml`):**
   - Ícone retangular 16:9 de alta resolução que aparece no menu inicial da sua Smart TV ou Google TV.
4. **Mesmas Funcionalidades do Mobile:**
   - Canais ao vivo, Filmes VOD, Séries com episódios agrupados por temporada.
   - Continuar assistindo de onde parou com barra de progresso do YouTube.
   - Sincronização automática de últimos lançamentos ao abrir o app sem apagar cache.
   - Mídias baixadas offline e canais favoritos.

---

## Como Instalar na Smart TV / Android TV

### Método 1: Via Rede Wi-Fi (ADB Sem Fio)
1. Na sua TV, ative as **Opções de Desenvolvedor** e ative a **Depuração de Rede (Wireless Debugging)** ou **Depuração USB**.
2. Anote o endereço IP da TV (ex: `192.168.1.100` nas configurações de Rede).
3. No computador, abra o PowerShell no diretório do projeto e execute:
   ```powershell
   adb connect 192.168.1.100:5555
   adb install -r -t "apk\tv\MobileTV-TV-debug.apk"
   ```

### Método 2: Via Pendrive USB
1. Copie o arquivo `MobileTV-TV-debug.apk` para um pendrive formatado em FAT32 ou NTFS.
2. Conecte o pendrive na porta USB da Smart TV ou TV Box.
3. Na TV, abra qualquer gerenciador de arquivos (ex: *FX File Explorer*, *File Commander* ou *AnExplorer*).
4. Localize o APK no pendrive e clique nele para instalar.

### Método 3: Via Aplicativo "Send Files to TV" ou "Downloader"
1. Instale o app gratuito **Send Files to TV** na TV e no seu celular.
2. Envie o arquivo `MobileTV-TV-debug.apk` do celular para a TV via Wi-Fi.
3. Abra o arquivo recebido na TV para instalar.
