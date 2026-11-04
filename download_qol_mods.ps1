<#
.SYNOPSIS
    Downloads and installs curated Fabric 1.20.1 QoL and dynamism mods.

.DESCRIPTION
    Requirement R4: Integration of Quality of Life and Dynamism mods for SayIt (Fabric 1.20.1).
    Downloads 6 verified zero-vocabulary utility mods from Modrinth CDN:
      1. omnimovement (Client only) - 360-degree sprint & 1-block auto step-up
      2. InventorySorter (Client + Server) - Middle-click container sorting
      3. stack-to-nearby-chests (Client only) - Terraria-style quick stack
      4. entityculling (Client only) - Occlusion culling for entity rendering
      5. krypton (Client + Server) - Netty pipeline optimization for multiplayer
      6. appleskin (Client only) - Saturation & food tooltip overlay

    Strictly enforces client/server segregation per AGENTS.md Lesson L13.
    Verifies non-zero byte size and exact SHA-512 checksum for every downloaded JAR.

.PARAMETER BaseDir
    Root directory of the SayIt project. Defaults to script root or "F:\Jogos\Mine".
#>

[CmdletBinding()]
param(
    [Parameter(Position = 0)]
    [string]$BaseDir = "F:\Jogos\Mine"
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

# Force TLS 1.2 for Modrinth CDN connections
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Host "=================================================================" -ForegroundColor Cyan
Write-Host " SayIt Fabric 1.20.1 - QoL & Dynamism Mod Provisioning Pipeline " -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

$clientModsDir = Join-Path $BaseDir "Instancia_Cliente\mods"
$serverModsDir = Join-Path $BaseDir "Servidor_Dedicado\mods"
$tempDownloadDir = Join-Path $env:TEMP "sayit_qol_downloads"

if (-not (Test-Path $clientModsDir)) {
    throw "Client mods directory not found: $clientModsDir"
}
if (-not (Test-Path $serverModsDir)) {
    throw "Server mods directory not found: $serverModsDir"
}
if (-not (Test-Path $tempDownloadDir)) {
    New-Item -ItemType Directory -Path $tempDownloadDir -Force | Out-Null
}

# Mod definitions table with live Modrinth v2 metadata
$qolMods = @(
    @{
        Name        = "Omnimovement"
        Slug        = "omnimovement"
        Filename    = "omnimovement-fabric-1.0+mc1.20.1.jar"
        Url         = "https://cdn.modrinth.com/data/ZmKZ0vTP/versions/zDpV6cM8/omnimovement-fabric-1.0%2Bmc1.20.1.jar"
        ExpectedSize= 29222
        Sha512      = "a4e8b6ab705f146a7e059825edcc0da612335d6cf84734f5d4ed381cb4a0bac29389ba6b52a8d3c521dd3d676ed1f99e4f6a6561812cc57d32d8a39d3417325e"
        IsClient    = $true
        IsServer    = $false
        Description = "360-degree omnidirectional sprinting and 1-block auto step-up"
    },
    @{
        Name        = "Inventory Sorting"
        Slug        = "inventory-sorting"
        Filename    = "InventorySorter-1.9.0-1.20.jar"
        Url         = "https://cdn.modrinth.com/data/5ibSyLAz/versions/tr2ZGtz4/InventorySorter-1.9.0-1.20.jar"
        ExpectedSize= 222894
        Sha512      = "bb8f71a53940e82ddc20918b64602b2f6dfa289055966e43d1c324266d24ef06161d4a0d71a9abda6081d53559c1bd8264187ae369583bfea9a8c3ef9dc59c93"
        IsClient    = $true
        IsServer    = $true
        Description = "Middle-click chest & inventory sorting with 0 dependencies"
    },
    @{
        Name        = "Stack to Nearby Chests"
        Slug        = "stack-to-nearby-chests"
        Filename    = "stack-to-nearby-chests-mc1.20.1-0.5.10.jar"
        Url         = "https://cdn.modrinth.com/data/HtGckJVc/versions/wMJlsfCI/stack-to-nearby-chests-mc1.20.1-0.5.10.jar"
        ExpectedSize= 559703
        Sha512      = "6f2efa75879ac5f164d962fc321e77a418f05b0be3b0fa504be1ecd47ccb0b6e129841c1598415e09c67f796f4f830b560fee4cd6885e694909143978c11b1ec"
        IsClient    = $true
        IsServer    = $false
        Description = "Terraria-style quick stack & restock to openable containers"
    },
    @{
        Name        = "Entity Culling"
        Slug        = "entityculling"
        Filename    = "entityculling-fabric-1.11.3-mc1.20.1.jar"
        Url         = "https://cdn.modrinth.com/data/NNAgCjsB/versions/xSrd3qsM/entityculling-fabric-1.11.3-mc1.20.1.jar"
        ExpectedSize= 1516696
        Sha512      = "6bb4af050ec3bb5f4e2f76b33abc2bb6e0096c94849ba728979200beddeed8b2c3a5a962ecbec9cf79f11a99ba996a847814f1aabdb8ffad54105800fc0fed72"
        IsClient    = $true
        IsServer    = $false
        Description = "Occlusion culling to skip rendering blocked entities behind walls"
    },
    @{
        Name        = "Krypton"
        Slug        = "krypton"
        Filename    = "krypton-0.2.3.jar"
        Url         = "https://cdn.modrinth.com/data/fQEb0iXm/versions/jiDwS0W1/krypton-0.2.3.jar"
        ExpectedSize= 185895
        Sha512      = "92b73a70737cfc1daebca211bd1525de7684b554be392714ee29cbd558f2a27a8bdda22accbe9176d6e531d74f9bf77798c28c3e8559c970f607422b6038bc9e"
        IsClient    = $true
        IsServer    = $true
        Description = "Netty stack direct buffer optimization for multiplayer sessions"
    },
    @{
        Name        = "AppleSkin"
        Slug        = "appleskin"
        Filename    = "appleskin-fabric-mc1.20.1-2.5.2.jar"
        Url         = "https://cdn.modrinth.com/data/EsAfCjCV/versions/N5XeV21r/appleskin-fabric-mc1.20.1-2.5.2.jar"
        ExpectedSize= 1097240
        Sha512      = "712ca0f86050ab3c6dbdbe62c5d9fabe77837d70529d1cd686f915a5f09d3a4f71ad4d3866984fbdded63ae2eb1c1ceedfb3386253510ee3a54857f77677e4f4"
        IsClient    = $true
        IsServer    = $false
        Description = "Food saturation & hunger HUD restoration preview"
    }
)

$webClient = New-Object System.Net.WebClient
$webClient.Headers.Add("User-Agent", "SayItModProvisioner/1.0 (Windows NT 10.0; Win64; x64)")

$failures = 0

foreach ($mod in $qolMods) {
    Write-Host "`n>>> Processing: $($mod.Name) ($($mod.Filename))" -ForegroundColor Yellow
    Write-Host "    Scope: $(if ($mod.IsClient -and $mod.IsServer) { 'Universal (Client + Server)' } elseif ($mod.IsClient) { 'Client-Only' } else { 'Server-Only' })" -ForegroundColor DarkGray
    Write-Host "    Purpose: $($mod.Description)" -ForegroundColor DarkGray

    $stagedFile = Join-Path $tempDownloadDir $mod.Filename

    # Check if staging file already exists and matches hash
    $needsDownload = $true
    if (Test-Path $stagedFile) {
        $existingHash = (Get-FileHash -Path $stagedFile -Algorithm SHA512).Hash.ToLowerInvariant()
        if ($existingHash -eq $mod.Sha512.ToLowerInvariant()) {
            Write-Host "    [STAGING] Valid cached file found in staging." -ForegroundColor Green
            $needsDownload = $false
        } else {
            Write-Host "    [STAGING] Cached file hash mismatch. Re-downloading..." -ForegroundColor Yellow
        }
    }

    if ($needsDownload) {
        Write-Host "    Downloading from $($mod.Url)..." -ForegroundColor Cyan
        try {
            $webClient.DownloadFile($mod.Url, $stagedFile)
            Write-Host "    Download complete." -ForegroundColor Green
        } catch {
            Write-Host "    [ERROR] Failed to download $($mod.Filename): $($_.Exception.Message)" -ForegroundColor Red
            $failures++
            continue
        }
    }

    # Verify byte size and SHA-512
    $item = Get-Item $stagedFile
    if ($item.Length -le 0) {
        Write-Host "    [ERROR] Downloaded file is empty (0 bytes)!" -ForegroundColor Red
        $failures++
        continue
    }

    $actualHash = (Get-FileHash -Path $stagedFile -Algorithm SHA512).Hash.ToLowerInvariant()
    if ($actualHash -ne $mod.Sha512.ToLowerInvariant()) {
        Write-Host "    [ERROR] SHA-512 hash mismatch!" -ForegroundColor Red
        Write-Host "      Expected: $($mod.Sha512)" -ForegroundColor Red
        Write-Host "      Actual:   $actualHash" -ForegroundColor Red
        $failures++
        continue
    }
    Write-Host "    [INTEGRITY] SHA-512 verified ($($item.Length) bytes)." -ForegroundColor Green

    # Deploy to Client
    if ($mod.IsClient) {
        $clientTarget = Join-Path $clientModsDir $mod.Filename
        Copy-Item -Path $stagedFile -Destination $clientTarget -Force
        Write-Host "    [DEPLOY CLIENT] Placed into $clientTarget" -ForegroundColor Green
    }

    # Deploy to Server
    if ($mod.IsServer) {
        $serverTarget = Join-Path $serverModsDir $mod.Filename
        Copy-Item -Path $stagedFile -Destination $serverTarget -Force
        Write-Host "    [DEPLOY SERVER] Placed into $serverTarget" -ForegroundColor Green
    } else {
        # Strict enforcement: ensure client-only mod is NOT in dedicated server
        $prohibitedServerTarget = Join-Path $serverModsDir $mod.Filename
        if (Test-Path $prohibitedServerTarget) {
            Write-Host "    [SEGREGATION] Removing client-only mod from server: $prohibitedServerTarget" -ForegroundColor Yellow
            Remove-Item -Path $prohibitedServerTarget -Force
        }
    }
}

$webClient.Dispose()

Write-Host "`n=================================================================" -ForegroundColor Cyan
Write-Host " Verification & Segregation Audit" -ForegroundColor Cyan
Write-Host "=================================================================" -ForegroundColor Cyan

# Audit Client directory
Write-Host "`n--- Client Mods Audit ($clientModsDir) ---" -ForegroundColor White
$clientExpected = @(
    "omnimovement-fabric-1.0+mc1.20.1.jar",
    "InventorySorter-1.9.0-1.20.jar",
    "stack-to-nearby-chests-mc1.20.1-0.5.10.jar",
    "entityculling-fabric-1.11.3-mc1.20.1.jar",
    "krypton-0.2.3.jar",
    "appleskin-fabric-mc1.20.1-2.5.2.jar"
)

foreach ($f in $clientExpected) {
    $p = Join-Path $clientModsDir $f
    if (Test-Path $p) {
        $len = (Get-Item $p).Length
        Write-Host "  [OK] Client mod present: $f ($len bytes)" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Missing client mod: $f" -ForegroundColor Red
        $failures++
    }
}

# Audit Server directory
Write-Host "`n--- Server Mods Audit ($serverModsDir) ---" -ForegroundColor White
$serverUniversal = @(
    "InventorySorter-1.9.0-1.20.jar",
    "krypton-0.2.3.jar"
)
$serverClientOnly = @(
    "omnimovement-fabric-1.0+mc1.20.1.jar",
    "stack-to-nearby-chests-mc1.20.1-0.5.10.jar",
    "entityculling-fabric-1.11.3-mc1.20.1.jar",
    "appleskin-fabric-mc1.20.1-2.5.2.jar"
)

foreach ($f in $serverUniversal) {
    $p = Join-Path $serverModsDir $f
    if (Test-Path $p) {
        $len = (Get-Item $p).Length
        Write-Host "  [OK] Universal mod present on server: $f ($len bytes)" -ForegroundColor Green
    } else {
        Write-Host "  [FAIL] Missing universal mod on server: $f" -ForegroundColor Red
        $failures++
    }
}

foreach ($f in $serverClientOnly) {
    $p = Join-Path $serverModsDir $f
    if (Test-Path $p) {
        Write-Host "  [FAIL] VIOLATION: Client-only mod detected in dedicated server: $f" -ForegroundColor Red
        $failures++
    } else {
        Write-Host "  [OK] Segregation confirmed: $f is NOT in dedicated server" -ForegroundColor Green
    }
}

if ($failures -gt 0) {
    Write-Host "`n[SUMMARY] Provisioning completed with $failures failure(s)!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n[SUMMARY] All 6 QoL mods provisioned, verified, and segregated successfully (100% PASS)." -ForegroundColor Green
    exit 0
}
