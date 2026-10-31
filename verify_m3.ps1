# verify_m3.ps1 - Comprehensive Verification for Milestone M3: Russian Pedagogical Progression & Font Stack
[CmdletBinding()]
param (
    [string]$BaseDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "   TEST SUITE: Milestone M3 Russian Progression & TrueType Fonts     " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host "Diretório Alvo: $BaseDir" -ForegroundColor Gray

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

$instanceDir = Join-Path $BaseDir "Instancia_Cliente"
$modsDir = Join-Path $instanceDir "mods"
$configDir = Join-Path $instanceDir "config"
$heraclesDir = Join-Path $configDir "heracles"
$heraclesQuestsDir = Join-Path $heraclesDir "quests"
$kubejsScriptPath = Join-Path $instanceDir "kubejs\client_scripts\russian_tooltips.js"
$packDir = Join-Path $instanceDir "resourcepacks\Russian_Bilingual_Educational_Pack"

# -------------------------------------------------------------------------
# TEST GROUP 1: Mod JAR Presence, Authenticity & Fabric IDs
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 1/4] Presença e Integridade dos Mods Educacionais (Fabric 1.20.1) ---" -ForegroundColor Yellow

$requiredMods = @(
    @{ Name = "Modern UI"; Filename = "ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar"; ExpectedId = "modernui"; MinBytes = 20000000 },
    @{ Name = "Forge Config API Port"; Filename = "ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar"; ExpectedId = "forgeconfigapiport"; MinBytes = 300000 },
    @{ Name = "Heracles"; Filename = "Heracles-fabric-1.20.1-1.1.13.jar"; ExpectedId = "heracles"; MinBytes = 1000000 },
    @{ Name = "Heracles Button"; Filename = "heraclesbutton-1.0.0.jar"; ExpectedId = "heraclesbutton"; MinBytes = 100000 },
    @{ Name = "Resourceful Lib"; Filename = "resourcefullib-fabric-1.20.1-2.1.29.jar"; ExpectedId = "resourcefullib"; MinBytes = 300000 },
    @{ Name = "Language Reload"; Filename = "language-reload-1.7.5+1.20.1.jar"; ExpectedId = "languagereload"; MinBytes = 50000 },
    @{ Name = "Jade"; Filename = "Jade-1.20-Fabric-11.13.3.jar"; ExpectedId = "jade"; MinBytes = 400000 },
    @{ Name = "KubeJS"; Filename = "kubejs-fabric-2001.6.5-build.26.jar"; ExpectedId = "kubejs"; MinBytes = 1000000 },
    @{ Name = "Rhino"; Filename = "rhino-fabric-2001.2.3-build.10.jar"; ExpectedId = "rhino"; MinBytes = 1000000 },
    @{ Name = "Architectury API"; Filename = "architectury-9.2.14-fabric.jar"; ExpectedId = "architectury"; MinBytes = 500000 }
)

Add-Type -AssemblyName System.IO.Compression.FileSystem

foreach ($mod in $requiredMods) {
    $jarPath = Join-Path $modsDir $mod.Filename
    $exists = Test-Path $jarPath
    Assert-Test $exists "Arquivo JAR presente: $($mod.Filename)"

    if ($exists) {
        $size = (Get-Item $jarPath).Length
        Assert-Test ($size -ge $mod.MinBytes) "Tamanho integro para $($mod.Name) ($size bytes >= $($mod.MinBytes))"

        $idMatched = $false
        try {
            $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
            $entry = $zip.GetEntry("fabric.mod.json")
            if ($entry) {
                $sr = New-Object System.IO.StreamReader($entry.Open())
                $json = $sr.ReadToEnd() | ConvertFrom-Json
                $sr.Close()
                if ($json.id -eq $mod.ExpectedId) {
                    $idMatched = $true
                }
            }
            $zip.Dispose()
        } catch {
            $idMatched = $false
        }
        Assert-Test $idMatched "ID verificado em fabric.mod.json: '$($mod.ExpectedId)'"
    }
}

# -------------------------------------------------------------------------
# TEST GROUP 2: Modern UI & Language Reload Configuration
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 2/4] Configuracoes de Renderizacao Vetorial (Modern UI & Language Reload) ---" -ForegroundColor Yellow

$clientTomlPath = Join-Path $configDir "modernui-client.toml"
$clientTomlExists = Test-Path $clientTomlPath
Assert-Test $clientTomlExists "modernui-client.toml existe em config\"

if ($clientTomlExists) {
    $clientToml = [System.IO.File]::ReadAllText($clientTomlPath, [System.Text.Encoding]::UTF8)
    Assert-Test ($clientToml -match 'firstFontFamily\s*=\s*"Segoe UI"') "Modern UI firstFontFamily configurado como 'Segoe UI'"
    Assert-Test ($clientToml -match 'fallbackFontFamilyList\s*=\s*\[') "Modern UI possui fallbackFontFamilyList definido"
    Assert-Test ($clientToml -match 'linearMetrics\s*=\s*true') "Modern UI linearMetrics ativado para TrueType proporcional"
    Assert-Test ($clientToml -match 'tooltip\s*=\s*true') "Modern UI tooltips customizados ativados"
}

$textTomlPath = Join-Path $configDir "modernui-text.toml"
$textTomlExists = Test-Path $textTomlPath
Assert-Test $textTomlExists "modernui-text.toml existe em config\"

if ($textTomlExists) {
    $textToml = [System.IO.File]::ReadAllText($textTomlPath, [System.Text.Encoding]::UTF8)
    Assert-Test ($textToml -match 'defaultFontBehavior\s*=\s*"ALL"') "Modern UI defaultFontBehavior='ALL' (renderizacao vetorial em todo texto)"
    Assert-Test ($textToml -match 'antiAliasing\s*=\s*true') "Modern UI subpixel antiAliasing ativado para glifos cirílicos"
    Assert-Test ($textToml -match 'smartSDFShaders\s*=\s*true') "Modern UI smartSDFShaders ativado para nitidez em qualquer escala"
    Assert-Test ($textToml -match 'allowSDFTextIn2D\s*=\s*true') "Modern UI allowSDFTextIn2D ativado"
}

$lrConfigPath = Join-Path $configDir "languagereload.json"
$lrConfigExists = Test-Path $lrConfigPath
Assert-Test $lrConfigExists "languagereload.json existe em config\"

if ($lrConfigExists) {
    try {
        $lrJson = [System.IO.File]::ReadAllText($lrConfigPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        Assert-Test ($lrJson.multilingualItemSearch -eq $true) "Language Reload com busca multilíngue ativada"
        Assert-Test ($lrJson.fallbacks -contains "ru_ru") "Language Reload com fallback para ru_ru"
        Assert-Test ($lrJson.fallbacks -contains "pt_br") "Language Reload com fallback para pt_br"
    } catch {
        Assert-Test $false "Erro ao ler languagereload.json: $_"
    }
}

# -------------------------------------------------------------------------
# TEST GROUP 3: Validacao do Curriculo de Russo (Heracles 4 Tiers & UTF-8)
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 3/4] Validacao do Curriculo de Russo (68 Quests nos 4 Tiers) ---" -ForegroundColor Yellow

$groupsTxtPath = Join-Path $heraclesDir "groups.txt"
$groupsTxtExists = Test-Path $groupsTxtPath
Assert-Test $groupsTxtExists "heracles/groups.txt existe"

if ($groupsTxtExists) {
    $groupsLines = [System.IO.File]::ReadAllLines($groupsTxtPath, [System.Text.Encoding]::UTF8) | Where-Object { -not [string]::IsNullOrWhiteSpace($_) }
    Assert-Test ($groupsLines.Count -eq 4) "heracles/groups.txt contem exatamente 4 tiers cadastrados"
    Assert-Test ($groupsLines -contains "tier1_novice") "Tier 1 Novice listado em groups.txt"
    Assert-Test ($groupsLines -contains "tier2_apprentice") "Tier 2 Apprentice listado em groups.txt"
    Assert-Test ($groupsLines -contains "tier3_journeyman") "Tier 3 Journeyman listado em groups.txt"
    Assert-Test ($groupsLines -contains "tier4_master") "Tier 4 Master listado em groups.txt"
}

$expectedTiers = @(
    @{ Name = "tier1_novice"; Count = 16 },
    @{ Name = "tier2_apprentice"; Count = 18 },
    @{ Name = "tier3_journeyman"; Count = 18 },
    @{ Name = "tier4_master"; Count = 16 }
)

$totalQuestsFound = 0
$allQuestsValid = $true
$cyrillicPattern = "[\u0400-\u04FF]"
$mojibakePattern = "[ÃÐÑâ][\x80-\xFF]|\uFFFD"

foreach ($t in $expectedTiers) {
    $tierDir = Join-Path $heraclesQuestsDir $t.Name
    $dirExists = Test-Path $tierDir
    Assert-Test $dirExists "Diretorio existe: quests/$($t.Name)"

    if ($dirExists) {
        $files = Get-ChildItem -Path $tierDir -Filter "*.json"
        $countOk = ($files.Count -eq $t.Count)
        Assert-Test $countOk "Quests encontradas em $($t.Name): $($files.Count) de $($t.Count) esperadas"
        $totalQuestsFound += $files.Count

        foreach ($f in $files) {
            try {
                $rawText = [System.IO.File]::ReadAllText($f.FullName, [System.Text.Encoding]::UTF8)
                
                # Teste anti-mojibake / integridade UTF-8
                if ($rawText -match $mojibakePattern) {
                    Write-Host "    [AVISO] Possivel mojibake detectado em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                $quest = $rawText | ConvertFrom-Json

                # Validar schema Heracles
                if (-not $quest.display) {
                    Write-Host "    [ERRO] Campo 'display' ausente em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not ($quest.display.title -match $cyrillicPattern)) {
                    Write-Host "    [ERRO] Titulo sem caracteres cirílicos em $($f.Name): $($quest.display.title)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not $quest.display.subtitle) {
                    Write-Host "    [ERRO] Subtitulo fonetico ausente em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not $quest.display.description -or $quest.display.description.Count -lt 5) {
                    Write-Host "    [ERRO] Descricao pedagogica incompleta em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not $quest.display.icon -or -not $quest.display.icon.item) {
                    Write-Host "    [ERRO] Icone de item ausente em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not $quest.display.groups."$($t.Name)") {
                    Write-Host "    [ERRO] Grupo '$($t.Name)' nao mapeado no display de $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }

                if (-not $quest.tasks) {
                    Write-Host "    [ERRO] Tarefas ausentes em $($f.Name)" -ForegroundColor Red
                    $allQuestsValid = $false
                }
            } catch {
                Write-Host "    [ERRO FATAL] Falha de parse JSON em $($f.Name): $_" -ForegroundColor Red
                $allQuestsValid = $false
            }
        }
    }
}

Assert-Test ($totalQuestsFound -eq 68) "Total consolidado de quests Heracles = 68 (16+18+18+16)"
Assert-Test $allQuestsValid "Integridade de schema e codificacao UTF-8 cirílica em todas as 68 quests"

# -------------------------------------------------------------------------
# TEST GROUP 4: Tooltips Bilingues & Transliteracao Fonetica
# -------------------------------------------------------------------------
Write-Host "`n--- [TEST 4/4] Validacao dos Tooltips Bilingues (KubeJS & Resource Pack) ---" -ForegroundColor Yellow

$kubejsExists = Test-Path $kubejsScriptPath
Assert-Test $kubejsExists "Script KubeJS presente: kubejs/client_scripts/russian_tooltips.js"

if ($kubejsExists) {
    $scriptContent = [System.IO.File]::ReadAllText($kubejsScriptPath, [System.Text.Encoding]::UTF8)
    Assert-Test ($scriptContent -match "ItemEvents\.tooltip") "Script KubeJS intercepta evento ItemEvents.tooltip"
    Assert-Test ($scriptContent -match "\[RU\]") "Script KubeJS injeta tag [RU]"
    Assert-Test ($scriptContent -match "\[Pronúncia\]") "Script KubeJS injeta tag [Pronúncia]"
    Assert-Test ($scriptContent -match "\[PT\]") "Script KubeJS injeta tag [PT]"
    Assert-Test ($scriptContent -match "\[Nível\]") "Script KubeJS injeta tag [Nível]"

    # Contagem de itens no dicionário KubeJS
    $itemMatches = [regex]::Matches($scriptContent, "'minecraft:[a-z0-9_]+':\s*\{")
    Assert-Test ($itemMatches.Count -ge 68) "Dicionario KubeJS mapeia todos os 68 itens pedagógicos (encontrados: $($itemMatches.Count))"
}

$packMcmetaPath = Join-Path $packDir "pack.mcmeta"
Assert-Test (Test-Path $packMcmetaPath) "Resource Pack possui pack.mcmeta valido"

$ruRuPackPath = Join-Path $packDir "assets\minecraft\lang\ru_ru.json"
$ptBrPackPath = Join-Path $packDir "assets\minecraft\lang\pt_br.json"
$ruRuExists = Test-Path $ruRuPackPath
$ptBrExists = Test-Path $ptBrPackPath

Assert-Test $ruRuExists "Resource Pack contem assets/minecraft/lang/ru_ru.json"
Assert-Test $ptBrExists "Resource Pack contem assets/minecraft/lang/pt_br.json"

if ($ruRuExists) {
    try {
        $ruPack = [System.IO.File]::ReadAllText($ruRuPackPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        $entryCount = ($ruPack | Get-Member -MemberType NoteProperty).Count
        Assert-Test ($entryCount -ge 136) "ru_ru.json bilingue contem entradas de itens e blocos ($entryCount entradas)"
    } catch {
        Assert-Test $false "Erro de parse em ru_ru.json do resource pack: $_"
    }
}

if ($ptBrExists) {
    try {
        $ptPack = [System.IO.File]::ReadAllText($ptBrPackPath, [System.Text.Encoding]::UTF8) | ConvertFrom-Json
        $entryCount = ($ptPack | Get-Member -MemberType NoteProperty).Count
        Assert-Test ($entryCount -ge 136) "pt_br.json bilingue contem entradas de itens e blocos ($entryCount entradas)"
    } catch {
        Assert-Test $false "Erro de parse em pt_br.json do resource pack: $_"
    }
}

# -------------------------------------------------------------------------
# RELATORIO FINAL
# -------------------------------------------------------------------------
Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host "                     RESULTADO DOS TESTES (M3)                        " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Total de Checagens Realizadas: $($passedChecks + $failedChecks)" -ForegroundColor White
Write-Host " Checagens Aprovadas [PASS]:     $passedChecks" -ForegroundColor Green
Write-Host " Checagens Falhadas  [FAIL]:     $failedChecks" -ForegroundColor $(if ($failedChecks -eq 0) { "Green" } else { "Red" })

if ($failedChecks -eq 0) {
    Write-Host "`n>>> [SUCESSO ABSOLUTO] O Milestone M3 esta 100% validado e em conformidade! <<<" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n>>> [FALHA] Existem $failedChecks checagens reprovadas no Milestone M3. <<<" -ForegroundColor Red
    exit 1
}
