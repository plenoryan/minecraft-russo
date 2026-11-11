<#
.SYNOPSIS
    Compilador e Empacotador do Cliente Portatil Minecraft Russo (Milestone M3).
.DESCRIPTION
    Monta uma arvore 100% autossuficiente e portatil em dist\Minecraft_Russo_Client_Portable,
    elimina todas as juncoes NTFS (ReparsePoints), empacota OpenJDK 17 64-bit,
    normaliza configuracoes do UltimMC, cria o inicializador de clique unico Jogar.bat,
    gera o arquivo dist\Minecraft_Russo_Client_Setup.zip e calcula o hash SHA-256 genuino.
.PARAMETER TargetRoot
    Diretorio raiz do projeto (padrao: F:\Jogos\Mine ou diretorio deste script).
.PARAMETER OutputDir
    Diretorio de destino da arvore portatil.
.PARAMETER ZipOutput
    Caminho do arquivo ZIP final gerado.
.PARAMETER SkipZip
    Se definido, pula a etapa de compressao em ZIP (util para inspecao rapida).
.PARAMETER Clean
    Se definido, limpa completamente a pasta de saida antes da montagem.
#>

[CmdletBinding()]
param(
    [string]$TargetRoot = "",
    [string]$OutputDir = "",
    [string]$ZipOutput = "",
    [switch]$SkipZip,
    [switch]$Clean
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# ------------------------------------------------------------------------------
# 1. Resolucao de Caminhos
# ------------------------------------------------------------------------------
if ([string]::IsNullOrWhiteSpace($TargetRoot)) {
    if (Test-Path "F:\Jogos\Mine") {
        $TargetRoot = "F:\Jogos\Mine"
    } else {
        $TargetRoot = $PSScriptRoot
    }
}
$TargetRoot = [System.IO.Path]::GetFullPath($TargetRoot)

$DistDir = Join-Path $TargetRoot "dist"
if ([string]::IsNullOrWhiteSpace($OutputDir)) {
    $OutputDir = Join-Path $DistDir "Minecraft_Russo_Client_Portable"
}
$OutputDir = [System.IO.Path]::GetFullPath($OutputDir)

if ([string]::IsNullOrWhiteSpace($ZipOutput)) {
    $ZipOutput = Join-Path $DistDir "Minecraft_Russo_Client_Setup.zip"
}
$ZipOutput = [System.IO.Path]::GetFullPath($ZipOutput)
$ChecksumOutput = $ZipOutput + ".sha256"

function Write-Step {
    param([string]$Title)
    Write-Host ("`n=== [" + (Get-Date -Format 'HH:mm:ss') + "] " + $Title + " ===") -ForegroundColor Cyan
}

function Write-Success {
    param([string]$Message)
    Write-Host (" [OK] " + $Message) -ForegroundColor Green
}

function Write-Warn {
    param([string]$Message)
    Write-Host (" [AVISO] " + $Message) -ForegroundColor Yellow
}

function Write-Utf8NoBom {
    param([string]$FilePath, [string]$Content)
    $parent = [System.IO.Path]::GetDirectoryName($FilePath)
    if (-not [System.IO.Directory]::Exists($parent)) {
        [System.IO.Directory]::CreateDirectory($parent) | Out-Null
    }
    $encoding = [System.Text.UTF8Encoding]::new($false)
    [System.IO.File]::WriteAllText($FilePath, $Content, $encoding)
}

function Compute-OfflineUUID {
    param([string]$Username)
    if ([string]::IsNullOrEmpty($Username)) {
        return "00000000-0000-0000-0000-000000000000"
    }
    $bytes = [System.Text.Encoding]::UTF8.GetBytes("OfflinePlayer:" + $Username)
    $md5 = [System.Security.Cryptography.MD5]::Create().ComputeHash($bytes)
    $md5[6] = [byte](($md5[6] -band 0x0f) -bor 0x30)
    $md5[8] = [byte](($md5[8] -band 0x3f) -bor 0x80)
    $hex = [System.BitConverter]::ToString($md5).Replace("-", "").ToLower()
    return "{0}-{1}-{2}-{3}-{4}" -f $hex.Substring(0,8), $hex.Substring(8,4), $hex.Substring(12,4), $hex.Substring(16,4), $hex.Substring(20,12)
}

function Copy-DirectoryFlattened {
    param(
        [string]$SourcePath,
        [string]$DestinationPath,
        [string[]]$Excludes = @()
    )
    if (-not (Test-Path $SourcePath)) {
        throw ("Diretorio de origem nao encontrado: " + $SourcePath)
    }
    if (-not (Test-Path $DestinationPath)) {
        [System.IO.Directory]::CreateDirectory($DestinationPath) | Out-Null
    }
    $srcDir = [System.IO.DirectoryInfo]::new($SourcePath)
    $files = $srcDir.EnumerateFiles("*", [System.IO.SearchOption]::AllDirectories)
    $copied = 0
    foreach ($file in $files) {
        $rel = $file.FullName.Substring($srcDir.FullName.Length).TrimStart('\', '/')
        
        $skip = $false
        foreach ($ex in $Excludes) {
            if ($rel -like $ex -or $file.Name -like $ex) {
                $skip = $true
                break
            }
        }
        if ($skip) { continue }
        
        $target = [System.IO.Path]::Combine($DestinationPath, $rel)
        $parent = [System.IO.Path]::GetDirectoryName($target)
        if (-not [System.IO.Directory]::Exists($parent)) {
            [System.IO.Directory]::CreateDirectory($parent) | Out-Null
        }
        [System.IO.File]::Copy($file.FullName, $target, $true)
        $copied++
    }
    return $copied
}

Write-Host "==============================================================================" -ForegroundColor DarkCyan
Write-Host " MINECRAFT RUSSO - COMPILADOR E EMPACOTADOR DO CLIENTE PORTATIL [M3]" -ForegroundColor DarkCyan
Write-Host "==============================================================================" -ForegroundColor DarkCyan
Write-Host (" Raiz do Projeto   : " + $TargetRoot)
Write-Host (" Destino Portatil  : " + $OutputDir)
Write-Host (" Arquivo ZIP       : " + $ZipOutput)
Write-Host (" Checksum SHA-256  : " + $ChecksumOutput)
Write-Host (" Pular ZIP         : " + $(if ($SkipZip) { 'Sim' } else { 'Nao' }))

$stopwatch = [System.Diagnostics.Stopwatch]::StartNew()

# ------------------------------------------------------------------------------
# 2. Limpeza e Preparacao de Diretorios
# ------------------------------------------------------------------------------
Write-Step "Preparando diretorio de distribuicao"
if (-not (Test-Path $DistDir)) {
    [System.IO.Directory]::CreateDirectory($DistDir) | Out-Null
}

if (Test-Path $OutputDir) {
    Write-Host "Limpando arvore portatil pre-existente..." -ForegroundColor DarkGray
    Remove-Item -Path $OutputDir -Recurse -Force -ErrorAction Stop
}
[System.IO.Directory]::CreateDirectory($OutputDir) | Out-Null
Write-Success ("Arvore de destino inicializada: " + $OutputDir)

# ------------------------------------------------------------------------------
# 3. Empacotar Java 17 Runtime (64-bit)
# ------------------------------------------------------------------------------
Write-Step "Empacotando OpenJDK 17 Runtime 64-bit"
$javaSrc = Join-Path $TargetRoot "Java17"
$javaDst = Join-Path $OutputDir "Java17"

if (-not (Test-Path (Join-Path $javaSrc "bin\javaw.exe"))) {
    throw ("Java 17 nao encontrado em " + $javaSrc + ". Execute install_java17.ps1 antes de empacotar.")
}

$javaFiles = Copy-DirectoryFlattened -SourcePath $javaSrc -DestinationPath $javaDst
$javawCheck = Join-Path $javaDst "bin\javaw.exe"
if (-not (Test-Path $javawCheck)) {
    throw ("Falha ao copiar Java 17: " + $javawCheck + " nao encontrado.")
}
Write-Success ("Java 17 empacotado com sucesso (" + $javaFiles + " arquivos copiados, sem juncoes).")

# ------------------------------------------------------------------------------
# 4. Copiar Binarios e Estrutura Basica do UltimMC Launcher
# ------------------------------------------------------------------------------
Write-Step "Copiando binarios do inicializador portatil UltimMC"
$launcherSrc = Join-Path $TargetRoot "Launcher"
$launcherDst = Join-Path $OutputDir "Launcher"

[System.IO.Directory]::CreateDirectory($launcherDst) | Out-Null

# Copiar arquivos na raiz do Launcher (exe, DLLs, etc.)
$rootFiles = Get-ChildItem -Path $launcherSrc -File -ErrorAction Stop
foreach ($rf in $rootFiles) {
    if ($rf.Name -eq "ultimmc.cfg") { continue } # Normalizado adiante
    [System.IO.File]::Copy($rf.FullName, (Join-Path $launcherDst $rf.Name), $true)
}

# Copiar pastas de suporte (exceto data, instances e juncoes)
$supportDirs = @("iconengines", "imageformats", "jars", "meta", "platforms")
foreach ($sd in $supportDirs) {
    $sSrc = Join-Path $launcherSrc $sd
    $sDst = Join-Path $launcherDst $sd
    if (Test-Path $sSrc) {
        Copy-DirectoryFlattened -SourcePath $sSrc -DestinationPath $sDst | Out-Null
    }
}
Write-Success "Binarios UltimMC e bibliotecas Qt copiados para Launcher\"

# ------------------------------------------------------------------------------
# 5. Copiar Dados do Launcher (Assets, Libraries, Temas, Meta)
# ------------------------------------------------------------------------------
Write-Step "Copiando caches offline do UltimMC (assets e libraries vanilla)"
$dataSrc = Join-Path $launcherSrc "data"
$dataDst = Join-Path $launcherDst "data"
[System.IO.Directory]::CreateDirectory($dataDst) | Out-Null

$dataDirs = @("assets", "libraries", "icons", "injectors", "meta", "themes", "translations")
foreach ($dd in $dataDirs) {
    $dSrc = Join-Path $dataSrc $dd
    $dDst = Join-Path $dataDst $dd
    if (Test-Path $dSrc) {
        Write-Host ("  -> Copiando data\" + $dd + "...") -ForegroundColor DarkGray
        Copy-DirectoryFlattened -SourcePath $dSrc -DestinationPath $dDst | Out-Null
    }
}

if (Test-Path (Join-Path $dataSrc "metacache")) {
    [System.IO.File]::Copy((Join-Path $dataSrc "metacache"), (Join-Path $dataDst "metacache"), $true)
}
Write-Success "Assets e bibliotecas offline do Fabric/Minecraft copiados com sucesso."

# ------------------------------------------------------------------------------
# 6. Montar Instancia Minecraft_Russo e Achatar Todas as Juncoes NTFS
# ------------------------------------------------------------------------------
Write-Step "Achatando juncoes NTFS e montando instancia fisica Minecraft_Russo"
$instDst = Join-Path $dataDst "instances\Minecraft_Russo"
$mcDst = Join-Path $instDst ".minecraft"
[System.IO.Directory]::CreateDirectory($mcDst) | Out-Null

# Copiar manifesto do Fabric 1.20.1
$mmcPackSrc = Join-Path $dataSrc "instances\Minecraft_Russo\mmc-pack.json"
if (-not (Test-Path $mmcPackSrc)) {
    $mmcPackSrc = Join-Path $TargetRoot "Instancia_Cliente\mmc-pack.json"
}
[System.IO.File]::Copy($mmcPackSrc, (Join-Path $instDst "mmc-pack.json"), $true)

# Copiar arquivos de configuracao de topo do .minecraft
$instClientSrc = Join-Path $TargetRoot "Instancia_Cliente"
$topFiles = @("iris.properties", "heracles_options.json", "icon.png", "irisUpdateInfo.json", "usercache.json", "usernamecache.json")
foreach ($tf in $topFiles) {
    $tfSrc = Join-Path $instClientSrc $tf
    if (-not (Test-Path $tfSrc)) {
        $tfSrc = Join-Path $dataSrc "instances\Minecraft_Russo\.minecraft\$tf"
    }
    if (Test-Path $tfSrc) {
        [System.IO.File]::Copy($tfSrc, (Join-Path $mcDst $tf), $true)
    }
}

# 6.1 ACHATAR MODS (43 mods fisicos, incluindo sayitenglish)
Write-Host "  -> Copiando 43 mods Fabric para .minecraft\mods\..." -ForegroundColor DarkGray
$modsSrc = Join-Path $instClientSrc "mods"
$modsDst = Join-Path $mcDst "mods"
$modCount = Copy-DirectoryFlattened -SourcePath $modsSrc -DestinationPath $modsDst
Write-Success ("Mods copiados como diretorio fisico: " + $modCount + " arquivos em .minecraft\mods\")

# 6.2 ACHATAR CONFIG (incluindo modelo neural Vosk e audio_cache)
Write-Host "  -> Copiando configs pedagogicas e modelo neural Vosk para .minecraft\config\..." -ForegroundColor DarkGray
$configSrc = Join-Path $instClientSrc "config"
$configDst = Join-Path $mcDst "config"
$cfgCount = Copy-DirectoryFlattened -SourcePath $configSrc -DestinationPath $configDst
Write-Success ("Configuracoes pedagogicas e modelo Vosk copiados: " + $cfgCount + " arquivos em .minecraft\config\")

# 6.3 ACHATAR KUBEJS (traducoes ru_ru e scripts pedagogicos)
Write-Host "  -> Copiando KubeJS (assets ru_ru e russian_tooltips.js) para .minecraft\kubejs\..." -ForegroundColor DarkGray
$kubejsSrc = Join-Path $instClientSrc "kubejs"
$kubejsDst = Join-Path $mcDst "kubejs"
$kubeCount = Copy-DirectoryFlattened -SourcePath $kubejsSrc -DestinationPath $kubejsDst
Write-Success ("KubeJS copiado como diretorio fisico: " + $kubeCount + " arquivos em .minecraft\kubejs\")

# 6.4 ACHATAR RESOURCEPACKS (Russian_Bilingual_Educational_Pack e complementares)
Write-Host "  -> Copiando resourcepacks educativos para .minecraft\resourcepacks\..." -ForegroundColor DarkGray
$rpSrc = Join-Path $instClientSrc "resourcepacks"
$rpDst = Join-Path $mcDst "resourcepacks"
$rpCount = Copy-DirectoryFlattened -SourcePath $rpSrc -DestinationPath $rpDst
Write-Success ("Resourcepacks copiados como diretorio fisico: " + $rpCount + " arquivos em .minecraft\resourcepacks\")

# 6.5 ACHATAR SHADERPACKS
Write-Host "  -> Copiando shaderpacks para .minecraft\shaderpacks\..." -ForegroundColor DarkGray
$spSrc = Join-Path $instClientSrc "shaderpacks"
$spDst = Join-Path $mcDst "shaderpacks"
$spCount = Copy-DirectoryFlattened -SourcePath $spSrc -DestinationPath $spDst
Write-Success ("Shaderpacks copiados: " + $spCount + " arquivos em .minecraft\shaderpacks\")

# 6.6 ACHATAR CUSTOMSKINLOADER
Write-Host "  -> Copiando CustomSkinLoader para .minecraft\CustomSkinLoader\..." -ForegroundColor DarkGray
$cslSrc = Join-Path $instClientSrc "CustomSkinLoader"
$cslDst = Join-Path $mcDst "CustomSkinLoader"
if (Test-Path $cslSrc) {
    Copy-DirectoryFlattened -SourcePath $cslSrc -DestinationPath $cslDst | Out-Null
    Write-Success "CustomSkinLoader copiado como diretorio fisico."
}

# 6.7 ACHATAR SAYIT_AUDIO_CACHE (arquivos MP3 pre-gerados para offline imediato)
Write-Host "  -> Copiando cache fonetico offline para .minecraft\sayit_audio_cache\..." -ForegroundColor DarkGray
$audioSrc = Join-Path $instClientSrc "sayit_audio_cache"
$audioDst = Join-Path $mcDst "sayit_audio_cache"
if (Test-Path $audioSrc) {
    $audioCount = Copy-DirectoryFlattened -SourcePath $audioSrc -DestinationPath $audioDst
    Write-Success ("Cache de audio fonetico pre-gerado copiado: " + $audioCount + " arquivos em .minecraft\sayit_audio_cache\")
}

# 6.8 NORMALIZAR OPTIONS.TXT (UTF-8 Zero-BOM, Key 'H' registrada, Iris 'R' desvinculada)
Write-Host "  -> Gravando options.txt normalizado (UTF-8 Zero-BOM)..." -ForegroundColor DarkGray
$optionsSrc = Join-Path $instClientSrc "options.txt"
$optionsDst = Join-Path $mcDst "options.txt"

$optionsLines = [System.Collections.Generic.List[string]]::new()
if (Test-Path $optionsSrc) {
    $rawLines = Get-Content -Path $optionsSrc -Encoding UTF8
    foreach ($line in $rawLines) {
        $trimmed = $line.Trim()
        # Filtrar entradas que iremos padronizar para evitar duplicatas
        if ($trimmed -match '^key_key\.sayitenglish\.' -or $trimmed -match '^key_iris\.keybind\.reload') {
            continue
        }
        $optionsLines.Add($line)
    }
}

# Inserir atalhos pedagogicos SayIt e desvincular 'R' do Iris
$optionsLines.Add("key_key.sayitenglish.type:key.keyboard.y")
$optionsLines.Add("key_key.sayitenglish.settings:key.keyboard.u")
$optionsLines.Add("key_key.sayitenglish.radial:key.keyboard.v")
$optionsLines.Add("key_key.sayitenglish.hear:key.keyboard.h")
$optionsLines.Add("key_iris.keybind.reload:key.keyboard.unknown")

$finalOptionsText = ($optionsLines -join "`r`n") + "`r`n"
Write-Utf8NoBom -FilePath $optionsDst -Content $finalOptionsText

# Validar Zero-BOM estrito em options.txt
$optBytes = [System.IO.File]::ReadAllBytes($optionsDst)
if ($optBytes.Length -ge 3 -and $optBytes[0] -eq 0xEF -and $optBytes[1] -eq 0xBB -and $optBytes[2] -eq 0xBF) {
    throw "Violacao de integridade: options.txt gerado com BOM proibido."
}
Write-Success "options.txt gravado com sucesso (UTF-8 Zero-BOM, Key 'H' e 'V' ativas, Iris 'R' desvinculada)."

# ------------------------------------------------------------------------------
# 7. Normalizar Configuracoes do UltimMC (Caminhos Relativos / Dinamicos)
# ------------------------------------------------------------------------------
Write-Step "Normalizando configuracoes do UltimMC e contas offline"

# 7.1 instance.cfg na pasta da instancia
$instCfgDst = Join-Path $instDst "instance.cfg"
$instanceCfgLines = @(
    "AutoCloseConsole=false",
    "InstanceType=OneSix",
    "JavaArchitecture=64",
    "JavaPath=Java17/bin/javaw.exe",
    "JavaTimestamp=1787101018000",
    "JavaVersion=17.0.20.1",
    "JoinServerOnLaunch=false",
    "JvmArgs=-XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1",
    "MaxMemAlloc=4096",
    "MinMemAlloc=2048",
    "MinecraftWinHeight=480",
    "MinecraftWinWidth=854",
    "OverrideJava=true",
    "OverrideJavaArgs=true",
    "OverrideJavaLocation=true",
    "QuitAfterLaunch=true",
    "RecordGameTime=true",
    "ShowConsole=false",
    "ShowConsoleOnError=true",
    "ShowGameTime=true",
    "iconKey=default",
    "name=Minecraft_Russo"
)
Write-Utf8NoBom -FilePath $instCfgDst -Content (($instanceCfgLines -join "`r`n") + "`r`n")
Write-Success "instance.cfg normalizado com JavaPath=Java17/bin/javaw.exe"

# 7.2 ultimmc.cfg em Launcher\data\ e Launcher\
$dataUltimMc = Join-Path $dataDst "ultimmc.cfg"
$dataUltimMcLines = @(
    "Analytics=false",
    "ConsoleMaxLines=100000",
    "ConsoleOverflowStop=true",
    "JavaArchitecture=64",
    "JavaPath=Java17/bin/javaw.exe",
    "JavaTimestamp=1787101018000",
    "JavaVersion=17.0.20.1",
    "Language=pt_BR",
    "MaxMemAlloc=4096",
    "MinMemAlloc=2048",
    "InstanceDir=instances"
)
Write-Utf8NoBom -FilePath $dataUltimMc -Content (($dataUltimMcLines -join "`r`n") + "`r`n")

$rootUltimMc = Join-Path $launcherDst "ultimmc.cfg"
$rootUltimMcLines = @(
    "Analytics=false",
    "JavaArchitecture=64",
    "JavaPath=Java17/bin/javaw.exe",
    "Language=pt_BR",
    "MaxMemAlloc=4096",
    "MinMemAlloc=2048",
    "InstanceDir=data/instances"
)
Write-Utf8NoBom -FilePath $rootUltimMc -Content (($rootUltimMcLines -join "`r`n") + "`r`n")
Write-Success "ultimmc.cfg normalizado em data\ e na raiz de Launcher\"

# 7.3 accounts.json com perfil offline exclusivo (codigo aleatorio para evitar colisoes de rede)
$randChars = [char[]]((48..57) + (65..90) | ForEach-Object { [char]$_ })
$randCode = -join ($randChars | Get-Random -Count 4)
$portableUsername = "Estudante_" + $randCode
$portableUUID = Compute-OfflineUUID $portableUsername

$accountsJsonTemplate = @{
    accounts = @(
        @{
            active = $true
            entitlement = @{
                canPlayMinecraft = $true
                ownsMinecraft = $true
            }
            profile = @{
                capes = @()
                id = $portableUUID
                name = $portableUsername
                skins = @()
            }
            type = "dummy"
            ygg = @{
                extra = @{
                    clientToken = [System.Guid]::NewGuid().ToString("N")
                    userName = $portableUsername
                }
                iat = [int][DateTimeOffset]::UtcNow.ToUnixTimeSeconds()
                token = $portableUsername
            }
        }
    )
    formatVersion = 3
} | ConvertTo-Json -Depth 5
Write-Utf8NoBom -FilePath (Join-Path $dataDst "accounts.json") -Content ($accountsJsonTemplate + "`r`n")
Write-Utf8NoBom -FilePath (Join-Path $launcherDst "accounts.json") -Content ($accountsJsonTemplate + "`r`n")
Write-Success ("accounts.json configurado com perfil offline aleatorio '" + $portableUsername + "' (dummy)")

# ------------------------------------------------------------------------------
# 8. Criar Jogar.bat e Mudar_Nome.bat no Pacote Portatil
# ------------------------------------------------------------------------------
Write-Step "Criando executaveis Jogar.bat e Mudar_Nome.bat no pacote portatil"

$sourceJogarBat = Join-Path $launcherSrc "Jogar.bat"
if (-not (Test-Path $sourceJogarBat)) {
    $sourceJogarBat = Join-Path $TargetRoot "Jogar.bat"
}
if (-not (Test-Path $sourceJogarBat)) {
    throw ("Launcher\Jogar.bat de referencia nao encontrado: " + $sourceJogarBat)
}

$jogarBatText = Get-Content -Raw -Encoding UTF8 $sourceJogarBat

# Grava Jogar.bat na raiz da distribuicao portatil
$portableJogarBat = Join-Path $OutputDir "Jogar.bat"
Write-Utf8NoBom -FilePath $portableJogarBat -Content $jogarBatText

# Copia para Launcher\Jogar.bat dentro do pacote
Write-Utf8NoBom -FilePath (Join-Path $launcherDst "Jogar.bat") -Content $jogarBatText

# Copia Mudar_Nome.bat para raiz e Launcher\
$sourceMudarNomeBat = Join-Path $launcherSrc "Mudar_Nome.bat"
if (-not (Test-Path $sourceMudarNomeBat)) {
    $sourceMudarNomeBat = Join-Path $TargetRoot "Mudar_Nome.bat"
}
if (Test-Path $sourceMudarNomeBat) {
    $mudarNomeBatText = Get-Content -Raw -Encoding UTF8 $sourceMudarNomeBat
    Write-Utf8NoBom -FilePath (Join-Path $OutputDir "Mudar_Nome.bat") -Content $mudarNomeBatText
    Write-Utf8NoBom -FilePath (Join-Path $launcherDst "Mudar_Nome.bat") -Content $mudarNomeBatText
    Write-Success "Mudar_Nome.bat copiado para a distribuicao portatil."
}

# Instrucoes para amigos em LEIA-ME.txt
$leiaMeText = "======================================================================`r`n" +
    "         MINECRAFT RUSSO - MODPACK COOPERATIVO DE APRENDIZADO`r`n" +
    "======================================================================`r`n`r`n" +
    "COMO JOGAR [1 CLIQUE]:`r`n" +
    "1. De dois cliques no arquivo Jogar.bat nesta pasta.`r`n" +
    "2. O inicializador abrira e iniciara automaticamente a instancia com:`r`n" +
    "   - 43 mods Fabric 1.20.1 configurados.`r`n" +
    "   - Reconhecimento de voz em russo (offline via modelo neural Vosk).`r`n" +
    "   - Referencia fonetica auditiva com voz russa nativa.`r`n" +
    "   - Perfil offline exclusivo pre-configurado para evitar colisoes na rede.`r`n`r`n" +
    "COMO MUDAR SEU NOME DE JOGADOR:`r`n" +
    "1. De dois cliques no arquivo Mudar_Nome.bat nesta pasta.`r`n" +
    "2. Digite seu apelido desejado (ex: Arthur, Pedro, Lucas, etc.) e aperte ENTER.`r`n" +
    "3. Pronto! O nome sera atualizado instantaneamente.`r`n`r`n" +
    "TECLAS IMPORTANTES NO JOGO:`r`n" +
    " - [V] : Abre a Roleta Radial de Acoes Cooperativas.`r`n" +
    " - [H] : Ouve a pronuncia nativa em russo do item ou desafio atual [Pressione para Ouvir].`r`n" +
    " - [Y] : Fallback para digitacao via teclado caso esteja sem microfone.`r`n" +
    " - [U] : Abre o Livro de Missoes e Configuracoes Pedagogicas.`r`n`r`n" +
    "REQUISITOS DO SISTEMA:`r`n" +
    "- Windows 10 ou Windows 11 (64-bit).`r`n" +
    "- Nao precisa instalar nada! O Java 17 e todos os componentes ja estao incluidos.`r`n`r`n" +
    "Bons estudos e boa diversao!`r`n" +
    "======================================================================`r`n"
Write-Utf8NoBom -FilePath (Join-Path $OutputDir "LEIA-ME.txt") -Content $leiaMeText
Write-Success "Jogar.bat, Mudar_Nome.bat e LEIA-ME.txt criados na raiz da distribuicao portatil."

# ------------------------------------------------------------------------------
# 9. Auditoria de Integridade Estrita (Zero Juncoes / Todos os Arquivos)
# ------------------------------------------------------------------------------
Write-Step "Executando auditoria de integridade da arvore portatil"

# 9.1 Zero ReparsePoints
$reparsePoints = @(Get-ChildItem -Path $OutputDir -Recurse -Attributes ReparsePoint -ErrorAction SilentlyContinue)
if ($reparsePoints.Count -gt 0) {
    $badList = ($reparsePoints | Select-Object -ExpandProperty FullName) -join "`n"
    throw ("FALHA DE INTEGRIDADE: Foram detectados ReparsePoints/juncoes na distribuicao portatil:`n" + $badList)
}
Write-Success "Auditoria de ReparsePoints: 0 juncoes ou links simbolicos detectados (100% fisico e portatil)."

# 9.2 Verificacao de Arquivos Essenciais
$mustExist = @(
    "Java17\bin\javaw.exe",
    "Launcher\UltimMC.exe",
    "Launcher\Qt5Core.dll",
    "Launcher\platforms\qwindows.dll",
    "Launcher\data\accounts.json",
    "Launcher\data\ultimmc.cfg",
    "Launcher\data\instances\Minecraft_Russo\instance.cfg",
    "Launcher\data\instances\Minecraft_Russo\mmc-pack.json",
    "Launcher\data\instances\Minecraft_Russo\.minecraft\options.txt",
    "Launcher\data\instances\Minecraft_Russo\.minecraft\config\sayit.json",
    "Launcher\data\instances\Minecraft_Russo\.minecraft\config\sayitenglish\models\vosk-model-small-ru-0.22",
    "Launcher\data\instances\Minecraft_Russo\.minecraft\kubejs\client_scripts\russian_tooltips.js",
    "Launcher\data\instances\Minecraft_Russo\.minecraft\sayit_audio_cache",
    "Jogar.bat",
    "Mudar_Nome.bat",
    "LEIA-ME.txt"
)

foreach ($me in $mustExist) {
    $full = Join-Path $OutputDir $me
    if (-not (Test-Path $full)) {
        throw ("FALHA DE INTEGRIDADE: Arquivo essencial ausente na distribuicao: " + $me)
    }
}
Write-Success ("Todos os " + $mustExist.Count + " arquivos essenciais confirmados presentes.")

# 9.3 Contagem de Mods
$installedMods = @(Get-ChildItem -Path (Join-Path $mcDst "mods") -Filter "*.jar" -ErrorAction SilentlyContinue)
if ($installedMods.Count -lt 43) {
    throw ("FALHA DE INTEGRIDADE: Quantidade insuficiente de mods em .minecraft\mods (" + $installedMods.Count + " < 43)")
}
Write-Success ("Auditoria de Mods: " + $installedMods.Count + " JARs Fabric verificados com sucesso.")

# 9.4 options.txt Key 'H' e Iris 'R'
$optContent = Get-Content -Raw -Encoding UTF8 (Join-Path $mcDst "options.txt")
if ($optContent -notmatch "key_key\.sayitenglish\.hear:key\.keyboard\.h") {
    throw "FALHA DE INTEGRIDADE: options.txt nao possui key_key.sayitenglish.hear registrado na tecla 'H'."
}
if ($optContent -notmatch "key_iris\.keybind\.reload:key\.keyboard\.unknown") {
    throw "FALHA DE INTEGRIDADE: options.txt nao possui key_iris.keybind.reload desvinculado (unknown)."
}
if ($optContent -match 'key_iris\.keybind\.reload:key\.keyboard\.r') {
    throw "FALHA DE INTEGRIDADE: options.txt vincula indevidamente shader reload a tecla 'R'."
}
Write-Success "Auditoria de options.txt: Atalho 'H' registrado e tecla 'R' de shaders desvinculada com sucesso."

# ------------------------------------------------------------------------------
# 10. Compressao em ZIP e Geracao de Checksum SHA-256
# ------------------------------------------------------------------------------
if ($SkipZip) {
    Write-Warn "Etapa de compressao em ZIP pulada conforme solicitacao (-SkipZip)."
} else {
    Write-Step ("Comprimindo distribuicao em " + $ZipOutput)
    
    if (Test-Path $ZipOutput) {
        Remove-Item -Path $ZipOutput -Force -ErrorAction Stop
    }
    if (Test-Path $ChecksumOutput) {
        Remove-Item -Path $ChecksumOutput -Force -ErrorAction SilentlyContinue
    }

    Write-Host "Iniciando compressao de alta velocidade (System.IO.Compression)..." -ForegroundColor DarkGray
    [System.Reflection.Assembly]::LoadWithPartialName("System.IO.Compression.FileSystem") | Out-Null
    
    $zipTimer = [System.Diagnostics.Stopwatch]::StartNew()
    [System.IO.Compression.ZipFile]::CreateFromDirectory(
        $OutputDir,
        $ZipOutput,
        [System.IO.Compression.CompressionLevel]::Fastest,
        $false
    )
    $zipTimer.Stop()

    $zipItem = Get-Item $ZipOutput
    $zipSizeMB = [math]::Round($zipItem.Length / 1MB, 2)
    Write-Success ("ZIP gerado com sucesso: " + $zipSizeMB + " MB em " + $zipTimer.Elapsed.TotalSeconds.ToString('F1') + "s")

    # Calculo do hash SHA-256 genuino
    Write-Step "Calculando hash criptografico SHA-256"
    $hashResult = (Get-FileHash -Path $ZipOutput -Algorithm SHA256).Hash.ToLowerInvariant()
    $zipFileName = Split-Path $ZipOutput -Leaf
    $checksumContent = $hashResult + "  " + $zipFileName + "`r`n"
    
    Write-Utf8NoBom -FilePath $ChecksumOutput -Content $checksumContent
    Write-Success ("Checksum SHA-256 genuino gravado em " + $ChecksumOutput)
    Write-Host ("  -> Hash: " + $hashResult) -ForegroundColor Yellow

    # Validacao cruzada do Checksum
    $verifyHash = (Get-FileHash -Path $ZipOutput -Algorithm SHA256).Hash.ToLowerInvariant()
    if ($verifyHash -ne $hashResult) {
        throw ("Erro de verificacao cruzada de SHA-256: '" + $verifyHash + "' != '" + $hashResult + "'")
    }
    Write-Success "Verificacao cruzada de hash aprovada com 100% de precisao."

    # Sincronizar com docs\downloads\ e atualizar docs\index.html
    $docsDownloads = Join-Path $TargetRoot "docs\downloads"
    if (Test-Path $docsDownloads) {
        $docsZip = Join-Path $docsDownloads $zipFileName
        if (Test-Path $docsZip) { Remove-Item -Path $docsZip -Force -ErrorAction SilentlyContinue }
        try {
            New-Item -ItemType HardLink -Path $docsZip -Target $ZipOutput -ErrorAction Stop | Out-Null
        } catch {
            Copy-Item -Path $ZipOutput -Destination $docsZip -Force
        }
        Copy-Item -Path $ChecksumOutput -Destination (Join-Path $docsDownloads ($zipFileName + ".sha256")) -Force
        Write-Success ("Distribuicao sincronizada com " + $docsDownloads)
    }
    $indexHtmlPath = Join-Path $TargetRoot "docs\index.html"
    if (Test-Path $indexHtmlPath) {
        $htmlText = Get-Content -Raw -Encoding UTF8 $indexHtmlPath
        $updatedHtml = $htmlText -replace 'id="sha256-val">[^<]+<', ('id="sha256-val">' + $hashResult + '<')
        $updatedHtml = $updatedHtml -replace 'data-copy="[a-f0-9]{64}"\s+data-copy-label="SHA-256"', ('data-copy="' + $hashResult + '" data-copy-label="SHA-256"')
        if ($updatedHtml -ne $htmlText) {
            Write-Utf8NoBom -FilePath $indexHtmlPath -Content $updatedHtml
            Write-Success "docs\index.html sincronizado com o hash SHA-256 atual."
        }
    }
}

$stopwatch.Stop()
Write-Host ""
Write-Host "==============================================================================" -ForegroundColor Green
Write-Host " EMPACOTAMENTO CONCLUIDO COM SUCESSO!" -ForegroundColor Green
Write-Host (" Tempo Total: " + $stopwatch.Elapsed.TotalSeconds.ToString('F1') + " segundos") -ForegroundColor Green
Write-Host "==============================================================================" -ForegroundColor Green

exit 0
