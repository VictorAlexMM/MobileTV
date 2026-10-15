# MobileTV — APKs de Distribuição

Esta pasta contém os pacotes APK compilados do **MobileTV** prontos para instalação direta em smartphones e dispositivos Android.

## Estrutura de Pastas

- **[`apk/mobile/`](./mobile/)**: Contém a versão compilada para smartphones e tablets Android (`MobileTV-Mobile-debug.apk`).
- **[`apk/tv/`](./tv/)**: Contém a versão com suporte a controle remoto (D-Pad), Leanback Launcher e banner para Smart TV, Android TV, Google TV e Fire TV (`MobileTV-TV-debug.apk`).
- **`apk/MobileTV.apk`**: Atalho direto para o último APK universal compilado.

## Como Instalar no Celular

### Opção 1: Via Cabo USB (ADB)
Conecte o smartphone ao computador com a **Depuração USB** ativada e execute no terminal:
```powershell
adb install -r -t "apk\mobile\MobileTV-Mobile-debug.apk"
```

### Opção 2: Transferência Direta
1. Envie o arquivo `MobileTV-Mobile-debug.apk` para o celular (via WhatsApp, Telegram, Google Drive, ou cabo USB).
2. No celular, abra o gerenciador de arquivos e toque no arquivo `.apk`.
3. Autorize a instalação de fontes desconhecidas se solicitado e conclua a instalação.
