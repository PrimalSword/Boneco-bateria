# Voltinho ⚡

Um mascote flutuante que reage ao nível da bateria do Android.

## O que já existe no MVP

- Overlay arrastável sobre outros aplicativos.
- Três mascotes originais: **Pingo**, **Byte** e **Mimo**.
- Animações geradas por código, sem assets pagos e sem licença de terceiros.
- Reações para carga completa, carregando, bateria alta, média, baixa e crítica.
- Porcentagem opcional, tamanho e opacidade configuráveis.
- Inicialização após reiniciar o aparelho.
- Serviço em primeiro plano com notificação e ação de desativar.
- Compatibilidade de Android 8.0 (API 26) a Android 16 (API 36).
- CI no GitHub Actions com APK de debug como artefato.

## Arquitetura

- Kotlin + Jetpack Compose na interface principal.
- `WindowManager.TYPE_APPLICATION_OVERLAY` para o mascote.
- `BatteryManager` / `ACTION_BATTERY_CHANGED` para leitura local da bateria.
- `MascotView`, um `View` customizado que desenha e anima os personagens em tempo real.
- `SharedPreferences` para preferências pequenas e locais.

## Como testar

1. Abra a aba **Actions** do repositório.
2. Entre na execução mais recente de **Android CI**.
3. Baixe o artefato `voltinho-debug-apk`.
4. Instale o APK em um Android 8 ou superior.
5. Abra o app, conceda **Exibir sobre outros apps** e toque em **Ativar mascote**.

## Privacidade

O Voltinho não usa internet, não cria conta, não contém analytics e não coleta dados pessoais. Consulte [PRIVACY.md](PRIVACY.md).

## Licença

Código sob Apache License 2.0. Os mascotes e suas animações são originais deste projeto.
