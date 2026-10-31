# verify_m4.ps1 - Comprehensive Verification for Milestone M4: Offline Skins, Multiplayer Networking & Turnkey Packaging
[CmdletBinding()]
param (
    [string]$BaseDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   TEST SUITE: Milestone M4 Multiplayer, Skins & Turnkey Packaging   " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Diretorio Alvo: $BaseDir" -ForegroundColor Gray

$passedChecks = 0
$failedChecks = 0

function Assert-Test($condition, $description) {
    if ($condition) {
        Write-Host "  [PASS] $description" -ForegroundColor Green
        $script:passedChecks++
    } else {
        Write-Host "  [FAIL] $description" -ForegroundColor Red
        $script:failedChecks++
    }
}

Add-Type -AssemblyName System.IO.Compression.FileSystem

# -------------------------------------------------------------------------
# TEST GROUP 1: Client-Side Offline Skins & P2P Tunnel Mods
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 1/5] Mods Client-Side: CustomSkinLoader & e4all ---" -ForegroundColor Yellow

$clientModsDir = Join-Path $BaseDir "Instancia_Cliente\mods"

# CustomSkinLoader
$cslJars = Get-ChildItem -Path $clientModsDir -Filter "*CustomSkinLoader*.jar" -ErrorAction SilentlyContinue
Assert-Test ($cslJars.Count -gt 0) "CustomSkinLoader JAR presente em Instancia_Cliente\mods\"
if ($cslJars.Count -gt 0) {
    $cslJar = $cslJars[0]
    Assert-Test ($cslJar.Length -gt 150000) "CustomSkinLoader JAR tem tamanho valido ($($cslJar.Length) bytes)"
    
    $cslZip = [System.IO.Compression.ZipFile]::OpenRead($cslJar.FullName)
    $hasFabricJson = ($cslZip.Entries | Where-Object { $_.FullName -eq "fabric.mod.json" }) -ne $null
    $cslZip.Dispose()
    Assert-Test $hasFabricJson "CustomSkinLoader contem manifesto fabric.mod.json"
}

# e4all
$e4allJars = Get-ChildItem -Path $clientModsDir -Filter "*e4all*.jar" -ErrorAction SilentlyContinue
Assert-Test ($e4allJars.Count -gt 0) "e4all JAR presente em Instancia_Cliente\mods\"
if ($e4allJars.Count -gt 0) {
    $e4Jar = $e4allJars[0]
    Assert-Test ($e4Jar.Length -gt 500000) "e4all JAR tem tamanho valido ($($e4Jar.Length) bytes)"
    
    $e4Zip = [System.IO.Compression.ZipFile]::OpenRead($e4Jar.FullName)
    $e4ModJson = $e4Zip.Entries | Where-Object { $_.FullName -eq "fabric.mod.json" }
    Assert-Test ($e4ModJson -ne $null) "e4all contem manifesto fabric.mod.json"
    if ($e4ModJson) {
        $stream = $e4ModJson.Open()
        $reader = New-Object System.IO.StreamReader($stream)
        $modJsonContent = $reader.ReadToEnd()
        $reader.Dispose(); $stream.Dispose()
        Assert-Test ($modJsonContent -match '"id"\s*:\s*"e4all"') "e4all ID do mod verificado"
    }
    $e4Zip.Dispose()
}

# -------------------------------------------------------------------------
# TEST GROUP 2: Configuracao CustomSkinLoader (LocalSkin + Ely.by API)
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 2/5] Configuracao CustomSkinLoader e Pastas Locais ---" -ForegroundColor Yellow

$cslConfigPrimary = Join-Path $BaseDir "Instancia_Cliente\config\CustomSkinLoader\CustomSkinLoader.json"
$cslConfigSecondary = Join-Path $BaseDir "Instancia_Cliente\CustomSkinLoader\CustomSkinLoader.json"

Assert-Test (Test-Path $cslConfigPrimary) "CustomSkinLoader.json presente em config/CustomSkinLoader/"
Assert-Test (Test-Path $cslConfigSecondary) "CustomSkinLoader.json presente em CustomSkinLoader/"

if (Test-Path $cslConfigPrimary) {
    $cslRaw = Get-Content -Raw -Encoding UTF8 $cslConfigPrimary
    $cslJson = $cslRaw | ConvertFrom-Json
    
    $loadList = $cslJson.skinLoading.loadList
    $localSkin = $loadList | Where-Object { $_.name -eq "LocalSkin" -or $_.type -eq "Local" }
    $elyBy = $loadList | Where-Object { $_.name -eq "ElyBy" -or $_.type -eq "ElyByAPI" }
    
    Assert-Test ($localSkin -ne $null) "Provedor LocalSkin presente na lista de carregamento"
    Assert-Test ($localSkin.skin -match "CustomSkinLoader/LocalSkin/skins") "Caminho de skin local mapeado corretamente"
    Assert-Test ($elyBy -ne $null) "Provedor Ely.by presente na lista de carregamento"
    Assert-Test ($elyBy.root -match "ely\.by") "Endpoint da API Ely.by verificado"
}

$localSkinsDir = Join-Path $BaseDir "Instancia_Cliente\CustomSkinLoader\LocalSkin\skins"
Assert-Test (Test-Path $localSkinsDir) "Diretorio LocalSkin\skins existe para insercao de skins locais"

# -------------------------------------------------------------------------
# TEST GROUP 3: Servidor Dedicado (Estrutura, Properties, Mods, EULA)
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 3/5] Servidor Dedicado Fabric 1.20.1 ---" -ForegroundColor Yellow

$serverDir = Join-Path $BaseDir "Servidor_Dedicado"
$srvProps = Join-Path $serverDir "server.properties"
$srvEula = Join-Path $serverDir "eula.txt"
$srvLaunchJar = Join-Path $serverDir "fabric-server-launch.jar"
$srvModsDir = Join-Path $serverDir "mods"

Assert-Test (Test-Path $serverDir) "Diretorio Servidor_Dedicado/ existe"
Assert-Test (Test-Path $srvProps) "server.properties presente"
Assert-Test (Test-Path $srvEula) "eula.txt presente"
Assert-Test (Test-Path $srvLaunchJar) "fabric-server-launch.jar presente"

if (Test-Path $srvProps) {
    $propsContent = Get-Content -Raw -Encoding UTF8 $srvProps
    Assert-Test ($propsContent -match "online-mode\s*=\s*false") "server.properties contem online-mode=false"
    Assert-Test ($propsContent -match "enforce-secure-profile\s*=\s*false") "server.properties contem enforce-secure-profile=false"
    Assert-Test ($propsContent -match "server-port\s*=\s*25565") "server.properties contem porta 25565"
    Assert-Test ($propsContent -match "motd\s*=") "server.properties contem MOTD customizado"
}

if (Test-Path $srvEula) {
    $eulaContent = Get-Content -Raw -Encoding UTF8 $srvEula
    Assert-Test ($eulaContent -match "eula\s*=\s*true") "eula.txt contem eula=true"
}

if (Test-Path $srvLaunchJar) {
    $srvJarItem = Get-Item $srvLaunchJar
    Assert-Test ($srvJarItem.Length -gt 50000) "fabric-server-launch.jar tem tamanho integro ($($srvJarItem.Length) bytes)"
}

if (Test-Path $srvModsDir) {
    $srvJars = Get-ChildItem -Path $srvModsDir -Filter "*.jar" -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Name
    
    $hasFabricApi = ($srvJars | Where-Object { $_ -match "fabric-api" }) -ne $null
    $hasSayIt = ($srvJars | Where-Object { $_ -match "sayit" }) -ne $null
    $hasTailor = ($srvJars | Where-Object { $_ -match "fabric-tailor" -or $_ -match "fabrictailor" }) -ne $null
    $hasLithium = ($srvJars | Where-Object { $_ -match "lithium" }) -ne $null
    $hasFerrite = ($srvJars | Where-Object { $_ -match "ferritecore" }) -ne $null
    
    Assert-Test $hasFabricApi "Servidor contem Fabric API ($($srvJars | Where-Object { $_ -match 'fabric-api' }))"
    Assert-Test $hasSayIt "Servidor contem SayIt ($($srvJars | Where-Object { $_ -match 'sayit' }))"
    Assert-Test $hasTailor "Servidor contem FabricTailor para skins ($($srvJars | Where-Object { $_ -match 'fabric-tailor|fabrictailor' }))"
    Assert-Test $hasLithium "Servidor contem Lithium ($($srvJars | Where-Object { $_ -match 'lithium' }))"
    Assert-Test $hasFerrite "Servidor contem FerriteCore ($($srvJars | Where-Object { $_ -match 'ferritecore' }))"
    
    # Validar ausencia de mods client-only de renderizacao/HUD
    $clientOnlyList = @("sodium", "modernui", "immediatelyfast", "customskinloader")
    foreach ($c in $clientOnlyList) {
        $found = $srvJars | Where-Object { $_ -match $c }
        Assert-Test ($found -eq $null) "Servidor exclui mod client-only: $c"
    }
}

# -------------------------------------------------------------------------
# TEST GROUP 4: 1-Click Server Launcher (Iniciar_Servidor.bat)
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 4/5] Launcher do Servidor (Iniciar_Servidor.bat) ---" -ForegroundColor Yellow

$serverBatPath = Join-Path $BaseDir "Iniciar_Servidor.bat"
Assert-Test (Test-Path $serverBatPath) "Iniciar_Servidor.bat presente no diretorio raiz"

if (Test-Path $serverBatPath) {
    $batContent = Get-Content -Raw -Encoding UTF8 $serverBatPath
    Assert-Test ($batContent -match 'chcp\s+65001') "Iniciar_Servidor.bat utiliza UTF-8 (chcp 65001)"
    Assert-Test ($batContent -match 'Java17\\bin\\java\.exe') "Iniciar_Servidor.bat detecta Java 17 embutido"
    Assert-Test ($batContent -match 'eula\.txt') "Iniciar_Servidor.bat valida aceitacao do EULA"
    Assert-Test ($batContent -match 'online-mode=false') "Iniciar_Servidor.bat valida online-mode=false"
    Assert-Test ($batContent -match 'enforce-secure-profile=false') "Iniciar_Servidor.bat valida enforce-secure-profile=false"
    Assert-Test ($batContent -match 'UseG1GC') "Iniciar_Servidor.bat contem flags Aikar G1GC (-XX:+UseG1GC)"
    Assert-Test ($batContent -match 'ParallelRefProcEnabled') "Iniciar_Servidor.bat contem flag ParallelRefProcEnabled"
    Assert-Test ($batContent -match 'fabric-server-launch\.jar') "Iniciar_Servidor.bat invoca fabric-server-launch.jar"
    Assert-Test ($batContent -match 'pause') "Iniciar_Servidor.bat contem pause para retencao de logs em crash"
}

# -------------------------------------------------------------------------
# TEST GROUP 5: Guia de Distribuicao para Amigos (GUIA_DISTRIBUICAO_AMIGOS.md)
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 5/5] Guia de Distribuicao em Portugues ---" -ForegroundColor Yellow

$guidePath = Join-Path $BaseDir "GUIA_DISTRIBUICAO_AMIGOS.md"
Assert-Test (Test-Path $guidePath) "GUIA_DISTRIBUICAO_AMIGOS.md presente no diretorio raiz"

if (Test-Path $guidePath) {
    $guideContent = Get-Content -Raw -Encoding UTF8 $guidePath
    Assert-Test ($guideContent.Length -gt 2500) "Guia tem tamanho detalhado ($($guideContent.Length) caracteres)"
    
    $requiredKeywords = @("microfone", "offline", "servidor", "amigos", "e4all", "24454", "voz", "Jogar.bat", "LocalSkin", "Ely.by")
    foreach ($kw in $requiredKeywords) {
        Assert-Test ($guideContent -match [regex]::Escape($kw)) "Guia contem topico essencial: $kw"
    }
}

# -------------------------------------------------------------------------
# SUMARIO FINAL
# -------------------------------------------------------------------------
Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "   RESUMO DA VERIFICACAO DO MILESTONE M4                             " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Total de Assercoes   : $($passedChecks + $failedChecks)" -ForegroundColor Gray
Write-Host " Assercoes Aprovadas  : $passedChecks" -ForegroundColor Green
Write-Host " Assercoes Falhas     : $failedChecks" -ForegroundColor $(if ($failedChecks -eq 0) { "Green" } else { "Red" })

if ($failedChecks -gt 0) {
    Write-Host "`n[FALHA] Foram encontrados $failedChecks erros na verificacao do M4." -ForegroundColor Red
    exit 1
}

Write-Host "`n[SUCESSO] 100% dos testes do Milestone M4 foram aprovados com sucesso!" -ForegroundColor Green
exit 0
