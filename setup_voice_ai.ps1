# setup_voice_ai.ps1 - Automated Setup for Melty Voice Recognition System (SayIt + Vosk Russian STT)
# Milestone: M2 Melty Voice AI
[CmdletBinding()]
param (
    [string]$RootDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " INSTALACAO AUTOMATIZADA: SAYIT + VOSK RUSSO STT (MILESTONE M2)       " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Diretorio do Projeto: $RootDir`n"

# 1. Ensure Target Directories
$clientDir = Join-Path $RootDir "Instancia_Cliente"
$modsDir = Join-Path $clientDir "mods"
$modelsDir = Join-Path $clientDir "models\vosk"
$voskModelDir = Join-Path $modelsDir "vosk-model-small-ru-0.22"
$configDir = Join-Path $clientDir "config"
$sayitConfigDir = Join-Path $configDir "sayit"
$langDir = Join-Path $sayitConfigDir "lang"

foreach ($dir in @($modsDir, $modelsDir, $sayitConfigDir, $langDir)) {
    if (-not (Test-Path $dir)) {
        New-Item -ItemType Directory -Path $dir -Force | Out-Null
        Write-Host "[DIR] Criado: $dir" -ForegroundColor Green
    }
}

# 2. Vosk Offline Russian Model
$wordsFile = Join-Path $voskModelDir "words.txt"
$finalMdl = Join-Path $voskModelDir "am\final.mdl"

if (-not (Test-Path $finalMdl)) {
    Write-Host "[DOWNLOAD] Baixando modelo acustico Vosk Russo (vosk-model-small-ru-0.22)..." -ForegroundColor Yellow
    $zipPath = Join-Path $modelsDir "vosk-model-small-ru-0.22.zip"
    $voskUrl = "https://alphacephei.com/vosk/models/vosk-model-small-ru-0.22.zip"
    
    $wc = New-Object System.Net.WebClient
    $wc.DownloadFile($voskUrl, $zipPath)
    Write-Host "[EXTRACT] Descompactando modelo Vosk..." -ForegroundColor Yellow
    Expand-Archive -Path $zipPath -DestinationPath $modelsDir -Force
    Remove-Item -Path $zipPath -Force
    Write-Host "[OK] Modelo Vosk extraido com sucesso em: $voskModelDir" -ForegroundColor Green
} else {
    Write-Host "[OK] Modelo Vosk ja presente em: $voskModelDir" -ForegroundColor Green
}

# 3. Ground-truth Minecraft 1.20.1 Russian Language File (ru_ru.json)
$ruJsonPath = Join-Path $langDir "ru_ru.json"
if (-not (Test-Path $ruJsonPath) -or ((Get-Item $ruJsonPath).Length -lt 100000)) {
    Write-Host "[DOWNLOAD] Baixando dicionario oficial ru_ru.json (Minecraft 1.20.1)..." -ForegroundColor Yellow
    $ruUrl = "https://raw.githubusercontent.com/InventivetalentDev/minecraft-assets/1.20.1/assets/minecraft/lang/ru_ru.json"
    $wc = New-Object System.Net.WebClient
    $wc.Encoding = [System.Text.Encoding]::UTF8
    $wc.DownloadFile($ruUrl, $ruJsonPath)
    Write-Host "[OK] ru_ru.json baixado com sucesso ($((Get-Item $ruJsonPath).Length) bytes)" -ForegroundColor Green
} else {
    Write-Host "[OK] ru_ru.json verificado ($((Get-Item $ruJsonPath).Length) bytes)" -ForegroundColor Green
}

# 4. Vosk Cyrillic Dictionary (words.txt) and HCLG.fst
if (-not (Test-Path $wordsFile)) {
    Write-Host "[BUILD] Gerando words.txt a partir de ru_ru.json..." -ForegroundColor Yellow
    $json = Get-Content -Raw -Encoding UTF8 $ruJsonPath | ConvertFrom-Json
    $uniqueWords = [System.Collections.Generic.HashSet[string]]::new([System.StringComparer]::OrdinalIgnoreCase)
    $tokens = @("<eps>", "!SIL", "<unk>", "[hes]", "[laughter]", "[music]")

    foreach ($prop in $json.PSObject.Properties) {
        $val = [string]$prop.Value
        if ($val) {
            $cleaned = [System.Text.RegularExpressions.Regex]::Replace($val, "\u00A7[0-9a-fk-or]", "")
            $cleaned = [System.Text.RegularExpressions.Regex]::Replace($cleaned, "[\u0300-\u036F]", "")
            $cleaned = [System.Text.RegularExpressions.Regex]::Replace($cleaned, "[^\p{IsCyrillic}\s-]", "")
            $words = $cleaned.ToLower([System.Globalization.CultureInfo]::GetCultureInfo("ru-RU")).Split(@(" ", "-"), [System.StringSplitOptions]::RemoveEmptyEntries)
            foreach ($w in $words) {
                if ($w.Length -gt 0) {
                    [void]$uniqueWords.Add($w)
                    if ($w.Contains("ё")) { [void]$uniqueWords.Add($w.Replace("ё", "е")) }
                }
            }
        }
    }

    $sortedWords = [System.Collections.Generic.List[string]]::new($uniqueWords)
    $sortedWords.Sort([System.StringComparer]::Ordinal)

    $lines = [System.Collections.Generic.List[string]]::new()
    $id = 0
    foreach ($t in $tokens) { $lines.Add("$t $id"); $id++ }
    foreach ($w in $sortedWords) { $lines.Add("$w $id"); $id++ }

    $utf8NoBom = New-Object System.Text.UTF8Encoding $false
    [System.IO.File]::WriteAllLines($wordsFile, $lines, $utf8NoBom)
    $graphWords = Join-Path $voskModelDir "graph\words.txt"
    [System.IO.File]::WriteAllLines($graphWords, $lines, $utf8NoBom)
    Write-Host "[OK] words.txt gerado com $($lines.Count) entradas cirilicas" -ForegroundColor Green
} else {
    Write-Host "[OK] words.txt presente em: $wordsFile" -ForegroundColor Green
}

$hclgRoot = Join-Path $voskModelDir "HCLG.fst"
$hclrGraph = Join-Path $voskModelDir "graph\HCLr.fst"
if ((Test-Path $hclrGraph) -and (-not (Test-Path $hclgRoot))) {
    Copy-Item $hclrGraph $hclgRoot -Force
    Write-Host "[OK] HCLG.fst vinculado a partir do grafo FST" -ForegroundColor Green
}

# 5. SayIt JSON Configuration
$sayitJson1 = Join-Path $configDir "sayit.json"
$sayitJson2 = Join-Path $sayitConfigDir "config.json"

$configContent = @'
{
  "version": 1,
  "language": {
    "targetLanguage": "ru_ru",
    "fallbackLanguage": "en_us",
    "dictionarySource": "config/sayit/lang/ru_ru.json",
    "normalizeYoToYe": true,
    "stripDiacriticsAndAccents": true,
    "fuzzyMatchThreshold": 0.65
  },
  "speechEngine": {
    "engineType": "VOSK",
    "offlineOnly": true,
    "modelPath": "models/vosk/vosk-model-small-ru-0.22",
    "sampleRate": 16000,
    "channels": 1,
    "bitDepth": 16,
    "chunkSize": 4000,
    "enableDynamicGrammar": true,
    "dualRecognizerScoring": true
  },
  "mechanics": {
    "triggerOnNewItemAcquisition": true,
    "challengeQueue": "FIFO",
    "maxConcurrentChallenges": 1,
    "penaltyOnTimeout": "DROP",
    "penaltyModes": {
      "delete": false,
      "drop": true,
      "practice": false
    },
    "serverLatencyGracePeriodMs": 1500,
    "adaptiveTimers": {
      "enabled": true,
      "tier1_novice_seconds": 18,
      "tier2_apprentice_seconds": 14,
      "tier3_journeyman_seconds": 9,
      "tier4_master_seconds": 5
    }
  },
  "hud": {
    "renderVoiceOverlay": true,
    "renderCountdownTimer": true,
    "renderCyrillicTargetName": true,
    "renderPhoneticTransliteration": true,
    "renderAudioWaveform": true,
    "renderMicVolumeMeter": true,
    "renderPartialTranscript": true,
    "hudPosition": "TOP_CENTER",
    "timerColorGradient": {
      "full": "#2ECC71",
      "half": "#F1C40F",
      "critical": "#E74C3C"
    }
  },
  "accessibility": {
    "keyboardFallbackEnabled": true,
    "fallbackTriggerKey": "key.keyboard.y",
    "transliterationLatinTypingAllowed": true
  },
  "pocketDictionary": {
    "enabled": true,
    "giveBookOnFirstJoin": true,
    "saveProgressByOfflineUUID": true,
    "dataPath": "config/sayit/dictionary/"
  }
}
'@

[System.IO.File]::WriteAllText($sayitJson1, $configContent, [System.Text.Encoding]::UTF8)
[System.IO.File]::WriteAllText($sayitJson2, $configContent, [System.Text.Encoding]::UTF8)
Write-Host "[OK] Configuracoes de SayIt gravadas em sayit.json e sayit\config.json" -ForegroundColor Green

# 6. SayIt Mod JAR Verification
$sayitJar = Join-Path $modsDir "sayit-1.20.1-fabric.jar"
if (-not (Test-Path $sayitJar)) {
    throw "SayIt JAR nao encontrado em $sayitJar. Execute a compilacao do modulo."
}
Write-Host "[OK] Mod SayIt verificado: $sayitJar ($((Get-Item $sayitJar).Length) bytes)" -ForegroundColor Green

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " INSTALACAO DE VOICE AI CONCLUIDA COM SUCESSO!                        " -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Cyan
