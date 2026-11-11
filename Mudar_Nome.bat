@echo off
chcp 65001 >nul
title Minecraft Russo - Trocar Nome de Jogador
cls

set "SCRIPT_DIR=%~dp0"
if exist "%~dp0Launcher\data\accounts.json" (
    set "LAUNCHER_DIR=%~dp0Launcher\"
) else if exist "%~dp0data\accounts.json" (
    set "LAUNCHER_DIR=%~dp0"
) else if exist "%~dp0..\Launcher\data\accounts.json" (
    set "LAUNCHER_DIR=%~dp0..\Launcher\"
) else (
    set "LAUNCHER_DIR=%~dp0Launcher\"
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$rawArg = '%*';" ^
    "$launcherDir = $env:LAUNCHER_DIR;" ^
    "$dataAccounts = Join-Path $launcherDir 'data\accounts.json';" ^
    "$rootAccounts = Join-Path $launcherDir 'accounts.json';" ^
    "function Compute-UUID([string]$name) {" ^
    "    $b = [System.Text.Encoding]::UTF8.GetBytes('OfflinePlayer:' + $name);" ^
    "    $h = [System.Security.Cryptography.MD5]::Create().ComputeHash($b);" ^
    "    $h[6] = [byte](($h[6] -band 0x0f) -bor 0x30);" ^
    "    $h[8] = [byte](($h[8] -band 0x3f) -bor 0x80);" ^
    "    $x = [System.BitConverter]::ToString($h).Replace('-', '').ToLower();" ^
    "    return '{0}-{1}-{2}-{3}-{4}' -f $x.Substring(0,8), $x.Substring(8,4), $x.Substring(12,4), $x.Substring(16,4), $x.Substring(20,12);" ^
    "};" ^
    "$currentName = 'Desconhecido';" ^
    "if (Test-Path -LiteralPath $dataAccounts) {" ^
    "    try { $j = Get-Content -LiteralPath $dataAccounts -Raw -Encoding UTF8 | ConvertFrom-Json; $currentName = $j.accounts[0].ygg.extra.userName; } catch {}" ^
    "} elseif (Test-Path -LiteralPath $rootAccounts) {" ^
    "    try { $j = Get-Content -LiteralPath $rootAccounts -Raw -Encoding UTF8 | ConvertFrom-Json; $currentName = $j.accounts[0].ygg.extra.userName; } catch {}" ^
    "}" ^
    "Write-Host '======================================================================' -ForegroundColor Cyan;" ^
    "Write-Host '            MINECRAFT RUSSO - TROCA RAPIDA DE NOME                   ' -ForegroundColor Cyan;" ^
    "Write-Host '======================================================================' -ForegroundColor Cyan;" ^
    "Write-Host (' Nome de jogador atual: ' + $currentName) -ForegroundColor Yellow;" ^
    "Write-Host '';" ^
    "$newName = $rawArg.Trim();" ^
    "if (-not $newName) {" ^
    "    Write-Host ' Digite seu novo apelido de jogador (3 a 16 letras/numeros, sem espaco):' -ForegroundColor White;" ^
    "    Write-Host ' (Exemplos: Arthur, Pedro, Lucas, Amigo_Russo. Pressione ENTER para manter)' -ForegroundColor DarkGray;" ^
    "    $newName = Read-Host ' [Novo Nome]';" ^
    "    if ($newName) { $newName = $newName.Trim(); }" ^
    "}" ^
    "if (-not $newName -or $newName -eq $currentName) {" ^
    "    Write-Host '';" ^
    "    Write-Host (' Nenhuma alteracao realizada. O nome foi mantido como: ' + $currentName) -ForegroundColor DarkYellow;" ^
    "    exit 0;" ^
    "}" ^
    "if ($newName -notmatch '^[a-zA-Z0-9_]{3,16}$') {" ^
    "    Write-Host '';" ^
    "    Write-Host '[ERRO] Nome invalido! Use de 3 a 16 caracteres (letras, numeros ou sublinhado _).' -ForegroundColor Red;" ^
    "    exit 1;" ^
    "}" ^
    "$uuid = Compute-UUID $newName;" ^
    "$token = [System.Guid]::NewGuid().ToString('N');" ^
    "$accObj = @{" ^
    "    formatVersion = 3;" ^
    "    accounts = @(@{" ^
    "        active = $true;" ^
    "        entitlement = @{ canPlayMinecraft = $true; ownsMinecraft = $true };" ^
    "        profile = @{ capes = @(); id = $uuid; name = $newName; skins = @() };" ^
    "        type = 'dummy';" ^
    "        ygg = @{" ^
    "            extra = @{ clientToken = $token; userName = $newName };" ^
    "            iat = [int][DateTimeOffset]::UtcNow.ToUnixTimeSeconds();" ^
    "            token = $newName;" ^
    "        };" ^
    "    })" ^
    "};" ^
    "$jsonStr = ($accObj | ConvertTo-Json -Depth 5);" ^
    "$enc = [System.Text.UTF8Encoding]::new($false);" ^
    "$pData = Split-Path $dataAccounts -Parent;" ^
    "if (Test-Path -LiteralPath $pData) {" ^
    "    [System.IO.File]::WriteAllText($dataAccounts, $jsonStr + \"`r`n\", $enc);" ^
    "}" ^
    "$pRoot = Split-Path $rootAccounts -Parent;" ^
    "if (Test-Path -LiteralPath $pRoot) {" ^
    "    [System.IO.File]::WriteAllText($rootAccounts, $jsonStr + \"`r`n\", $enc);" ^
    "}" ^
    "Write-Host '';" ^
    "Write-Host ('[SUCESSO] Nome alterado com sucesso para: ' + $newName + '!') -ForegroundColor Green;" ^
    "Write-Host ('UUID offline gerado: ' + $uuid) -ForegroundColor DarkGray;" ^
    "Write-Host '';"

if errorlevel 1 (
    echo.
    if "%~1"=="" pause
    exit /b 1
)

echo.
echo Configuracao atualizada! Para iniciar o jogo, basta abrir o Jogar.bat.
if "%~1"=="" pause
exit /b 0
