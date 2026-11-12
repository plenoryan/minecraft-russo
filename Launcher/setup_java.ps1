# setup_java.ps1 - Auto-vinculacao e normalizacao do Java 17 no UltimMC
param(
    [string]$BaseDir = ""
)

$ErrorActionPreference = "SilentlyContinue"

# 1. Determinar BaseDir (%~dp0 do Jogar.bat)
if (-not $BaseDir) {
    if (Test-Path (Join-Path $PSScriptRoot "..\Java17\bin\javaw.exe")) {
        $BaseDir = (Resolve-Path (Join-Path $PSScriptRoot "..")).Path
    } elseif (Test-Path (Join-Path $PSScriptRoot "Java17\bin\javaw.exe")) {
        $BaseDir = (Resolve-Path $PSScriptRoot).Path
    } else {
        $BaseDir = (Get-Location).Path
    }
}

$BaseDir = [System.IO.Path]::GetFullPath($BaseDir)

# 2. Localizar o executavel javaw.exe
$bundledJava = Join-Path $BaseDir "Java17\bin\javaw.exe"
$targetJavaExe = ""

if (Test-Path -LiteralPath $bundledJava) {
    $targetJavaExe = $bundledJava
} else {
    $sysJava = Get-Command javaw.exe -ErrorAction SilentlyContinue
    if ($sysJava) {
        $targetJavaExe = $sysJava.Source
    }
}

if (-not $targetJavaExe) {
    Write-Host "[AVISO] Nao foi possivel detectar o Java 17 automaticamente." -ForegroundColor Yellow
    exit 0
}

$javaNorm = $targetJavaExe.Replace('\', '/')
Write-Host "[OK] Java 17 configurado automaticamente: $javaNorm" -ForegroundColor Green

# 3. Criar juncoes NTFS para suporte a caminhos relativos de fallback
$launcherDir = Join-Path $BaseDir "Launcher"
$dataDir = Join-Path $launcherDir "data"
$bundledJavaDir = Join-Path $BaseDir "Java17"

if (Test-Path -LiteralPath $bundledJavaDir) {
    $jLauncher = Join-Path $launcherDir "Java17"
    $jData = Join-Path $dataDir "Java17"

    if (-not (Test-Path -LiteralPath $jLauncher)) {
        cmd.exe /c "mklink /J `"$jLauncher`" `"$bundledJavaDir`"" 2>$null | Out-Null
    }
    if (-not (Test-Path -LiteralPath $jData)) {
        cmd.exe /c "mklink /J `"$jData`" `"$bundledJavaDir`"" 2>$null | Out-Null
    }
}

# 4. Atualizar arquivos de configuracao do UltimMC e instancias
$configs = @(
    (Join-Path $dataDir "ultimmc.cfg"),
    (Join-Path $launcherDir "ultimmc.cfg"),
    (Join-Path $dataDir "instances\Minecraft_Russo\instance.cfg"),
    (Join-Path $launcherDir "instances\Minecraft_Russo\instance.cfg")
)

$utf8NoBom = [System.Text.UTF8Encoding]::new($false)

foreach ($cfg in $configs) {
    if (Test-Path -LiteralPath $cfg) {
        $lines = [System.Collections.Generic.List[string]](Get-Content -LiteralPath $cfg -Encoding UTF8)
        $hasJava = $false
        $hasAnalyticsSeen = $false
        $hasLanguage = $false
        $hasOverrideJava = $false
        $hasOverrideJavaLoc = $false

        for ($i = 0; $i -lt $lines.Count; $i++) {
            $line = $lines[$i]
            if ($line -match '^JavaPath=') {
                $lines[$i] = "JavaPath=$javaNorm"
                $hasJava = $true
            } elseif ($line -match '^AnalyticsSeen=') {
                $lines[$i] = "AnalyticsSeen=2"
                $hasAnalyticsSeen = $true
            } elseif ($line -match '^Language=') {
                $hasLanguage = $true
            } elseif ($line -match '^OverrideJava=') {
                $lines[$i] = "OverrideJava=true"
                $hasOverrideJava = $true
            } elseif ($line -match '^OverrideJavaLocation=') {
                $lines[$i] = "OverrideJavaLocation=true"
                $hasOverrideJavaLoc = $true
            }
        }

        if (-not $hasJava) {
            $lines.Add("JavaPath=$javaNorm")
        }

        if ($cfg -like '*ultimmc.cfg') {
            if (-not $hasAnalyticsSeen) { $lines.Add("AnalyticsSeen=2") }
            if (-not $hasLanguage) { $lines.Add("Language=pt_BR") }
        }

        if ($cfg -like '*instance.cfg') {
            if (-not $hasOverrideJava) { $lines.Add("OverrideJava=true") }
            if (-not $hasOverrideJavaLoc) { $lines.Add("OverrideJavaLocation=true") }
        }

        [System.IO.File]::WriteAllLines($cfg, $lines, $utf8NoBom)
    }
}

# 5. Normalizar accounts.json para tipo dummy caso esteja offline
$accounts = @(
    (Join-Path $dataDir "accounts.json"),
    (Join-Path $launcherDir "accounts.json")
)

foreach ($acc in $accounts) {
    if (Test-Path -LiteralPath $acc) {
        $a = Get-Content -LiteralPath $acc -Raw -Encoding UTF8
        if ($a -match '"type":\s*"Offline"') {
            $a = $a -replace '"type":\s*"Offline"', '"type": "dummy"'
            [System.IO.File]::WriteAllText($acc, $a, $utf8NoBom)
        }
    }
}
