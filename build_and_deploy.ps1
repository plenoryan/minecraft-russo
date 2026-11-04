# ==============================================================================
# Build & Dual Deployment Pipeline for SayIt (Minecraft 1.20.1 Fabric)
# Compiles src/**/*.java, injects bytecodes via jar.exe uf, and deploys
# to both Instancia_Cliente\mods and Servidor_Dedicado\mods.
# ==============================================================================

[CmdletBinding()]
param(
    [string]$TargetRoot = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"

# Enforce UTF-8 Console Output
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " SAYIT MODPACK (FABRIC 1.20.1) - BUILD & DEPLOYMENT PIPELINE" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " Target Directory : $TargetRoot" -ForegroundColor Gray
Write-Host " Timestamp (UTC)  : $([DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ'))" -ForegroundColor Gray
Write-Host ""

# 1. Validate Prerequisites and Toolchain
$javaExe  = Join-Path $TargetRoot "Java17\bin\java.exe"
$javacExe = Join-Path $TargetRoot "Java17\bin\javac.exe"
$jarExe   = Join-Path $TargetRoot "Java17\bin\jar.exe"

if (-not (Test-Path $javacExe)) { throw "OpenJDK 17 javac.exe not found at '$javacExe'" }
if (-not (Test-Path $jarExe))   { throw "OpenJDK 17 jar.exe not found at '$jarExe'" }
if (-not (Test-Path $javaExe))  { throw "OpenJDK 17 java.exe not found at '$javaExe'" }

$srcDir = Join-Path $TargetRoot "src"
if (-not (Test-Path $srcDir)) { throw "Source directory not found at '$srcDir'" }

$clientModJar = Join-Path $TargetRoot "Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar"
$serverModJar = Join-Path $TargetRoot "Servidor_Dedicado\mods\sayitenglish-fabric-1.20.1-1.1.0.jar"
if (-not (Test-Path $clientModJar)) { throw "Base client mod JAR not found at '$clientModJar'" }

$clientIntermediaryJar = Join-Path $TargetRoot "Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar"
if (-not (Test-Path $clientIntermediaryJar)) { throw "Client intermediary JAR not found at '$clientIntermediaryJar'" }

$jadeJar = Join-Path $TargetRoot "Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar"
if (-not (Test-Path $jadeJar)) { throw "Jade mod JAR not found at '$jadeJar'" }

Write-Host "[1/5] Validating toolchain and libraries..." -ForegroundColor Yellow
$serverLibDir = Join-Path $TargetRoot "Servidor_Dedicado\libraries"
$clientLibDir = Join-Path $TargetRoot "Launcher\data\libraries"

$serverLibraries = @()
if (Test-Path $serverLibDir) {
    $serverLibraries = Get-ChildItem -Recurse -Path $serverLibDir -Filter "*.jar" | 
        Where-Object { $_.Name -notmatch "sponge-mixin" } | 
        Select-Object -ExpandProperty FullName
}

$clientLibraries = @()
if (Test-Path $clientLibDir) {
    $clientLibraries = Get-ChildItem -Recurse -Path $clientLibDir -Filter "*.jar" | 
        Select-Object -ExpandProperty FullName
}

$classpathJars = @(
    $clientModJar,
    $clientIntermediaryJar,
    $jadeJar
) + $serverLibraries + $clientLibraries

$classpath = $classpathJars -join ";"
Write-Host "      Toolchain: OpenJDK 17 ($(Split-Path $javacExe))" -ForegroundColor Gray
Write-Host "      Classpath JAR count: $($classpathJars.Count) JARs" -ForegroundColor Gray

# 2. Compile Java Source Files
Write-Host "`n[2/5] Compiling Java source files with javac..." -ForegroundColor Yellow
$javaFiles = Get-ChildItem -Recurse -Path $srcDir -Filter "*.java" | Select-Object -ExpandProperty FullName
if ($javaFiles.Count -eq 0) { throw "No .java files found under '$srcDir'" }
Write-Host "      Found $($javaFiles.Count) Java source files in '$srcDir'" -ForegroundColor Gray

$stagingDir = Join-Path ([System.IO.Path]::GetTempPath()) ("sayit_build_" + [Guid]::NewGuid().ToString("N"))
$classesDir = Join-Path $stagingDir "classes"
New-Item -ItemType Directory -Path $classesDir -Force | Out-Null

try {
    & $javacExe -encoding UTF-8 -cp $classpath -d $classesDir $javaFiles
    if ($LASTEXITCODE -ne 0) {
        throw "javac compilation failed with exit code $LASTEXITCODE"
    }

    $compiledClasses = Get-ChildItem -Recurse -Path $classesDir -Filter "*.class"
    Write-Host "      [SUCCESS] Compiled $($compiledClasses.Count) .class files successfully (exit code 0)" -ForegroundColor Green

    # 3. Inject Compiled Classes into Mod JAR via jar.exe uf
    Write-Host "`n[3/5] Packaging and injecting classes via jar.exe uf..." -ForegroundColor Yellow
    $stagingJar = Join-Path $stagingDir "sayitenglish-fabric-1.20.1-1.1.0.jar"
    
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $extractDir = Join-Path $stagingDir "jar_extract"
    New-Item -ItemType Directory -Path $extractDir -Force | Out-Null
    [System.IO.Compression.ZipFile]::ExtractToDirectory($clientModJar, $extractDir)
    
    $manifestPath = Join-Path $extractDir "META-INF\MANIFEST.MF"
    & $jarExe cfm $stagingJar $manifestPath -C $extractDir .
    if ($LASTEXITCODE -ne 0) { throw "jar.exe cfm failed to assemble base JAR (exit code $LASTEXITCODE)" }
    Remove-Item -Recurse -Force $extractDir

    # Authoritative injection of compiled bytecodes via jar.exe uf
    & $jarExe uf $stagingJar -C $classesDir com
    if ($LASTEXITCODE -ne 0) { throw "jar.exe uf failed to inject classes (exit code $LASTEXITCODE)" }
    
    Write-Host "      [SUCCESS] Injected bytecodes into '$stagingJar' via jar.exe uf (exit code 0)" -ForegroundColor Green

    # Verify key classes in staging JAR
    $jarEntries = & $jarExe tf $stagingJar
    $requiredClasses = @(
        "com/spicesfire/sayitenglish/Alphabet.class",
        "com/spicesfire/sayitenglish/ChallengeManager.class",
        "com/spicesfire/sayitenglish/ChallengeManager`$CoopManager.class",
        "com/spicesfire/sayitenglish/Packets.class",
        "com/spicesfire/sayitenglish/Packets`$CoopInvoke.class",
        "com/spicesfire/sayitenglish/Packets`$CoopCue.class",
        "com/spicesfire/sayitenglish/Packets`$CoopSync.class",
        "com/spicesfire/sayitenglish/Progress.class",
        "com/spicesfire/sayitenglish/client/ChallengeHud.class",
        "com/spicesfire/sayitenglish/client/ChallengeSession.class"
    )
    foreach ($rc in $requiredClasses) {
        if ($jarEntries -notcontains $rc) {
            throw "Required class '$rc' not found in freshly built JAR!"
        }
    }
    Write-Host "      [SUCCESS] Verified all $($requiredClasses.Count) required milestone classes inside JAR" -ForegroundColor Green

    # 4. Dual Deployment to Client and Server
    Write-Host "`n[4/5] Deploying freshly built JAR to Client and Dedicated Server..." -ForegroundColor Yellow
    
    $clientModsDir = Split-Path $clientModJar -Parent
    $serverModsDir = Split-Path $serverModJar -Parent
    if (-not (Test-Path $clientModsDir)) { New-Item -ItemType Directory -Path $clientModsDir -Force | Out-Null }
    if (-not (Test-Path $serverModsDir)) { New-Item -ItemType Directory -Path $serverModsDir -Force | Out-Null }

    Copy-Item -Path $stagingJar -Destination $clientModJar -Force
    Copy-Item -Path $stagingJar -Destination $serverModJar -Force

    # 5. Post-Deployment Verification
    Write-Host "`n[5/5] Verifying deployed artifacts..." -ForegroundColor Yellow
    $targets = @(
        @{ Name = "Client Mod JAR"; Path = $clientModJar },
        @{ Name = "Server Mod JAR"; Path = $serverModJar }
    )

    $now = Get-Date
    foreach ($target in $targets) {
        $item = Get-Item $target.Path
        if (-not $item.Exists) { throw "$($target.Name) missing at '$($target.Path)'" }
        if ($item.Length -lt 10000000) { throw "$($target.Name) size ($($item.Length) bytes) is suspiciously small" }
        
        $ageSeconds = ($now - $item.LastWriteTime).TotalSeconds
        Write-Host "      $($target.Name): $($item.Length) bytes, modified $($item.LastWriteTime.ToString('HH:mm:ss')) (age: $([Math]::Round($ageSeconds, 1))s) -> OK" -ForegroundColor Green
    }

    Write-Host "`n==============================================================================" -ForegroundColor Cyan
    Write-Host " BUILD & DUAL DEPLOYMENT COMPLETED SUCCESSFULLY (100% PASS)" -ForegroundColor Cyan
    Write-Host "==============================================================================" -ForegroundColor Cyan
    Write-Host " Client: $clientModJar" -ForegroundColor Gray
    Write-Host " Server: $serverModJar" -ForegroundColor Gray
    Write-Host ""
    exit 0
}
finally {
    if (Test-Path $stagingDir) {
        Remove-Item -Recurse -Force $stagingDir -ErrorAction SilentlyContinue
    }
}
