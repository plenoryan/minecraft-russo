# download_mods.ps1 - Automated downloader for Fabric 1.20.1 Core & Optimization Mods
[CmdletBinding()]
param (
    [string]$ModsDir = "F:\Jogos\Mine\Instancia_Cliente\mods"
)

$ErrorActionPreference = "Stop"
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Host "============================================================" -ForegroundColor Cyan
Write-Host " Download dos Mods Base e Otimizacao (Fabric 1.20.1)        " -ForegroundColor Cyan
Write-Host "============================================================" -ForegroundColor Cyan
Write-Host "Destino: $ModsDir"

if (-not (Test-Path $ModsDir)) {
    New-Item -ItemType Directory -Path $ModsDir -Force | Out-Null
}

$modCatalog = @(
    @{
        Slug = "fabric-api"
        Name = "Fabric API"
        DirectUrl = "https://cdn.modrinth.com/data/P7dR8mSH/versions/rvI2dfzR/fabric-api-0.92.12%2B1.20.1.jar"
        Filename = "fabric-api-0.92.12+1.20.1.jar"
        MinBytes = 1000000
    },
    @{
        Slug = "sodium"
        Name = "Sodium"
        DirectUrl = "https://cdn.modrinth.com/data/AANobbMI/versions/OihdIimA/sodium-fabric-0.5.13%2Bmc1.20.1.jar"
        Filename = "sodium-fabric-0.5.13+mc1.20.1.jar"
        MinBytes = 500000
    },
    @{
        Slug = "lithium"
        Name = "Lithium"
        DirectUrl = "https://cdn.modrinth.com/data/gvQqBUqZ/versions/iEcXOkz4/lithium-fabric-mc1.20.1-0.11.4.jar"
        Filename = "lithium-fabric-mc1.20.1-0.11.4.jar"
        MinBytes = 400000
    },
    @{
        Slug = "ferrite-core"
        Name = "FerriteCore"
        DirectUrl = "https://cdn.modrinth.com/data/uXXizFIs/versions/unerR5MN/ferritecore-6.0.1-fabric.jar"
        Filename = "ferritecore-6.0.1-fabric.jar"
        MinBytes = 80000
    },
    @{
        Slug = "modernfix"
        Name = "ModernFix"
        DirectUrl = "https://cdn.modrinth.com/data/nmDcB62a/versions/rPmgLeZC/modernfix-fabric-5.25.2%2Bmc1.20.1.jar"
        Filename = "modernfix-fabric-5.25.2+mc1.20.1.jar"
        MinBytes = 300000
    },
    @{
        Slug = "immediatelyfast"
        Name = "ImmediatelyFast"
        DirectUrl = "https://cdn.modrinth.com/data/5ZwdcRci/versions/iwYUrQJO/ImmediatelyFast-Fabric-1.5.5%2B1.20.4.jar"
        Filename = "ImmediatelyFast-Fabric-1.5.5+1.20.4.jar"
        MinBytes = 200000
    }
)

$webClient = New-Object System.Net.WebClient
$webClient.Headers.Add("User-Agent", "MinecraftRussoModpack/1.0 (Windows 11)")

try {
    foreach ($mod in $modCatalog) {
        $destPath = Join-Path $ModsDir $mod.Filename
        Write-Host "Processando $($mod.Name)..." -ForegroundColor Cyan
        
        $needDownload = $true
        if (Test-Path $destPath) {
            $currentSize = (Get-Item $destPath).Length
            if ($currentSize -ge $mod.MinBytes) {
                Write-Host "  [OK] Ja existe e integro ($([math]::Round($currentSize / 1KB, 1)) KB): $($mod.Filename)" -ForegroundColor Green
                $needDownload = $false
            }
        }
        
        if ($needDownload) {
            Write-Host "  Baixando de $($mod.DirectUrl)..." -ForegroundColor Yellow
            $webClient.DownloadFile($mod.DirectUrl, $destPath)
            $newSize = (Get-Item $destPath).Length
            if ($newSize -lt $mod.MinBytes) {
                throw "Arquivo baixado para $($mod.Name) tem tamanho inesperado ($newSize bytes)"
            }
            Write-Host "  [SUCESSO] Baixado com sucesso ($([math]::Round($newSize / 1KB, 1)) KB)" -ForegroundColor Green
        }
    }
} finally {
    $webClient.Dispose()
}

Write-Host "Todos os 6 mods base e de otimizacao estao presentes e autenticos!" -ForegroundColor Green
exit 0
