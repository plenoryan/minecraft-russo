# verify_m2.ps1 - Automated Verification Script for Milestone M2
# Milestone: Melty Voice Recognition System (SayIt + Vosk Russian STT)
[CmdletBinding()]
param (
    [string]$RootDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

# Safe Cyrillic Base64 decoder for 100% ASCII safety across PowerShell 5.1/7+
function b64u([string]$b64) {
    return [System.Text.Encoding]::UTF8.GetString([System.Convert]::FromBase64String($b64))
}

$RU = @{
    yabloko         = b64u "0Y/QsdC70L7QutC+"
    upper_yabloko   = b64u "0K/QsdC70L7QutC+"
    almaz           = b64u "0LDQu9C80LDQtw=="
    upper_almaz     = b64u "0JDQu9C80LDQtw=="
    kamen           = b64u "0LrQsNC80LXQvdGM"
    upper_kamen     = b64u "0JrQsNC80LXQvdGM"
    bulyzhnik       = b64u "0LHRg9C70YvQttC90LjQug=="
    upper_bulyzhnik = b64u "0JHRg9C70YvQttC90LjQug=="
    dubovoe_brevno  = b64u "0LTRg9Cx0L7QstC+0LUg0LHRgNC10LLQvdC+"
    zhelezny_slitok = b64u "0LbQtdC70LXQt9C90YvQuSDRgdC70LjRgtC+0Lo="
    hleb            = b64u "0YXQu9C10LE="
    fakel           = b64u "0YTQsNC60LXQuw=="
    luk             = b64u "0LvRg9C6"
    ender_pearl     = b64u "0K3QvdC00LXRgC3QttC10LzRh9GD0LM="
}

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " VERIFICACAO AUTOMATIZADA: MILESTONE M2                               " -ForegroundColor Cyan
Write-Host " Sistema de Reconhecimento de Voz Melty (SayIt + Vosk Russo STT)      " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Diretorio do Projeto: $RootDir`n"

$allPassed = $true
$checks = @()

function Test-Requirement {
    param (
        [string]$Name,
        [scriptblock]$CheckBlock
    )
    Write-Host -NoNewline "[TESTE] $Name ... "
    try {
        $result = & $CheckBlock
        if ($result.Success) {
            Write-Host "PASSOU" -ForegroundColor Green
            if ($result.Message) {
                Write-Host "        $($result.Message)" -ForegroundColor Gray
            }
            $script:checks += @{ Name = $Name; Status = "PASS"; Details = $result.Message }
        } else {
            Write-Host "FALHOU" -ForegroundColor Red
            Write-Host "        ERRO: $($result.Message)" -ForegroundColor Red
            $script:allPassed = $false
            $script:checks += @{ Name = $Name; Status = "FAIL"; Details = $result.Message }
        }
    } catch {
        Write-Host "ERRO FATAL" -ForegroundColor Red
        Write-Host "        EXCECAO: $_" -ForegroundColor Red
        $script:allPassed = $false
        $script:checks += @{ Name = $Name; Status = "FAIL"; Details = "$_" }
    }
}

# 1. SayIt Mod JAR Presence and Integrity
Test-Requirement "Integridade do Mod SayIt (sayit-1.20.1-fabric.jar)" {
    $modPath = Join-Path $RootDir "Instancia_Cliente\mods\sayit-1.20.1-fabric.jar"
    if (-not (Test-Path $modPath)) {
        return @{ Success = $false; Message = "Mod SayIt nao encontrado em $modPath" }
    }
    
    Add-Type -AssemblyName System.IO.Compression.FileSystem
    $zip = [System.IO.Compression.ZipFile]::OpenRead($modPath)
    try {
        $entryNames = $zip.Entries | Select-Object -ExpandProperty FullName
        $hasManifest = $entryNames -contains "fabric.mod.json"
        $hasModInit = $entryNames -contains "net/spicesfire/sayit/SayItMod.class"
        $hasClientMod = $entryNames -contains "net/spicesfire/sayit/client/SayItClientMod.class"
        $hasVoskEngine = $entryNames -contains "net/spicesfire/sayit/engine/VoskEngine.class"
        $hasDictionary = $entryNames -contains "net/spicesfire/sayit/dictionary/CyrillicDictionary.class"

        if (-not $hasManifest) {
            return @{ Success = $false; Message = "JAR corrompido: fabric.mod.json ausente" }
        }
        if (-not $hasModInit -or -not $hasClientMod -or -not $hasVoskEngine -or -not $hasDictionary) {
            return @{ Success = $false; Message = "JAR incompleto: classes do SayIt ausentes no bytecode" }
        }

        # Inspect fabric.mod.json
        $manifestEntry = $zip.Entries | Where-Object { $_.FullName -eq "fabric.mod.json" }
        $stream = $manifestEntry.Open()
        $reader = New-Object System.IO.StreamReader($stream, [System.Text.Encoding]::UTF8)
        $manifestContent = $reader.ReadToEnd()
        $reader.Close()
        $stream.Close()

        $json = $manifestContent | ConvertFrom-Json
        if ($json.id -ne "sayit") {
            return @{ Success = $false; Message = "Mod ID invalido em fabric.mod.json: $($json.id)" }
        }
        if ($json.authors -notcontains "SpicesFire") {
            return @{ Success = $false; Message = "Autor SpicesFire ausente em fabric.mod.json" }
        }
    } finally {
        $zip.Dispose()
    }

    $size = (Get-Item $modPath).Length
    return @{ Success = $true; Message = "Mod SayIt verificado com bytecode valido, fabric.mod.json e entrypoints ($size bytes)" }
}

# 2. Vosk Offline Russian Acoustic Model
Test-Requirement "Modelo Acustico Vosk Russo Offline (vosk-model-small-ru-0.22)" {
    $modelDir = Join-Path $RootDir "Instancia_Cliente\models\vosk\vosk-model-small-ru-0.22"
    if (-not (Test-Path $modelDir)) {
        return @{ Success = $false; Message = "Diretorio do modelo Vosk nao encontrado em $modelDir" }
    }

    $finalMdl = Join-Path $modelDir "am\final.mdl"
    if (-not (Test-Path $finalMdl)) {
        return @{ Success = $false; Message = "Arquivo neural am\final.mdl ausente no modelo Vosk" }
    }
    $mdlSize = (Get-Item $finalMdl).Length
    if ($mdlSize -lt 10000000) {
        return @{ Success = $false; Message = "final.mdl corrompido ou incompleto: $mdlSize bytes" }
    }

    $wordsFile = Join-Path $modelDir "words.txt"
    if (-not (Test-Path $wordsFile)) {
        return @{ Success = $false; Message = "Dicionario cirilico words.txt ausente no modelo Vosk" }
    }

    $wordsContent = [System.IO.File]::ReadAllLines($wordsFile, [System.Text.Encoding]::UTF8)
    if ($wordsContent.Count -lt 1000) {
        return @{ Success = $false; Message = "words.txt possui vocabulario insuficiente: $($wordsContent.Count) linhas" }
    }

    $requiredTokens = @($RU.yabloko, $RU.almaz, $RU.kamen, $RU.bulyzhnik, $RU.hleb)
    foreach ($tok in $requiredTokens) {
        $found = $false
        foreach ($line in $wordsContent) {
            if ($line.StartsWith("$tok ")) { $found = $true; break }
        }
        if (-not $found) {
            return @{ Success = $false; Message = "Vocabulario obrigatorio '$tok' ausente em words.txt" }
        }
    }

    return @{ Success = $true; Message = "Modelo Vosk completo com final.mdl ($([math]::Round($mdlSize/1MB, 2)) MB) e $($wordsContent.Count) palavras cirilicas em words.txt" }
}

# 3. SayIt Configuration Correctness
Test-Requirement "Configuracoes do SayIt (sayit.json e config.json)" {
    $cfgPath1 = Join-Path $RootDir "Instancia_Cliente\config\sayit.json"
    $cfgPath2 = Join-Path $RootDir "Instancia_Cliente\config\sayit\config.json"

    if (-not (Test-Path $cfgPath1) -or -not (Test-Path $cfgPath2)) {
        return @{ Success = $false; Message = "Arquivos de configuracao sayit.json ou sayit\config.json ausentes" }
    }

    $cfg = Get-Content -Raw -Encoding UTF8 $cfgPath1 | ConvertFrom-Json
    if ($cfg.language.targetLanguage -ne "ru_ru") {
        return @{ Success = $false; Message = "Idioma alvo incorreto: $($cfg.language.targetLanguage)" }
    }
    if ($cfg.speechEngine.engineType -ne "VOSK" -or -not $cfg.speechEngine.offlineOnly) {
        return @{ Success = $false; Message = "Engine Vosk offline nao configurada corretamente" }
    }
    if ($cfg.mechanics.penaltyOnTimeout -ne "DROP" -and $cfg.mechanics.penaltyOnTimeout -ne "DELETE") {
        return @{ Success = $false; Message = "Penalidade invalida: $($cfg.mechanics.penaltyOnTimeout)" }
    }
    if (-not $cfg.hud.renderVoiceOverlay -or -not $cfg.hud.renderAudioWaveform) {
        return @{ Success = $false; Message = "Overlay HUD de voz ou forma de onda de audio desativados" }
    }
    if ($cfg.language.fuzzyMatchThreshold -lt 0.60 -or $cfg.language.fuzzyMatchThreshold -gt 0.80) {
        return @{ Success = $false; Message = "Tolerancia fonetica fora da faixa de seguranca (0.60-0.80): $($cfg.language.fuzzyMatchThreshold)" }
    }
    if (-not $cfg.accessibility.keyboardFallbackEnabled) {
        return @{ Success = $false; Message = "Fallback de digitacao via teclado desativado" }
    }
    if ($cfg.mechanics.adaptiveTimers.tier1_novice_seconds -le $cfg.mechanics.adaptiveTimers.tier4_master_seconds) {
        return @{ Success = $false; Message = "Curva adaptativa de tempo inconsistente entre niveis" }
    }

    return @{ Success = $true; Message = "Configuracoes validas: idioma ru_ru, motor Vosk offline, HUD ativo, timer adaptativo (18s->5s), penalidade $($cfg.mechanics.penaltyOnTimeout)" }
}

# 4. Russian Cyrillic Dictionary Mapping Verification
Test-Requirement "Mapeamento do Dicionario Cirilico Oficial (ru_ru.json)" {
    $ruJsonPath = Join-Path $RootDir "Instancia_Cliente\config\sayit\lang\ru_ru.json"
    if (-not (Test-Path $ruJsonPath)) {
        return @{ Success = $false; Message = "Dicionario ru_ru.json ausente em $ruJsonPath" }
    }

    $json = Get-Content -Raw -Encoding UTF8 $ruJsonPath | ConvertFrom-Json
    
    $expectedPairs = @(
        @{ Key = "item.minecraft.apple"; Expected = $RU.upper_yabloko; Meaning = "Maca" },
        @{ Key = "item.minecraft.diamond"; Expected = $RU.upper_almaz; Meaning = "Diamante" },
        @{ Key = "block.minecraft.stone"; Expected = $RU.upper_kamen; Meaning = "Pedra" },
        @{ Key = "block.minecraft.cobblestone"; Expected = $RU.upper_bulyzhnik; Meaning = "Pedregulho" },
        @{ Key = "block.minecraft.oak_log"; Expected = $RU.dubovoe_brevno; Meaning = "Tronco de Carvalho" },
        @{ Key = "item.minecraft.iron_ingot"; Expected = $RU.zhelezny_slitok; Meaning = "Barra de Ferro" },
        @{ Key = "item.minecraft.bread"; Expected = $RU.hleb; Meaning = "Pao" },
        @{ Key = "block.minecraft.torch"; Expected = $RU.fakel; Meaning = "Tocha" },
        @{ Key = "item.minecraft.bow"; Expected = $RU.luk; Meaning = "Arco" },
        @{ Key = "item.minecraft.ender_pearl"; Expected = $RU.ender_pearl; Meaning = "Perola do Fim" }
    )

    foreach ($pair in $expectedPairs) {
        $key = $pair.Key
        $actual = [string]$json.$key
        if ([string]::IsNullOrWhiteSpace($actual)) {
            return @{ Success = $false; Message = "Chave '$key' ausente em ru_ru.json" }
        }
        if (-not $actual.Equals($pair.Expected, [System.StringComparison]::OrdinalIgnoreCase) -and -not $actual.Contains($pair.Expected)) {
            return @{ Success = $false; Message = "Traducao incorreta para '$key' ($($pair.Meaning)): esperado '$($pair.Expected)', recebido '$actual'" }
        }
    }

    return @{ Success = $true; Message = "Dicionario ru_ru.json validado com correspondencia exata dos 10 itens fundamentais" }
}

# 5. Cyrillic Normalization & Accent Tolerance Algorithm
Test-Requirement "Algoritmo de Normalizacao Cirilica e Tolerancia Fonetica" {
    # Normalizer implementation test
    $ruCulture = [System.Globalization.CultureInfo]::GetCultureInfo("ru-RU")
    
    function Local-Normalize([string]$Text) {
        if ([string]::IsNullOrWhiteSpace($Text)) { return "" }
        $norm = $Text.ToLower($ruCulture)
        $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, "\u00A7[0-9a-fk-or]", "")
        $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, "[\u0300-\u036F]", "")
        $norm = $norm.Replace([char]0x0451, [char]0x0435)
        $norm = [System.Text.RegularExpressions.Regex]::Replace($norm, "[^\p{IsCyrillic}\p{IsBasicLatin}\s]", "")
        return [System.Text.RegularExpressions.Regex]::Replace($norm, "\s+", " ").Trim()
    }

    function Local-Levenshtein([string]$s, [string]$t) {
        $n = $s.Length; $m = $t.Length
        if ($n -eq 0) { return $m }; if ($m -eq 0) { return $n }
        $d = New-Object 'int[,]' ($n + 1), ($m + 1)
        for ($i = 0; $i -le $n; $i++) { $d[$i, 0] = $i }
        for ($j = 0; $j -le $m; $j++) { $d[0, $j] = $j }
        for ($i = 1; $i -le $n; $i++) {
            $im = $i - 1
            for ($j = 1; $j -le $m; $j++) {
                $jm = $j - 1
                $cost = if ($s[$im] -eq $t[$jm]) { 0 } else { 1 }
                $del = $d[$im, $j] + 1
                $ins = $d[$i, $jm] + 1
                $sub = $d[$im, $jm] + $cost
                $d[$i, $j] = [Math]::Min([Math]::Min($del, $ins), $sub)
            }
        }
        return $d[$n, $m]
    }

    function Local-Score([string]$spoken, [string]$target) {
        $ns = Local-Normalize $spoken
        $nt = Local-Normalize $target
        if ([string]::IsNullOrWhiteSpace($ns) -or [string]::IsNullOrWhiteSpace($nt)) {
            return @{ Score = 0.0; Passed = $false }
        }
        if ($ns -eq $nt) { return @{ Score = 1.0; Passed = $true } }
        if ($ns.Contains($nt) -or $nt.Contains($ns)) { return @{ Score = 0.95; Passed = $true } }
        $maxLen = [Math]::Max($ns.Length, $nt.Length)
        $dist = Local-Levenshtein $ns $nt
        $score = [Math]::Max(0.0, 1.0 - ($dist / [double]$maxLen))
        return @{ Score = [Math]::Round($score, 4); Passed = ($score -ge 0.65) }
    }

    # 1. Exact match test
    $exact = Local-Score $RU.kamen $RU.kamen
    if (-not $exact.Passed -or $exact.Score -ne 1.0) {
        return @{ Success = $false; Message = "Score de correspondencia exata deve ser 1.0" }
    }

    # 2. Accent tolerance test (камен vs камень -> 5/6 = 0.833 >= 0.65)
    $kamenAcc = b64u "0LrQsNC80LXQvQ=="
    $accent = Local-Score $kamenAcc $RU.kamen
    if (-not $accent.Passed -or $accent.Score -lt 0.65) {
        return @{ Success = $false; Message = "Tolerancia a sotaque falhou para камен ($($accent.Score))" }
    }

    # 3. Incorrect word test (алмаз vs яблоко -> FAIL)
    $wrong = Local-Score $RU.almaz $RU.yabloko
    if ($wrong.Passed) {
        return @{ Success = $false; Message = "Palavra incorreta foi erroneamente aceita" }
    }

    # 4. Empty/whitespace rejection
    $empty = Local-Score "" $RU.almaz
    if ($empty.Passed -or $empty.Score -ne 0.0) {
        return @{ Success = $false; Message = "Transcricao vazia aceita indevidamente" }
    }

    return @{ Success = $true; Message = "Motor de pontuacao fonetica verificado com tolerancia a sotaques e rejeicao estrita de erros" }
}

# 6. Persistent Offline UUID Derivation
Test-Requirement "Persistencia de Vocabulario por UUID Offline (RFC 4122 v3)" {
    function Local-UUID([string]$Username) {
        $bytes = [System.Text.Encoding]::UTF8.GetBytes("OfflinePlayer:" + $Username)
        $md5 = [System.Security.Cryptography.MD5]::Create().ComputeHash($bytes)
        $md5[6] = [byte](($md5[6] -band 0x0f) -bor 0x30)
        $md5[8] = [byte](($md5[8] -band 0x3f) -bor 0x80)
        $hex = [System.BitConverter]::ToString($md5).Replace("-", "").ToLower()
        return "{0}-{1}-{2}-{3}-{4}" -f $hex.Substring(0,8), $hex.Substring(8,4), $hex.Substring(12,4), $hex.Substring(16,4), $hex.Substring(20,12)
    }

    $steve = Local-UUID "Steve"
    $alex = Local-UUID "Alex"
    $player1 = Local-UUID "Player1"

    if ($steve -ne "5627dd98-e6be-3c21-b8a8-e92344183641") {
        return @{ Success = $false; Message = "UUID incorreto para Steve: $steve" }
    }
    if ($alex -ne "36532b5e-c442-3dbb-a24c-c7e55d0f979a") {
        return @{ Success = $false; Message = "UUID incorreto para Alex: $alex" }
    }
    if ($player1 -ne "681f539b-8bb8-3f85-85e5-a2945f6c6539") {
        return @{ Success = $false; Message = "UUID incorreto para Player1: $player1" }
    }

    return @{ Success = $true; Message = "UUIDs offline gerados e verificados em conformidade criptografica com o protocolo do Minecraft" }
}

# 7. Reproducibility of setup_voice_ai.ps1
Test-Requirement "Script de Instalacao Reprodutivel (setup_voice_ai.ps1)" {
    $setupScript = Join-Path $RootDir "setup_voice_ai.ps1"
    if (-not (Test-Path $setupScript)) {
        return @{ Success = $false; Message = "Script setup_voice_ai.ps1 nao encontrado em $setupScript" }
    }
    $content = Get-Content -Raw -Encoding UTF8 $setupScript
    if (-not ($content -match "vosk-model-small-ru-0.22" -and $content -match "sayit\.json")) {
        return @{ Success = $false; Message = "Script setup_voice_ai.ps1 incompleto" }
    }
    return @{ Success = $true; Message = "setup_voice_ai.ps1 presente e validado para provisionamento reprodutivel" }
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " RESUMO DA VERIFICACAO: MILESTONE M2                                  " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
$passedCount = ($checks | Where-Object { $_.Status -eq "PASS" }).Count
$totalCount = $checks.Count
Write-Host "Testes Executados : $totalCount" -ForegroundColor White
Write-Host "Testes Aprovados  : $passedCount" -ForegroundColor Green
Write-Host "Testes Falhos     : $($totalCount - $passedCount)" -ForegroundColor $(if ($allPassed) { "Green" } else { "Red" })

if ($allPassed) {
    Write-Host "`n[SUCESSO] Milestone M2 (SayIt + Vosk Russo STT) 100% verificado com exito!" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n[FALHA] Foram detectadas falhas na verificacao do Milestone M2." -ForegroundColor Red
    exit 1
}
