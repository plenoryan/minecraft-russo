# install_java17.ps1 - Automated Eclipse Temurin JDK 17 Portable Installer for Windows x64
[CmdletBinding()]
param (
    [string]$TargetDir = "F:\Jogos\Mine\Java17"
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " Instalador do OpenJDK 17 Portatil (Eclipse Temurin)       " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Destino: $TargetDir"

$javaExe = Join-Path $TargetDir "bin\java.exe"
$javawExe = Join-Path $TargetDir "bin\javaw.exe"

if ((Test-Path $javaExe) -and (Test-Path $javawExe)) {
    Write-Host "[INFO] Java 17 ja detectado em $TargetDir. Testando execucao..." -ForegroundColor Yellow
    try {
        $ver = cmd.exe /c "`"$javaExe`" -version 2>&1"
        $verStr = ($ver | Out-String).Trim()
        Write-Host "[OK] Versao atual:`n$verStr" -ForegroundColor Green
        if ($verStr -match '17\.') {
            Write-Host "[OK] Runtime Java 17 ja instalado e funcional!" -ForegroundColor Green
            exit 0
        }
    } catch {
        Write-Host "[AVISO] Falha ao executar runtime existente, reinstalando..." -ForegroundColor Yellow
    }
}

if (-not (Test-Path $TargetDir)) {
    New-Item -ItemType Directory -Path $TargetDir -Force | Out-Null
}

$apiUrl = "https://api.adoptium.net/v3/assets/latest/17/hotspot?architecture=x64&image_type=jdk&os=windows&vendor=eclipse"
Write-Host "[1/4] Consultando Adoptium API para obter ultimo release..." -ForegroundColor Cyan

try {
    $resp = Invoke-RestMethod -Uri $apiUrl -Method Get -TimeoutSec 30
    $pkg = $resp[0].binary.package
    $downloadUrl = $pkg.link
    $pkgName = $pkg.name
    $expectedSize = $pkg.size
    Write-Host "[OK] Pacote identificado: $pkgName ($([math]::Round($expectedSize / 1MB, 2)) MB)" -ForegroundColor Green
    Write-Host "     URL: $downloadUrl"
} catch {
    Write-Host "[ERRO] Falha ao consultar Adoptium API: $_" -ForegroundColor Red
    # Fallback to direct latest Temurin 17 URL
    $downloadUrl = "https://github.com/adoptium/temurin17-binaries/releases/download/jdk-17.0.20.1%2B1/OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1.zip"
    $pkgName = "OpenJDK17U-jdk_x64_windows_hotspot_17.0.20.1_1.zip"
    Write-Host "[AVISO] Usando URL direta de fallback: $downloadUrl" -ForegroundColor Yellow
}

$tempZip = Join-Path $env:TEMP $pkgName
Write-Host "[2/4] Baixando pacote para $tempZip..." -ForegroundColor Cyan

$webClient = New-Object System.Net.WebClient
try {
    $webClient.DownloadFile($downloadUrl, $tempZip)
    $actualSize = (Get-Item $tempZip).Length
    Write-Host "[OK] Download concluido ($([math]::Round($actualSize / 1MB, 2)) MB)." -ForegroundColor Green
} catch {
    Write-Host "[ERRO] Falha no download: $_" -ForegroundColor Red
    exit 1
} finally {
    $webClient.Dispose()
}

$extractTemp = Join-Path $env:TEMP ("temurin17_extract_" + [System.Guid]::NewGuid().ToString("N"))
Write-Host "[3/4] Extraindo arquivos temporariamente em $extractTemp..." -ForegroundColor Cyan
New-Item -ItemType Directory -Path $extractTemp -Force | Out-Null

try {
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    [System.IO.Compression.ZipFile]::ExtractToDirectory($tempZip, $extractTemp)
    
    # Identify the root directory inside the zip (typically jdk-17.0.x+y)
    $innerDirs = Get-ChildItem -Path $extractTemp -Directory
    if ($innerDirs.Count -eq 1 -and (Test-Path (Join-Path $innerDirs[0].FullName "bin"))) {
        $sourceDir = $innerDirs[0].FullName
    } else {
        $sourceDir = $extractTemp
    }
    
    Write-Host "     Movendo arquivos para $TargetDir..." -ForegroundColor Cyan
    Get-ChildItem -Path $sourceDir | ForEach-Object {
        $destPath = Join-Path $TargetDir $_.Name
        if (Test-Path $destPath) {
            Remove-Item -Path $destPath -Recurse -Force
        }
        Move-Item -Path $_.FullName -Destination $destPath -Force
    }
    Write-Host "[OK] Extracao e configuracao concluidas com sucesso." -ForegroundColor Green
} finally {
    if (Test-Path $extractTemp) {
        Remove-Item -Path $extractTemp -Recurse -Force -ErrorAction SilentlyContinue
    }
    if (Test-Path $tempZip) {
        Remove-Item -Path $tempZip -Force -ErrorAction SilentlyContinue
    }
}

Write-Host "[4/4] Validando instalacao do Java 17..." -ForegroundColor Cyan
if (-not (Test-Path $javaExe)) {
    Write-Host "[ERRO] bin\java.exe nao encontrado em $TargetDir" -ForegroundColor Red
    exit 1
}

$testOutput = cmd.exe /c "`"$javaExe`" -version 2>&1"
$outText = ($testOutput | Out-String).Trim()
Write-Host "[SUCESSO] Java 17 instalado e verificado:`n$outText" -ForegroundColor Green
exit 0
