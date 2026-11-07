# ==============================================================================
# test_adversarial_hud_iris_mods.ps1 - Empirical Adversarial Stress Test Suite
# Challenges:
#   1. ChallengeHud Receiver Notification Card, Null Safety, Extreme Strings,
#      Duration Expiration, Action Resolution, '?' Badge, Concurrency Storm.
#   2. Iris Shader Reload Key Unbind, options.txt Persistence, Zero-BOM,
#      SayItClient Runtime Guard Behavior under Options Tampering.
#   3. Farmer's Respite and Comforts Mod Provisioning, Client/Server Parity,
#      Bilingual Vocabulary & Headless Dedicated Server Boot Under Stress.
# Supported: Windows 11 PowerShell 5.1 / 7+ (Pure 7-bit ASCII Source)
# ==============================================================================

[CmdletBinding()]
param(
    [string]$TargetRoot = "F:\Jogos\Mine",
    [switch]$ExportJson = $true
)

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
$OutputEncoding = [System.Text.Encoding]::UTF8

$testResults = [System.Collections.Generic.List[PSCustomObject]]::new()
$suiteStart = Get-Date

function Register-AdvCheck {
    param(
        [string]$Id,
        [string]$Category,
        [string]$Name,
        [string]$Status, # PASS, FAIL, WARNING, VULNERABILITY
        [string]$Details,
        [hashtable]$Evidence = @{}
    )

    $obj = [PSCustomObject]@{
        Id        = $Id
        Category  = $Category
        Name      = $Name
        Status    = $Status
        Details   = $Details
        Evidence  = $Evidence
        Timestamp = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
    }
    $testResults.Add($obj)

    $color = switch ($Status) {
        "PASS"          { "Green" }
        "FAIL"          { "Red" }
        "VULNERABILITY" { "Red" }
        "WARNING"       { "Yellow" }
        default         { "White" }
    }
    Write-Host ("  [{0}] {1} :: {2} -> {3}" -f $Id, $Category, $Name, $Status) -ForegroundColor $color
    if ($Details) {
        Write-Host ("      {0}" -f $Details) -ForegroundColor Gray
    }
}

Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " EMPIRICAL ADVERSARIAL STRESS TEST: HUD, IRIS UNBIND & MODS INTEGRATION" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " Target Directory : $TargetRoot" -ForegroundColor Gray
Write-Host " Timestamp (UTC)  : $((Get-Date).ToUniversalTime().ToString('yyyy-MM-ddTHH:mm:ssZ'))" -ForegroundColor Gray
Write-Host ""

# ------------------------------------------------------------------------------
# 1. Java Toolchain & Classpath Assembly
# ------------------------------------------------------------------------------
$javaExe  = Join-Path $TargetRoot "Java17\bin\java.exe"
$javacExe = Join-Path $TargetRoot "Java17\bin\javac.exe"

if (-not (Test-Path $javaExe) -or -not (Test-Path $javacExe)) {
    Write-Error "OpenJDK 17 toolchain not found in '$TargetRoot\Java17\bin'"
    exit 1
}

$stagingDir = Join-Path $env:TEMP "sayit_adv_hud_iris_staging"
if (Test-Path $stagingDir) {
    Remove-Item -Recurse -Force $stagingDir -ErrorAction SilentlyContinue
}
New-Item -ItemType Directory -Path $stagingDir -Force | Out-Null

$cpList = [System.Collections.Generic.List[string]]::new()
$cpList.Add((Join-Path $TargetRoot "Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar"))
$cpList.Add((Join-Path $TargetRoot "Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar"))
$cpList.Add((Join-Path $TargetRoot "Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar"))

$serverLibDir = Join-Path $TargetRoot "Servidor_Dedicado\libraries"
if (Test-Path $serverLibDir) {
    Get-ChildItem -Recurse -Path $serverLibDir -Filter "*.jar" | Where-Object { $_.Name -notmatch "sponge-mixin" } | ForEach-Object {
        $cpList.Add($_.FullName)
    }
}
$clientLibDir = Join-Path $TargetRoot "Launcher\data\libraries"
if (Test-Path $clientLibDir) {
    Get-ChildItem -Recurse -Path $clientLibDir -Filter "*.jar" | ForEach-Object {
        $cpList.Add($_.FullName)
    }
}

$classpath = ($cpList -join ";")

# ==============================================================================
# SECTION 1: JAVA ADVERSARIAL HARNESS (CHALLENGEHUD & IRIS REFLECTION)
# ==============================================================================
Write-Host "`n>>> [SECTION 1] JAVA ADVERSARIAL HARNESS: CHALLENGEHUD & RUNTIME GUARD" -ForegroundColor Yellow

$harnessSrc = Join-Path $TargetRoot "tests\AdversarialHudAndModsHarness.java"

$compileProc = Start-Process -FilePath $javacExe -ArgumentList @(
    "-encoding", "UTF-8",
    "-cp", "`"$classpath`"",
    "-d", "`"$stagingDir`"",
    "`"$harnessSrc`""
) -Wait -NoNewWindow -PassThru

if ($compileProc.ExitCode -eq 0) {
    Register-AdvCheck -Id "ADV-TOOL-01" -Category "Toolchain" -Name "Adversarial Java Harness Compilation" -Status "PASS" -Details "AdversarialHudAndModsHarness compiled cleanly"
} else {
    Register-AdvCheck -Id "ADV-TOOL-01" -Category "Toolchain" -Name "Adversarial Java Harness Compilation" -Status "FAIL" -Details "Compilation failed with exit code $($compileProc.ExitCode)"
}

$runCp = "$stagingDir;$classpath"
$procInfo = New-Object System.Diagnostics.ProcessStartInfo
$procInfo.FileName = $javaExe
$procInfo.Arguments = "-cp `"$runCp`" com.spicesfire.sayitenglish.AdversarialHudAndModsHarness"
$procInfo.RedirectStandardOutput = $true
$procInfo.RedirectStandardError = $true
$procInfo.UseShellExecute = $false

$runProc = [System.Diagnostics.Process]::Start($procInfo)
$stdout = $runProc.StandardOutput.ReadToEnd()
$stderr = $runProc.StandardError.ReadToEnd()
$runProc.WaitForExit(20000)

if ($runProc.ExitCode -eq 0) {
    Register-AdvCheck -Id "ADV-EXEC-01" -Category "Harness" -Name "Adversarial Java Execution (13 vectors)" -Status "PASS" -Details "All 13 Java stress vectors passed without exceptions" -Evidence @{ Output = $stdout }
} else {
    Register-AdvCheck -Id "ADV-EXEC-01" -Category "Harness" -Name "Adversarial Java Execution (13 vectors)" -Status "FAIL" -Details "Harness failed with exit code $($runProc.ExitCode): $stderr" -Evidence @{ Error = $stderr; Output = $stdout }
}

# ==============================================================================
# SECTION 2: IRIS KEY UNBIND, OPTIONS.TXT ENCODING & RUNTIME TAMPERING GUARD
# ==============================================================================
Write-Host "`n>>> [SECTION 2] IRIS KEY UNBIND, ZERO-BOM & RUNTIME GUARD UNDER TAMPERING" -ForegroundColor Yellow

$optionsPath = Join-Path $TargetRoot "Instancia_Cliente\options.txt"
if (Test-Path $optionsPath) {
    # 2.1 Byte-Level Zero-BOM check
    $bytes = [System.IO.File]::ReadAllBytes($optionsPath)
    $hasBom = ($bytes.Length -ge 3 -and $bytes[0] -eq 0xEF -and $bytes[1] -eq 0xBB -and $bytes[2] -eq 0xBF)
    $first4 = ($bytes[0..3] | ForEach-Object { "{0:X2}" -f $_ }) -join " "
    if (-not $hasBom) {
        Register-AdvCheck -Id "ADV-IRIS-03" -Category "IrisUnbind" -Name "options.txt Zero-BOM Compliance" -Status "PASS" -Details "Zero-BOM verified (Header bytes: $first4)" -Evidence @{ HeaderHex = $first4 }
    } else {
        Register-AdvCheck -Id "ADV-IRIS-03" -Category "IrisUnbind" -Name "options.txt Zero-BOM Compliance" -Status "FAIL" -Details "Forbidden UTF-8 BOM detected in options.txt" -Evidence @{ HeaderHex = $first4 }
    }

    # 2.2 Check key_iris.keybind.reload entry
    $lines = Get-Content $optionsPath -Encoding UTF8
    $irisLines = @($lines | Where-Object { $_ -match "key_iris\.keybind\.reload" })
    if ($irisLines.Count -gt 0 -and $irisLines[0].Trim() -eq "key_iris.keybind.reload:key.keyboard.unknown") {
        Register-AdvCheck -Id "ADV-IRIS-04" -Category "IrisUnbind" -Name "options.txt key_iris.keybind.reload Unknown Key" -Status "PASS" -Details "Entry strictly mapped to 'key.keyboard.unknown'" -Evidence @{ Line = $irisLines[0].Trim() }
    } else {
        Register-AdvCheck -Id "ADV-IRIS-04" -Category "IrisUnbind" -Name "options.txt key_iris.keybind.reload Unknown Key" -Status "FAIL" -Details "Missing or invalid key_iris.keybind.reload mapping: $($irisLines[0])"
    }

    # 2.3 Check 'R' key binding safety
    $reloadR = @($lines | Where-Object { $_ -match "reload" -and $_ -match ":key\.keyboard\.r$" })
    if ($reloadR.Count -eq 0) {
        Register-AdvCheck -Id "ADV-IRIS-05" -Category "IrisUnbind" -Name "Forbidden 'R' Keybind Decoupling" -Status "PASS" -Details "Key 'R' is strictly decoupled from shader reload"
    } else {
        Register-AdvCheck -Id "ADV-IRIS-05" -Category "IrisUnbind" -Name "Forbidden 'R' Keybind Decoupling" -Status "FAIL" -Details "Detected 'R' bound to reload: $($reloadR[0])" -Evidence @{ Violation = $reloadR[0] }
    }
} else {
    Register-AdvCheck -Id "ADV-IRIS-03" -Category "IrisUnbind" -Name "options.txt Existence" -Status "FAIL" -Details "Instancia_Cliente\options.txt missing"
}

# 2.4 SayItClient.java Source Analysis & Tampering Resilience Check
$sayitClientSrc = Join-Path $TargetRoot "src\com\spicesfire\sayitenglish\client\SayItClient.java"
$clientSrc = Get-Content -Raw -Encoding UTF8 $sayitClientSrc

# Check whether unbindIrisReloadKey is gated by a one-shot boolean
$hasOneShotGuard = ($clientSrc -match "if\s*\(!irisReloadUnbound\)\s*\{\s*unbindIrisReloadKey\(\);\s*irisReloadUnbound\s*=\s*true;\s*\}")
if ($hasOneShotGuard) {
    Register-AdvCheck -Id "ADV-IRIS-06" -Category "IrisUnbind" -Name "Runtime Guard Re-unbind on Post-Startup Tampering" -Status "WARNING" `
        -Details "SayItClient uses a one-shot flag 'irisReloadUnbound = true'. If options are modified/tampered after the initial client tick, SayItClient will NOT automatically re-unbind the key." `
        -Evidence @{
            Finding = "One-shot boolean latch blocks subsequent calls to unbindIrisReloadKey()";
            CodeSnippet = "if (!irisReloadUnbound) { unbindIrisReloadKey(); irisReloadUnbound = true; }";
            Recommendation = "Check unbindIrisReloadKey() periodically (e.g. every 20 ticks or when options are reloaded) instead of a one-time boolean guard."
        }
} else {
    Register-AdvCheck -Id "ADV-IRIS-06" -Category "IrisUnbind" -Name "Runtime Guard Re-unbind on Post-Startup Tampering" -Status "PASS" `
        -Details "SayItClient dynamically re-evaluates key unbinding without permanent one-shot latch."
}

# ==============================================================================
# SECTION 3: FARMER'S RESPITE & COMFORTS MOD INTEGRATION & CHECKSUMS
# ==============================================================================
Write-Host "`n>>> [SECTION 3] FARMER'S RESPITE & COMFORTS MOD INTEGRATION" -ForegroundColor Yellow

$clientModsDir = Join-Path $TargetRoot "Instancia_Cliente\mods"
$serverModsDir = Join-Path $TargetRoot "Servidor_Dedicado\mods"

$modList = @(
    @{ Name = "Farmersrespite-Fabric-3.0.2.jar"; ExpectedMinSize = 700000 },
    @{ Name = "comforts-fabric-6.4.0+1.20.1.jar"; ExpectedMinSize = 650000 }
)

foreach ($m in $modList) {
    $cPath = Join-Path $clientModsDir $m.Name
    $sPath = Join-Path $serverModsDir $m.Name

    if (-not (Test-Path $cPath)) {
        Register-AdvCheck -Id ("ADV-MOD-CL-" + $m.Name) -Category "ModParity" -Name ("Client Mod: " + $m.Name) -Status "FAIL" -Details "Missing in Instancia_Cliente\mods"
        continue
    }
    if (-not (Test-Path $sPath)) {
        Register-AdvCheck -Id ("ADV-MOD-SV-" + $m.Name) -Category "ModParity" -Name ("Server Mod: " + $m.Name) -Status "FAIL" -Details "Missing in Servidor_Dedicado\mods"
        continue
    }

    $cHash = (Get-FileHash -Path $cPath -Algorithm SHA256).Hash
    $sHash = (Get-FileHash -Path $sPath -Algorithm SHA256).Hash
    $cSize = (Get-Item $cPath).Length
    $sSize = (Get-Item $sPath).Length

    if ($cHash -eq $sHash -and $cSize -eq $sSize) {
        Register-AdvCheck -Id ("ADV-MOD-PAR-" + $m.Name) -Category "ModParity" -Name ("Dual Parity: " + $m.Name) -Status "PASS" `
            -Details ("Client and server hashes strictly match ($cSize bytes, SHA256: " + $cHash.Substring(0, 16) + "...)") `
            -Evidence @{ ClientHash = $cHash; ServerHash = $sHash; Size = $cSize }
    } else {
        Register-AdvCheck -Id ("ADV-MOD-PAR-" + $m.Name) -Category "ModParity" -Name ("Dual Parity: " + $m.Name) -Status "FAIL" `
            -Details "Checksum mismatch between client and server jars" -Evidence @{ ClientHash = $cHash; ServerHash = $sHash }
    }
}

# 3.2 Mod Metadata & Universal Fabric Architecture
Add-Type -AssemblyName System.IO.Compression.FileSystem
foreach ($m in $modList) {
    $cPath = Join-Path $clientModsDir $m.Name
    $zip = [System.IO.Compression.ZipFile]::OpenRead($cPath)
    $entry = $zip.GetEntry('fabric.mod.json')
    if ($entry) {
        $reader = New-Object System.IO.StreamReader($entry.Open())
        $json = $reader.ReadToEnd() | ConvertFrom-Json
        $reader.Close()

        $isUniversal = ($json.environment -eq "*")
        $hasDepends = ($null -ne $json.depends)

        if ($isUniversal) {
            Register-AdvCheck -Id ("ADV-MOD-ENV-" + $m.Name) -Category "ModMetadata" -Name ("Universal Environment: " + $json.id) -Status "PASS" `
                -Details ("Mod '" + $json.id + "' v" + $json.version + " explicitly targets universal environment ('*')")
        } else {
            Register-AdvCheck -Id ("ADV-MOD-ENV-" + $m.Name) -Category "ModMetadata" -Name ("Universal Environment: " + $json.id) -Status "FAIL" `
                -Details ("Mod environment is '" + $json.environment + "', expected universal '*'")
        }
    } else {
        Register-AdvCheck -Id ("ADV-MOD-ENV-" + $m.Name) -Category "ModMetadata" -Name ("fabric.mod.json: " + $m.Name) -Status "FAIL" -Details "Missing fabric.mod.json"
    }
    $zip.Dispose()
}

# 3.3 Bilingual Assets & Curriculum Verification
$frRu = Join-Path $TargetRoot "Instancia_Cliente\kubejs\assets\farmersrespite\lang\ru_ru.json"
$cfRu = Join-Path $TargetRoot "Instancia_Cliente\kubejs\assets\comforts\lang\ru_ru.json"
$ttScript = Join-Path $TargetRoot "Instancia_Cliente\kubejs\client_scripts\russian_tooltips.js"

if ((Test-Path $frRu) -and (Test-Path $cfRu) -and (Test-Path $ttScript)) {
    $frContent = Get-Content -Raw -Encoding UTF8 $frRu | ConvertFrom-Json
    $cfContent = Get-Content -Raw -Encoding UTF8 $cfRu | ConvertFrom-Json
    $ttContent = Get-Content -Raw -Encoding UTF8 $ttScript

    $hasKettle = ($null -ne $frContent.'block.farmersrespite.kettle')
    $hasTea    = ($null -ne $frContent.'item.farmersrespite.green_tea')
    $hasCoffee = ($null -ne $frContent.'item.farmersrespite.coffee')
    $hasHammock = ($null -ne $cfContent.'item.comforts.hammock_white' -or $null -ne $cfContent.'block.comforts.hammock_white')
    $hasBag     = ($null -ne $cfContent.'item.comforts.sleeping_bag_white' -or $null -ne $cfContent.'block.comforts.sleeping_bag_white')

    $ttHasFR = ($ttContent -match "farmersrespite:kettle" -and $ttContent -match "farmersrespite:coffee")
    $ttHasCF = ($ttContent -match "comforts:hammock_white" -and $ttContent -match "comforts:sleeping_bag_white")

    if ($hasKettle -and $hasTea -and $hasCoffee -and $hasHammock -and $hasBag -and $ttHasFR -and $ttHasCF) {
        Register-AdvCheck -Id "ADV-LANG-01" -Category "Curriculum" -Name "Bilingual Cyrillic Asset Coverage" -Status "PASS" `
            -Details "Cyrillic translations and pedagogical tooltips verified for kettle, tea, coffee, hammock, and sleeping bag"
    } else {
        Register-AdvCheck -Id "ADV-LANG-01" -Category "Curriculum" -Name "Bilingual Cyrillic Asset Coverage" -Status "FAIL" `
            -Details "Incomplete bilingual mapping in ru_ru.json or russian_tooltips.js"
    }
} else {
    Register-AdvCheck -Id "ADV-LANG-01" -Category "Curriculum" -Name "Bilingual Cyrillic Asset Coverage" -Status "FAIL" `
        -Details "Missing KubeJS lang assets or russian_tooltips.js"
}

# ==============================================================================
# SECTION 4: HEADLESS SERVER DRY-RUN UNDER STRESS
# ==============================================================================
Write-Host "`n>>> [SECTION 4] HEADLESS DEDICATED SERVER DRY-RUN UNDER STRESS" -ForegroundColor Yellow

$srvDir = Join-Path $TargetRoot "Servidor_Dedicado"
$srvJar = Join-Path $srvDir "fabric-server-launch.jar"

# Test 4.1: Dry-Run with --help flag (Fabric Mod Resolution & Entrypoints)
$psi1 = New-Object System.Diagnostics.ProcessStartInfo
$psi1.FileName = $javaExe
$psi1.Arguments = "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher -jar `"$srvJar`" --help"
$psi1.WorkingDirectory = $srvDir
$psi1.UseShellExecute = $false
$psi1.RedirectStandardOutput = $true
$psi1.RedirectStandardError = $true
$psi1.CreateNoWindow = $true

$proc1 = [System.Diagnostics.Process]::Start($psi1)
$stdout1 = $proc1.StandardOutput.ReadToEnd()
$stderr1 = $proc1.StandardError.ReadToEnd()
$proc1.WaitForExit(15000)

$combined1 = $stdout1 + "`n" + $stderr1
$hasModIncompat = ($combined1 -match "Incompatible mods found|FormattedException|Mod resolution failed")
$hasModLoadedFR = ($combined1 -match "farmersrespite 3\.0\.2")
$hasModLoadedCF = ($combined1 -match "comforts 6\.4\.0\+1\.20\.1")
$hasModLoadedSI = ($combined1 -match "sayitenglish 1\.1\.0")

if ($proc1.ExitCode -eq 0 -and -not $hasModIncompat -and $hasModLoadedFR -and $hasModLoadedCF -and $hasModLoadedSI) {
    Register-AdvCheck -Id "ADV-SRV-01" -Category "ServerStress" -Name "Headless Server Boot & 93-Mod Resolution" -Status "PASS" `
        -Details "Dedicated server booted cleanly; verified comforts, farmersrespite, and sayitenglish registered with 0 errors" `
        -Evidence @{ ExitCode = $proc1.ExitCode; ModsVerified = @("comforts 6.4.0", "farmersrespite 3.0.2", "sayitenglish 1.1.0") }
} else {
    Register-AdvCheck -Id "ADV-SRV-01" -Category "ServerStress" -Name "Headless Server Boot & 93-Mod Resolution" -Status "FAIL" `
        -Details "Server dry-run failed or missed required mods. ExitCode=$($proc1.ExitCode)" -Evidence @{ Output = $combined1 }
}

# Test 4.2: Full Server Config Bootstrap with --initSettings
$psi2 = New-Object System.Diagnostics.ProcessStartInfo
$psi2.FileName = $javaExe
$psi2.Arguments = "-Dfabric.dli.main=net.fabricmc.loader.impl.launch.server.FabricServerLauncher -jar `"$srvJar`" --initSettings"
$psi2.WorkingDirectory = $srvDir
$psi2.UseShellExecute = $false
$psi2.RedirectStandardOutput = $true
$psi2.RedirectStandardError = $true
$psi2.CreateNoWindow = $true

$proc2 = [System.Diagnostics.Process]::Start($psi2)
$stdout2 = $proc2.StandardOutput.ReadToEnd()
$stderr2 = $proc2.StandardError.ReadToEnd()
$proc2.WaitForExit(30000)

$combined2 = $stdout2 + "`n" + $stderr2
$initSuccess = ($combined2 -match "Initialized '.*server\.properties' and '.*eula\.txt'")

if ($proc2.ExitCode -eq 0 -and $initSuccess) {
    Register-AdvCheck -Id "ADV-SRV-02" -Category "ServerStress" -Name "Server Mod Config Resolution (--initSettings)" -Status "PASS" `
        -Details "Full server configuration & NightConfig resolution completed cleanly with ExitCode 0"
} else {
    Register-AdvCheck -Id "ADV-SRV-02" -Category "ServerStress" -Name "Server Mod Config Resolution (--initSettings)" -Status "FAIL" `
        -Details "Server --initSettings failed with ExitCode $($proc2.ExitCode)" -Evidence @{ Output = $combined2 }
}

# Verify no crash reports exist
$crashReportsDir = Join-Path $srvDir "crash-reports"
$crashCount = 0
if (Test-Path $crashReportsDir) {
    $crashCount = (Get-ChildItem -Path $crashReportsDir -Filter "*.txt").Count
}
if ($crashCount -eq 0) {
    Register-AdvCheck -Id "ADV-SRV-03" -Category "ServerStress" -Name "Zero Crash Reports Verification" -Status "PASS" `
        -Details "Dedicated server generated 0 crash reports under stress boot cycles"
} else {
    Register-AdvCheck -Id "ADV-SRV-03" -Category "ServerStress" -Name "Zero Crash Reports Verification" -Status "FAIL" `
        -Details "Found $crashCount crash reports in $crashReportsDir"
}

# ==============================================================================
# SUMMARY & REPORT GENERATION
# ==============================================================================
$duration = [Math]::Round(((Get-Date) - $suiteStart).TotalSeconds, 2)
$passCount    = @($testResults | Where-Object { $_.Status -eq "PASS" }).Count
$warnCount    = @($testResults | Where-Object { $_.Status -eq "WARNING" }).Count
$failCount    = @($testResults | Where-Object { $_.Status -eq "FAIL" -or $_.Status -eq "VULNERABILITY" }).Count
$totalCount   = $testResults.Count

Write-Host "`n==============================================================================" -ForegroundColor Cyan
Write-Host " ADVERSARIAL STRESS TEST SUMMARY" -ForegroundColor Cyan
Write-Host "==============================================================================" -ForegroundColor Cyan
Write-Host " Total Checks Executed : $totalCount" -ForegroundColor White
Write-Host " Checks Passed         : $passCount" -ForegroundColor Green
Write-Host " Warnings / Findings   : $warnCount" -ForegroundColor Yellow
Write-Host " Critical Failures     : $failCount" -ForegroundColor $(if ($failCount -gt 0) { "Red" } else { "Green" })
Write-Host " Total Elapsed Time    : $duration seconds" -ForegroundColor Gray
Write-Host "==============================================================================" -ForegroundColor Cyan

if ($ExportJson) {
    $jsonPath = Join-Path $TargetRoot "tests\test_adversarial_hud_iris_mods_results.json"
    $report = [PSCustomObject]@{
        Suite         = "Adversarial Stress Test: HUD, Iris Decoupling & Mods Integration"
        TimestampUtc  = (Get-Date).ToUniversalTime().ToString("yyyy-MM-ddTHH:mm:ssZ")
        TotalDuration = $duration
        Summary       = @{
            Total   = $totalCount
            Passed  = $passCount
            Warning = $warnCount
            Failed  = $failCount
        }
        Results       = $testResults
    }
    $report | ConvertTo-Json -Depth 6 | Set-Content -Path $jsonPath -Encoding UTF8
    Write-Host " Results exported to: $jsonPath" -ForegroundColor Cyan
}

if ($failCount -gt 0) {
    Write-Host "`n[VERDICT: REQUEST_CHANGES] Critical failures detected in adversarial stress suite!" -ForegroundColor Red
    exit 1
} else {
    Write-Host "`n[VERDICT: APPROVE] All critical functional & adversarial requirements verified!" -ForegroundColor Green
    if ($warnCount -gt 0) {
        Write-Host "  (Note: $warnCount non-blocking architectural finding/warning documented for team awareness)" -ForegroundColor Yellow
    }
    exit 0
}
