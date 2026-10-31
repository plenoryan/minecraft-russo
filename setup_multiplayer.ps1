# setup_multiplayer.ps1 - Automated Setup for Milestone M4: Offline Skins, Multiplayer Networking & Distribution
[CmdletBinding()]
param (
    [string]$BaseDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " CONFIGURACAO MULTIPLAYER, SKINS OFFLINE & PACOTE DE DISTRIBUICAO (M4)" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Diretorio Base: $BaseDir" -ForegroundColor Gray

$clientDir = Join-Path $BaseDir "Instancia_Cliente"
$clientModsDir = Join-Path $clientDir "mods"
$clientConfigDir = Join-Path $clientDir "config"
$launcherInstanceDir = Join-Path $BaseDir "Launcher\instances\Minecraft_Russo\.minecraft"
$serverDir = Join-Path $BaseDir "Servidor_Dedicado"
$serverModsDir = Join-Path $serverDir "mods"
$serverConfigDir = Join-Path $serverDir "config"

$webClient = New-Object System.Net.WebClient
$webClient.Headers.Add("User-Agent", "MinecraftRussoModpack/1.0 (Windows 11)")

# =========================================================================
# ETAPA 1: Mods Client-Side (CustomSkinLoader & e4all)
# =========================================================================
Write-Host "`n[1/5] Verificando e Instalando Mods Client-Side (Skins & P2P Tunnel)..." -ForegroundColor Yellow

if (-not (Test-Path $clientModsDir)) {
    New-Item -ItemType Directory -Path $clientModsDir -Force | Out-Null
}

# 1.1 CustomSkinLoader (Fabric 1.20.1)
$cslDest = Join-Path $clientModsDir "CustomSkinLoader_Universal-15.1.jar"
$cslUrl = "https://cdn.modrinth.com/data/idMHQ4n2/versions/yyYvYfZI/CustomSkinLoader_Universal-15.1.jar"
if (-not (Test-Path $cslDest) -or ((Get-Item $cslDest).Length -lt 200000)) {
    Write-Host "  Baixando CustomSkinLoader_Universal-15.1.jar..." -ForegroundColor Cyan
    $webClient.DownloadFile($cslUrl, $cslDest)
    Write-Host "  [OK] CustomSkinLoader baixado com sucesso." -ForegroundColor Green
} else {
    Write-Host "  [OK] CustomSkinLoader ja presente." -ForegroundColor Green
}

# 1.2 e4all (Fabric 1.20.1)
$e4allDest = Join-Path $clientModsDir "e4all-fabric-1.6.4.jar"
$e4allUrl = "https://github.com/Synquox/e4all/releases/download/v1.6.4/e4all-fabric-1.6.4.jar"
if (-not (Test-Path $e4allDest) -or ((Get-Item $e4allDest).Length -lt 800000)) {
    Write-Host "  Baixando e4all-fabric-1.6.4.jar..." -ForegroundColor Cyan
    $webClient.DownloadFile($e4allUrl, $e4allDest)
    Write-Host "  [OK] e4all baixado com sucesso." -ForegroundColor Green
} else {
    Write-Host "  [OK] e4all ja presente." -ForegroundColor Green
}

# Sincronizar mods para a instancia do Launcher portatil se ela nao for uma junction
if (Test-Path $launcherInstanceDir) {
    $launcherModsDir = Join-Path $launcherInstanceDir "mods"
    $isJunction = (Get-Item $launcherModsDir -ErrorAction SilentlyContinue).LinkType -eq "Junction"
    if (-not $isJunction -and (Test-Path $launcherModsDir)) {
        Copy-Item $cslDest (Join-Path $launcherModsDir "CustomSkinLoader_Universal-15.1.jar") -Force
        Copy-Item $e4allDest (Join-Path $launcherModsDir "e4all-fabric-1.6.4.jar") -Force
        Write-Host "  [OK] Mods sincronizados com o Launcher portatil." -ForegroundColor Green
    } else {
        Write-Host "  [OK] Pasta mods do Launcher ja vinculada via NTFS Junction." -ForegroundColor Green
    }
}

function Sanitize-SkinNickname([string]$Nickname) {
    if ([string]::IsNullOrWhiteSpace($Nickname)) { return "Steve" }
    $clean = [System.IO.Path]::GetFileName($Nickname)
    $clean = $clean -replace '[\\/:\*\?"<>\|]', '' -replace '\.\.', ''
    if ([string]::IsNullOrWhiteSpace($clean)) { $clean = "Steve" }
    return $clean
}

# =========================================================================
# ETAPA 2: Configuracao CustomSkinLoader (LocalSkin + Ely.by API)
# =========================================================================
Write-Host "`n[2/5] Configurando Sistema de Skins Offline (CustomSkinLoader)..." -ForegroundColor Yellow

$cslJsonContent = @'
{
  "version": "14.18",
  "skinLoading": {
    "loadList": [
      {
        "name": "LocalSkin",
        "type": "Local",
        "skin": "CustomSkinLoader/LocalSkin/skins/{USERNAME}.png",
        "cape": "CustomSkinLoader/LocalSkin/capes/{USERNAME}.png",
        "elytra": "CustomSkinLoader/LocalSkin/elytras/{USERNAME}.png"
      },
      {
        "name": "ElyBy",
        "type": "ElyByAPI",
        "root": "https://ely.by/"
      },
      {
        "name": "Mojang",
        "type": "MojangAPI"
      }
    ]
  },
  "enableSkull": true,
  "enableDynamicSkull": true,
  "cacheExpiry": 2592000
}
'@

$cslConfigDirs = @(
    (Join-Path $clientConfigDir "CustomSkinLoader"),
    (Join-Path $clientDir "CustomSkinLoader")
)

if (Test-Path $launcherInstanceDir) {
    $cslConfigDirs += (Join-Path $launcherInstanceDir "config\CustomSkinLoader")
    $cslConfigDirs += (Join-Path $launcherInstanceDir "CustomSkinLoader")
}

foreach ($cDir in $cslConfigDirs) {
    if (-not (Test-Path $cDir)) {
        New-Item -ItemType Directory -Path $cDir -Force | Out-Null
    }
    $cfgPath = Join-Path $cDir "CustomSkinLoader.json"
    [System.IO.File]::WriteAllText($cfgPath, $cslJsonContent, [System.Text.Encoding]::UTF8)
    
    # Criar subdiretorios LocalSkin para skins, capas e elitras locais
    $localSkinDir = Join-Path $cDir "LocalSkin"
    $skinsDir = Join-Path $localSkinDir "skins"
    $capesDir = Join-Path $localSkinDir "capes"
    $elytrasDir = Join-Path $localSkinDir "elytras"
    
    New-Item -ItemType Directory -Path $skinsDir -Force | Out-Null
    New-Item -ItemType Directory -Path $capesDir -Force | Out-Null
    New-Item -ItemType Directory -Path $elytrasDir -Force | Out-Null
    
    $readmeSkins = Join-Path $skinsDir "COMO_USAR_SKINS_LOCAIS.txt"
    $readmeText = "Coloque a imagem PNG da sua skin aqui renomeada exatamente com o seu apelido do jogo (Exemplo: Jogador1.png). Ela funcionará 100% offline!"
    [System.IO.File]::WriteAllText($readmeSkins, $readmeText, [System.Text.Encoding]::UTF8)
}
Write-Host "  [OK] CustomSkinLoader.json e pastas LocalSkin/ configurados." -ForegroundColor Green

# =========================================================================
# ETAPA 3: Servidor Dedicado (Estrutura, Properties, EULA, Launcher Jar)
# =========================================================================
Write-Host "`n[3/5] Configurando Servidor Dedicado (Servidor_Dedicado/)..." -ForegroundColor Yellow

if (-not (Test-Path $serverDir)) { New-Item -ItemType Directory -Path $serverDir -Force | Out-Null }
if (-not (Test-Path $serverModsDir)) { New-Item -ItemType Directory -Path $serverModsDir -Force | Out-Null }
if (-not (Test-Path $serverConfigDir)) { New-Item -ItemType Directory -Path $serverConfigDir -Force | Out-Null }

# 3.1 eula.txt
$eulaPath = Join-Path $serverDir "eula.txt"
[System.IO.File]::WriteAllText($eulaPath, "eula=true`r`n", [System.Text.Encoding]::UTF8)
Write-Host "  [OK] eula.txt configurado (eula=true)." -ForegroundColor Green

# 3.2 server.properties
$srvPropertiesContent = @"
# Minecraft Server Properties - Modpack Russo (Melty Voice)
# Configurado para Fabric 1.20.1 com autenticacao offline
server-port=25565
server-ip=
online-mode=false
enforce-secure-profile=false
prevent-proxy-connections=false
motd=\u00A7aServidor Minecraft Russo - Melty Voice \u00A77[\u00A7f1.20.1 Fabric\u00A77]
difficulty=normal
gamemode=survival
hardcore=false
pvp=true
spawn-protection=0
max-players=10
view-distance=10
simulation-distance=8
max-tick-time=60000
network-compression-threshold=256
sync-chunk-writes=true
level-name=Mundo_Russo
white-list=false
enable-command-block=true
allow-flight=true
"@

$srvPropsPath = Join-Path $serverDir "server.properties"
[System.IO.File]::WriteAllText($srvPropsPath, $srvPropertiesContent, [System.Text.Encoding]::UTF8)
Write-Host "  [OK] server.properties configurado (online-mode=false, enforce-secure-profile=false)." -ForegroundColor Green

# 3.3 Baixar fabric-server-launch.jar
$srvJarPath = Join-Path $serverDir "fabric-server-launch.jar"
$srvJarUrl = "https://meta.fabricmc.net/v2/versions/loader/1.20.1/0.16.14/1.0.1/server/jar"
if (-not (Test-Path $srvJarPath) -or ((Get-Item $srvJarPath).Length -lt 100000)) {
    Write-Host "  Baixando fabric-server-launch.jar da API Fabric Meta..." -ForegroundColor Cyan
    $webClient.DownloadFile($srvJarUrl, $srvJarPath)
    Write-Host "  [OK] fabric-server-launch.jar baixado ($((Get-Item $srvJarPath).Length) bytes)." -ForegroundColor Green
} else {
    Write-Host "  [OK] fabric-server-launch.jar ja presente." -ForegroundColor Green
}

# 3.4 Sincronizar e Baixar Mods do Servidor
Write-Host "  Sincronizando mods compativeis com o servidor..." -ForegroundColor Cyan

# Copiar mods compativeis do cliente
$modsToCopy = @(
    "fabric-api-0.92.12+1.20.1.jar",
    "sayit-1.20.1-fabric.jar",
    "lithium-fabric-mc1.20.1-0.11.4.jar",
    "ferritecore-6.0.1-fabric.jar"
)

foreach ($modName in $modsToCopy) {
    $src = Join-Path $clientModsDir $modName
    $dst = Join-Path $serverModsDir $modName
    if (Test-Path $src) {
        Copy-Item $src $dst -Force
        Write-Host "    [COPIADO] $modName" -ForegroundColor Green
    } else {
        Write-Host "    [AVISO] Mod de origem nao encontrado: $src" -ForegroundColor Yellow
    }
}

# Baixar FabricTailor (suporte a skins no servidor offline)
$tailorDest = Join-Path $serverModsDir "fabrictailor-2.1.2.jar"
$tailorUrl = "https://cdn.modrinth.com/data/g8w1NapE/versions/MNgY2xFj/fabrictailor-2.1.2.jar"
if (-not (Test-Path $tailorDest) -or ((Get-Item $tailorDest).Length -lt 100000)) {
    Write-Host "    Baixando FabricTailor para servidor..." -ForegroundColor Cyan
    $webClient.DownloadFile($tailorUrl, $tailorDest)
    Write-Host "    [OK] fabrictailor-2.1.2.jar instalado no servidor." -ForegroundColor Green
} else {
    Write-Host "    [OK] fabrictailor-2.1.2.jar ja presente no servidor." -ForegroundColor Green
}

# Garantir que nenhum mod client-only (rendering/GUI) esteja na pasta do servidor
$clientOnlyPatterns = @("sodium", "modernui", "immediatelyfast", "customskinloader", "kubejs", "heracles", "language-reload", "jade")
$existingSrvMods = Get-ChildItem -Path $serverModsDir -Filter "*.jar" -ErrorAction SilentlyContinue
foreach ($m in $existingSrvMods) {
    foreach ($pat in $clientOnlyPatterns) {
        if ($m.Name -match $pat) {
            Remove-Item $m.FullName -Force
            Write-Host "    [REMOVIDO MOD CLIENT-ONLY] $($m.Name)" -ForegroundColor Yellow
        }
    }
}

# 3.5 Copiar configuracao sayit para o servidor
$srcSayitConfig = Join-Path $clientConfigDir "sayit"
$dstSayitConfig = Join-Path $serverConfigDir "sayit"
if (Test-Path $srcSayitConfig) {
    if (-not (Test-Path $dstSayitConfig)) { New-Item -ItemType Directory -Path $dstSayitConfig -Force | Out-Null }
    Copy-Item (Join-Path $srcSayitConfig "*") $dstSayitConfig -Force
    Write-Host "  [OK] Configuracao SayIt sincronizada com o servidor." -ForegroundColor Green
}

# =========================================================================
# ETAPA 4: 1-Click Server Launcher (Iniciar_Servidor.bat)
# =========================================================================
Write-Host "`n[4/5] Criando 1-Click Server Launcher (Iniciar_Servidor.bat)..." -ForegroundColor Yellow

$serverBatContent = @'
@echo off
chcp 65001 >nul
title Servidor Dedicado - Minecraft Russo (Amigos)
cls

echo ======================================================================
echo           SERVIDOR DEDICADO - MINECRAFT RUSSO (OFFLINE/AMIGOS)
echo ======================================================================
echo.

set "BASE_DIR=%~dp0"
set "SERVER_DIR=%BASE_DIR%Servidor_Dedicado"
set "BUNDLED_JAVA=%BASE_DIR%Java17\bin\java.exe"

cd /d "%SERVER_DIR%"

:: 1. Selecionar Java Executable
if exist "%BUNDLED_JAVA%" (
    set "JAVA_CMD=%BUNDLED_JAVA%"
    echo [OK] Usando Java 17 embutido em: "%BUNDLED_JAVA%"
) else (
    set "JAVA_CMD=java"
    echo [OK] Usando comando Java do sistema.
)

:: 2. Validar EULA
if exist "eula.txt" (
    findstr /i "eula=true" eula.txt >nul
    if %errorlevel% neq 0 (
        echo eula=true > eula.txt
        echo [INFO] EULA aceito automaticamente.
    )
) else (
    echo eula=true > eula.txt
    echo [INFO] eula.txt criado com sucesso.
)

:: 3. Validar Configuracoes Offline no server.properties
if exist "server.properties" (
    findstr /i "online-mode=false" server.properties >nul
    if %errorlevel% neq 0 (
        echo [AVISO] Corrigindo server.properties para online-mode=false...
        powershell -NoProfile -Command "(Get-Content server.properties) -replace 'online-mode=true', 'online-mode=false' | Set-Content server.properties"
    )
    findstr /i "enforce-secure-profile=false" server.properties >nul
    if %errorlevel% neq 0 (
        echo [AVISO] Desativando enforce-secure-profile para contas offline...
        powershell -NoProfile -Command "(Get-Content server.properties) -replace 'enforce-secure-profile=true', 'enforce-secure-profile=false' | Set-Content server.properties"
    )
)

:: 4. Executar Servidor Fabric com Flags G1GC Aikar
echo.
echo [INICIANDO SERVIDOR] Alocando 2GB a 4GB de RAM...
echo Pressione Ctrl+C a qualquer momento para desligar o servidor com seguranca.
echo.

"%JAVA_CMD%" -Xms2G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -jar fabric-server-launch.jar nogui

echo.
echo ======================================================================
echo O servidor foi encerrado.
echo ======================================================================
pause
'@

$serverBatPath = Join-Path $BaseDir "Iniciar_Servidor.bat"
[System.IO.File]::WriteAllText($serverBatPath, $serverBatContent, [System.Text.Encoding]::UTF8)
Write-Host "  [OK] Iniciar_Servidor.bat criado com codificacao UTF-8 e flags Aikar G1GC." -ForegroundColor Green

# =========================================================================
# ETAPA 5: Guia de Distribuicao Completo em Portugues
# =========================================================================
Write-Host "`n[5/5] Gerando Guia de Distribuicao para Amigos (GUIA_DISTRIBUICAO_AMIGOS.md)..." -ForegroundColor Yellow

$guideContent = @'
# 🎮 Guia de Instalação e Conexão Multiplayer — Minecraft Russo ("Melty Voice")

Bem-vindo ao **Modpack Minecraft Russo**! Este pacote turnkey foi preparado para você aprender russo na prática enquanto joga sobrevivência cooperativa com seus amigos.

Toda vez que você pegar um novo item no jogo, o mod **SayIt** entrará em ação com o desafio de voz: pronuncie o nome do item em russo pelo seu microfone antes do tempo acabar! Se acertar a pronúncia, você fica com o item. Se o tempo esgotar, o item é dropado no chão ou perdido.

O jogo funciona perfeitamente com **contas offline (cracked/não-originais)** e **contas originais**, sem exigência de login da Microsoft ou Mojang.

---

## 🚀 1. Como Começar (Instalação e Execução em 1 Clique)

1. Extraia o pacote completo recebido em uma pasta rápida do seu computador (recomendado: `F:\Jogos\Mine` ou `C:\Jogos\Mine`).
2. Dê dois cliques no arquivo:
   👉 **`Jogar.bat`**
3. O inicializador portátil abrirá automaticamente:
   - Se pedir para adicionar conta, clique em **Adicionar Conta** -> Escolha **Offline** -> Digite o seu **Nome/Apelido** desejado.
   - O perfil **"Minecraft_Russo"** (Fabric 1.20.1) já estará pré-selecionado.
4. Clique em **Jogar** (ou **Iniciar**)! O jogo inicializará com todos os mods de aprendizado, otimização gráfica para AMD RDNA 2 e reconhecimento de voz prontos.

---

## 🎭 2. Como Colocar Sua Própria Skin (Mesmo em Conta Offline)

Você e seus amigos não precisam jogar com a skin padrão do Steve! O modpack vem com o **CustomSkinLoader** configurado com suporte a dois métodos fáceis:

### Método A: Skin Local no Seu Computador (100% Offline, Sem Internet)
1. Pegue a imagem da sua skin (arquivo `.png` no formato de skin de Minecraft, 64x64).
2. Renomeie o arquivo com o seu **Nome de Jogador** exato.  
   *Exemplo:* Se seu apelido offline é `AmigoRusso`, renomeie o arquivo para `AmigoRusso.png`.
3. Copie o arquivo para a pasta:
   `Instancia_Cliente\CustomSkinLoader\LocalSkin\skins\`  
   *(Ou dentro da pasta `Launcher\instances\Minecraft_Russo\.minecraft\CustomSkinLoader\LocalSkin\skins\`)*
4. Pronto! Ao entrar no jogo, sua skin será carregada localmente para você e renderizada no jogo.

### Método B: Usando o Perfil Gratuito Ely.by (Skin Online sem Conta Microsoft)
1. Crie uma conta gratuita no site [https://ely.by/](https://ely.by/) com o mesmo apelido que você usa no jogo.
2. Faça o upload da sua skin favorita no painel do Ely.by.
3. O modpack puxará sua skin automaticamente da nuvem do Ely.by quando você entrar em qualquer mundo!

---

## 🎙️ 3. Configurando o Microfone e o Reconhecimento de Voz

O reconhecimento de voz roda com o motor de inteligência artificial **Vosk** instalado **localmente no seu computador** (`Instancia_Cliente\vosk_models\`), sem enviar nenhum áudio para a internet e com latência inferior a 50ms!

1. Dentro do jogo, pressione a tecla de atalho:
   👉 **`U`** (Abre o menu de configurações do mod **SayIt**).
2. Na opção de Microfone, selecione o seu dispositivo de áudio principal (ex: *Microfone do Headset USB* ou *Microfone Realtek*).
3. Fale algo no microfone e observe a barrinha de captação de voz se mover para confirmar o volume de entrada.
4. Certifique-se de que o idioma de reconhecimento está definido como **Russian (ru)**.
5. **Ajuste de Tolerância da Pronúncia**: O padrão exige 70% de proximidade fonética. Se tiver dificuldades com o sotaque no começo, você pode ajustar a sensibilidade para 60% nessa mesma tela.
6. **Fallback de Emergência (Se o microfone falhar)**: Se o seu microfone desconectar no meio de um desafio tenso, aperte a tecla **`Y`** para abrir o teclado virtual de emergência e digitar a palavra em russo para não perder o seu item!

---

## 🌐 4. Como Jogar Multiplayer com os Amigos

O modpack oferece duas opções práticas para jogar junto com os amigos:

### OPÇÃO 1: Modo P2P Rápido via `e4all` (O Anfitrião abre o Singleplayer)
*A forma mais fácil e instantânea! Não precisa configurar roteador, não precisa abrir portas e não precisa instalar Hamachi.*

1. **O Anfitrião (quem vai criar e liderar a sessão)**:
   - Entra no seu mundo singleplayer normalmente pelo `Jogar.bat`.
   - Pressiona **ESC** e clica em **Abrir para LAN** (Open to LAN).
   - **CONFIGURAÇÃO CRUCIAL PARA AMIGOS OFFLINE**:
     - Localize a opção **Modo Online (Online Mode)** fornecida pelo mod `e4all`.
     - Altere para **[DESLIGADO / OFF]**.
   - Clique em **Iniciar Mundo LAN**.
   - O mod `e4all` gerará um link público de conexão exibido diretamente no chat (ex: `mine-amigos-7.e4mc.link:25565`).
   - Copie esse link e envie para os seus amigos no Discord ou WhatsApp.

2. **Os Amigos (quem vai entrar na partida)**:
   - Abrem o Minecraft pelo `Jogar.bat`.
   - Vão em **Multijogador** -> **Conexão Direta** (Direct Connection).
   - Colam o endereço fornecido pelo anfitrião e clicam em **Entrar no Servidor**.
   - O mod `e4all` faz o túnel reverso direto, com suporte automático ao proxy de voz UDP na porta **24454** para bate-papo de proximidade!

---

### OPÇÃO 2: Servidor Dedicado 24/7 (`Servidor_Dedicado`)
*Ideal para um mundo permanente de sobrevivência onde qualquer amigo pode entrar mesmo se o anfitrião não estiver jogando.*

1. **Quem vai hospedar o Servidor**:
   - Dê dois cliques no arquivo:
     👉 **`Iniciar_Servidor.bat`**
   - O script iniciará o servidor Fabric 1.20.1 usando o Java 17 embutido e as otimizações Aikar G1GC.
   - O arquivo `server.properties` já vem configurado com:
     - `online-mode=false` (Permite contas offline e originais)
     - `enforce-secure-profile=false` (Evita erros de chat signature)
     - `motd=§aServidor Minecraft Russo - Melty Voice [1.20.1 Fabric]`
   - Aguarde o console exibir a mensagem: `Done! For help, type "help"`.

2. **Liberando Conexão Externa para os Amigos (Sem Port Forwarding)**:
   Como a maioria das conexões residenciais no Brasil utiliza CGNAT, utilize uma das opções gratuitas:
   - **Playit.gg (Recomendado)**: Baixe o agente leve do [Playit.gg](https://playit.gg/), execute-o no computador do servidor e gere um domínio customizado (ex: `russo-amigos.gl.joinmc.link:25565`). Os amigos entram direto pelo domínio sem instalar nada!
   - **Radmin VPN**: Crie uma rede no Radmin VPN (ex: `Minecraft-Russo-Amigos`, senha: `123`). Seus amigos entram na rede e conectam direto pelo IP do Radmin: `26.x.x.x:25565`.

3. **Gerenciamento de Skins no Servidor**:
   O servidor dedicado inclui o mod **FabricTailor**. Qualquer jogador pode digitar comandos no chat para alterar sua skin em tempo real:
   - `/skin set <NomeDeJogadorOriginal>` (Copia a skin de qualquer conta original pública)
   - `/skin set url <LinkPNG>` (Aplica skin diretamente de um link de imagem na internet)
   - `/skin clear` (Restaura a skin padrão)

---

## 🛠️ 5. Resolução de Problemas Frequentes (Troubleshooting)

### P1: "Invalid Session" / "Sessão Inválida" ao tentar conectar
- **Causa**: O anfitrião esqueceu de marcar o modo online como desligado.
- **Solução**: No modo P2P, o anfitrião deve abrir o menu ESC -> Abrir para LAN -> mudar **Modo Online** para **[OFF]** antes de iniciar. No servidor dedicado, confirme que no `Servidor_Dedicado\server.properties` consta `online-mode=false`.

### P2: "Missing or invalid chat signature" / "Perfil seguro obrigatório"
- **Causa**: O servidor está cobrando assinaturas de chat criptografadas da Mojang que clientes offline não possuem.
- **Solução**: No arquivo `server.properties`, confirme que `enforce-secure-profile=false` está definido. O modpack já vem com essa linha configurada por padrão.

### P3: O microfone não capta o som ou a barra de voz não se move
1. Abra as **Configurações do Windows** -> **Privacidade e Segurança** -> **Microfone**.
2. Certifique-se de que a opção *"Permitir que os aplicativos da área de trabalho acessem seu microfone"* está **Ativada**.
3. No jogo, aperte **`U`** e confirme se o microfone selecionado é o aparelho correto e se o volume não está mudo no Windows.

### P4: O jogo ou o servidor não abre ao clicar no `.bat`
- O pacote inclui uma versão portátil do OpenJDK 17 na pasta `Java17\`. Se por algum motivo a pasta foi movida ou apagada, instale o **Java 17 de 64 bits (Eclipse Temurin)** através do site oficial: [https://adoptium.net/](https://adoptium.net/).

### P5: Conexão bloqueada pelo Firewall do Windows
- Na primeira vez que iniciar o jogo ou o servidor, o Windows Defender Firewall exibirá uma janela de aviso. Marque as caixas de **Redes Privadas** e **Redes Públicas** e clique em **Permitir Acesso**.
'@

$guidePath = Join-Path $BaseDir "GUIA_DISTRIBUICAO_AMIGOS.md"
[System.IO.File]::WriteAllText($guidePath, $guideContent, [System.Text.Encoding]::UTF8)
Write-Host "  [OK] GUIA_DISTRIBUICAO_AMIGOS.md gerado com sucesso." -ForegroundColor Green

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host " SUCESSO: Milestone M4 completamente provisionado e configurado!     " -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green
exit 0
