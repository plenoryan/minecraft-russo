# setup_progression.ps1 - Automated Setup for Russian Pedagogical Progression & Font Stack (Fabric 1.20.1)
[CmdletBinding()]
param (
    [string]$BaseDir = "F:\Jogos\Mine",
    [switch]$ForceRedownload = $false
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8
[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$Utf8NoBom = New-Object System.Text.UTF8Encoding($false)

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " Configuracao da Progressao Pedagogica de Russo & Tipografia Cirilica " -ForegroundColor Cyan
Write-Host " Minecraft 1.20.1 Fabric | Heracles Quests + Modern UI + Bilingual HUD" -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

$instanceDir = Join-Path $BaseDir "Instancia_Cliente"
$modsDir = Join-Path $instanceDir "mods"
$configDir = Join-Path $instanceDir "config"
$heraclesDir = Join-Path $configDir "heracles"
$heraclesQuestsDir = Join-Path $heraclesDir "quests"
$kubejsDir = Join-Path $instanceDir "kubejs"
$kubejsClientScriptsDir = Join-Path $kubejsDir "client_scripts"
$resourcePacksDir = Join-Path $instanceDir "resourcepacks"
$packDir = Join-Path $resourcePacksDir "Russian_Bilingual_Educational_Pack"

$dirsToEnsure = @(
    $modsDir,
    $configDir,
    $heraclesDir,
    $heraclesQuestsDir,
    (Join-Path $heraclesQuestsDir "tier1_novice"),
    (Join-Path $heraclesQuestsDir "tier2_apprentice"),
    (Join-Path $heraclesQuestsDir "tier3_journeyman"),
    (Join-Path $heraclesQuestsDir "tier4_master"),
    $kubejsClientScriptsDir,
    (Join-Path $packDir "assets\minecraft\lang")
)

foreach ($d in $dirsToEnsure) {
    if (-not (Test-Path $d)) {
        New-Item -ItemType Directory -Path $d -Force | Out-Null
    }
}

# -------------------------------------------------------------------------
# ETAPA 1: Download e Verificacao dos Mods Educacionais e de Tipografia
# -------------------------------------------------------------------------
Write-Host "`n[1/5] Verificando e instalando mods educacionais (Fabric 1.20.1)..." -ForegroundColor Yellow

$educationalMods = @(
    @{
        Slug = "modern-ui"
        Name = "Modern UI (TrueType/DirectWrite font engine)"
        Filename = "ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar"
        Url = "https://cdn.modrinth.com/data/3sjzyvGR/versions/X2Gc2jpP/ModernUI-Fabric-1.20.1-3.12.0.1-universal.jar"
        MinBytes = 20000000
    },
    @{
        Slug = "forge-config-api-port"
        Name = "Forge Config API Port (Modern UI Dep)"
        Filename = "ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar"
        Url = "https://cdn.modrinth.com/data/ohNO6lps/versions/HvR3IdRE/ForgeConfigAPIPort-v8.0.3-1.20.1-Fabric.jar"
        MinBytes = 300000
    },
    @{
        Slug = "heracles"
        Name = "Heracles (Odyssey Quests Engine)"
        Filename = "Heracles-fabric-1.20.1-1.1.13.jar"
        Url = "https://cdn.modrinth.com/data/lo90fZoB/versions/Iw91uuFK/Heracles-fabric-1.20.1-1.1.13.jar"
        MinBytes = 1000000
    },
    @{
        Slug = "heracles-button"
        Name = "Heracles Button (Inventory Quest Button)"
        Filename = "heraclesbutton-1.0.0.jar"
        Url = "https://cdn.modrinth.com/data/HibJK04a/versions/9fHNJX8w/heraclesbutton-1.0.0.jar"
        MinBytes = 100000
    },
    @{
        Slug = "resourceful-lib"
        Name = "Resourceful Lib (Heracles Dep)"
        Filename = "resourcefullib-fabric-1.20.1-2.1.29.jar"
        Url = "https://cdn.modrinth.com/data/G1hIVOrD/versions/UOdaYbhh/resourcefullib-fabric-1.20.1-2.1.29.jar"
        MinBytes = 300000
    },
    @{
        Slug = "language-reload"
        Name = "Language Reload (Instant Shift+F3+J & Multilingual Search)"
        Filename = "language-reload-1.7.5+1.20.1.jar"
        Url = "https://cdn.modrinth.com/data/uLbm7CG6/versions/UmzbPud1/language-reload-1.7.5%2B1.20.1.jar"
        MinBytes = 50000
    },
    @{
        Slug = "jade"
        Name = "Jade (In-World Block & Entity Russian Identifier)"
        Filename = "Jade-1.20-Fabric-11.13.3.jar"
        Url = "https://cdn.modrinth.com/data/nvQzSEkH/versions/ahavHbAT/Jade-1.20-Fabric-11.13.3.jar"
        MinBytes = 400000
    },
    @{
        Slug = "kubejs"
        Name = "KubeJS (Client-Side Dynamic Bilingual Tooltips)"
        Filename = "kubejs-fabric-2001.6.5-build.26.jar"
        Url = "https://cdn.modrinth.com/data/umyGl7zF/versions/Pu0Ygbq6/kubejs-fabric-2001.6.5-build.26.jar"
        MinBytes = 1000000
    },
    @{
        Slug = "rhino"
        Name = "Rhino (JavaScript Engine for KubeJS)"
        Filename = "rhino-fabric-2001.2.3-build.10.jar"
        Url = "https://cdn.modrinth.com/data/sk9knFPE/versions/MLIu0Tct/rhino-fabric-2001.2.3-build.10.jar"
        MinBytes = 1000000
    },
    @{
        Slug = "architectury-api"
        Name = "Architectury API (KubeJS Dep)"
        Filename = "architectury-9.2.14-fabric.jar"
        Url = "https://cdn.modrinth.com/data/lhGA9TYQ/versions/WbL7MStR/architectury-9.2.14-fabric.jar"
        MinBytes = 500000
    }
)

$webClient = New-Object System.Net.WebClient
$webClient.Headers.Add("User-Agent", "MinecraftRussoModpack/1.0 (Windows 11)")

try {
    foreach ($mod in $educationalMods) {
        $destPath = Join-Path $modsDir $mod.Filename
        $needDownload = $true

        if (Test-Path $destPath) {
            $len = (Get-Item $destPath).Length
            if ($len -ge $mod.MinBytes -and -not $ForceRedownload) {
                Write-Host "  [OK] $($mod.Name) ($([math]::Round($len / 1KB, 1)) KB)" -ForegroundColor Green
                $needDownload = $false
            }
        }

        if ($needDownload) {
            Write-Host "  Baixando $($mod.Name)..." -ForegroundColor Cyan
            $webClient.DownloadFile($mod.Url, $destPath)
            $len = (Get-Item $destPath).Length
            if ($len -lt $mod.MinBytes) {
                throw "Tamanho inesperado para $($mod.Name): $len bytes"
            }
            Write-Host "  [SUCESSO] Baixado ($([math]::Round($len / 1KB, 1)) KB)" -ForegroundColor Green
        }
    }
} finally {
    $webClient.Dispose()
}

# -------------------------------------------------------------------------
# ETAPA 2: Configuracao do Modern UI para Renderizacao Vetorial de Fontes Cirilicas
# -------------------------------------------------------------------------
Write-Host "`n[2/5] Configurando Modern UI (Fontes TrueType / DirectWrite / Segoe UI)..." -ForegroundColor Yellow

$modernUiTextConfig = @"
# Modern UI Text Engine Configuration (TrueType / DirectWrite Cyrillic Support)
[text]
    # Allow text renderer to drop shadow, setting to false can improve performance.
    allowShadow = true
    # Fix resolution level at 2.
    fixedResolution = false
    # Base font size for layout calculation.
    baseFontSize = 12.0
    # Baseline shift
    baselineShift = 0.0
    # Shadow offset
    shadowOffset = 0.75
    # Outline offset
    outlineOffset = 0.75
    # Bitmap offset
    bitmapOffset = 0.0
    # Cache lifespan
    cacheLifespan = 60
    # Text direction: "DEFAULT", "LTR", "RTL"
    textDirection = "DEFAULT"
    # Use text shaders in world
    useTextShadersInWorld = true
    # Default font behavior: "ALL" forces TrueType vector rendering for all texts including Cyrillic
    defaultFontBehavior = "ALL"
    # Default font rule set
    defaultFontRuleSet = ["^minecraft:font\\/(nonlatin_european|accented|ascii|element_ideographs|cjk_punctuations|ellipsis|2em_dash)\\.png$", "^minecraft:include\\/space \\/ minecraft:space$", "^minecraft:font\\/(mcsans|emoji)_05_00\\d.png$"]
    # Use component cache
    useComponentCache = true
    # Allow async layout
    allowAsyncLayout = true
    # Line break style: "NORMAL", "LOOSE", "STRICT"
    lineBreakStyle = "NORMAL"
    # Line break word style: "NORMAL", "PHRASE"
    lineBreakWordStyle = "NORMAL"
    # Smart Signed Distance Field (SDF) shaders
    smartSDFShaders = true
    # Compute device font size
    computeDeviceFontSize = true
    # Allow SDF text in 2D interfaces
    allowSDFTextIn2D = true
    # Tweak experience text
    tweakExperienceText = true
    # Subpixel anti-aliasing for smooth Cyrillic letters
    antiAliasing = true
    # Linear metrics for clear proportional text
    linearMetrics = true
    # Minimum pixel density for SDF
    minPixelDensityForSDF = 4
    # Linear sampling A8 atlas
    linearSamplingA8Atlas = false
"@

$modernUiClientConfig = @"
# Modern UI Client Interface Configuration
[client]
    # Primary font family for Cyrillic and Latin text rendering
    firstFontFamily = "Segoe UI"
    # Fallback font families in order of priority
    fallbackFontFamilyList = ["Segoe UI", "Inter", "Arial", "sans-serif"]
    # Font scale (1.0 = standard)
    fontScale = 1.0
    # Enable linear metrics
    linearMetrics = true
    # Tooltip improvements
    tooltip = true
    roundedTooltip = true
    tooltipLineWrapping = true
    centerTooltipTitle = false
    adaptiveTooltipColors = true
"@

$textTomlPath = Join-Path $configDir "modernui-text.toml"
$clientTomlPath = Join-Path $configDir "modernui-client.toml"
[System.IO.File]::WriteAllText($textTomlPath, $modernUiTextConfig, $Utf8NoBom)
[System.IO.File]::WriteAllText($clientTomlPath, $modernUiClientConfig, $Utf8NoBom)
Write-Host "  [OK] modernui-text.toml e modernui-client.toml configurados com sucesso." -ForegroundColor Green

# Configurar Language Reload
$languageReloadConfig = @"
{
  "version": 1,
  "multilingualItemSearch": true,
  "removableDefaultLanguage": false,
  "fallbacks": [
    "ru_ru",
    "pt_br",
    "en_us"
  ],
  "previousFallbacks": [],
  "language": "ru_ru",
  "previousLanguage": "pt_br"
}
"@
$lrConfigPath = Join-Path $configDir "languagereload.json"
[System.IO.File]::WriteAllText($lrConfigPath, $languageReloadConfig, $Utf8NoBom)
Write-Host "  [OK] languagereload.json configurado com busca multilíngue e fallback ru_ru / pt_br." -ForegroundColor Green

# -------------------------------------------------------------------------
# ETAPA 3: Curriculo Pedagogico de Russo em 4 Tiers (68 Itens Estruturados)
# -------------------------------------------------------------------------
Write-Host "`n[3/5] Construindo o Curriculo Pedagogico de Russo em 4 Tiers (Heracles UTF-8)..." -ForegroundColor Yellow

$groupsTxtContent = @"
tier1_novice
tier2_apprentice
tier3_journeyman
tier4_master
"@
$groupsTxtPath = Join-Path $heraclesDir "groups.txt"
[System.IO.File]::WriteAllText($groupsTxtPath, $groupsTxtContent, $Utf8NoBom)

# Catalogo completo com todos os 68 itens
$curriculum = @(
    # --- TIER 1: NOVICE (16 itens) ---
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "01_derevo"
        ItemId = "minecraft:oak_log"
        Ru = "Дéрево"
        Phon = "[DYE-rye-va]"
        Gender = "Neutro (с.р.)"
        Pt = "Madeira / Árvore"
        Grammar = "A primeira sílaba é tônica (ДЕ-ре-во). A vogal 'o' átona final sofre redução vocálica (ákanie), soando como [a]."
        TimerSec = 18
        Pos = @(0, 0)
        Deps = @()
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "02_doski"
        ItemId = "minecraft:oak_planks"
        Ru = "Дóски"
        Phon = "[DOS-kee]"
        Gender = "Plural (мн.ч.)"
        Pt = "Tábuas de Carvalho"
        Grammar = "'O' tônico longo e aberto. Substantivo no plural com terminação regular em -и."
        TimerSec = 18
        Pos = @(48, 0)
        Deps = @("01_derevo")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "03_palka"
        ItemId = "minecraft:stick"
        Ru = "Пáлка"
        Phon = "[PAHL-ka]"
        Gender = "Feminino (ж.р.)"
        Pt = "Graveto"
        Grammar = "Terminação regular feminina em -а. Acento tônico na primeira sílaba."
        TimerSec = 18
        Pos = @(96, 0)
        Deps = @("02_doski")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "04_kamen"
        ItemId = "minecraft:stone"
        Ru = "Кáмень"
        Phon = "[KAH-myen']"
        Gender = "Masculino (м.р.)"
        Pt = "Pedra"
        Grammar = "O sinal brando final (ь) suaviza a consoante dental 'н', tornando-a palatizada [n']."
        TimerSec = 18
        Pos = @(0, 48)
        Deps = @("01_derevo")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "05_bulyzhnik"
        ItemId = "minecraft:cobblestone"
        Ru = "Булӹжник"
        Phon = "[boo-LIZH-neek]"
        Gender = "Masculino (м.р.)"
        Pt = "Pedregulho"
        Grammar = "Contém a vogal gutural posterior dura 'ы'. A sílaba tônica é -лы-."
        TimerSec = 18
        Pos = @(48, 48)
        Deps = @("04_kamen")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "06_zemlya"
        ItemId = "minecraft:dirt"
        Ru = "Земл́я"
        Phon = "[zyem-LYAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Terra"
        Grammar = "Substantivo feminino oxítono terminado em -я. A primeira sílaba 'зем-' é suave."
        TimerSec = 18
        Pos = @(96, 48)
        Deps = @("01_derevo")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "07_pesok"
        ItemId = "minecraft:sand"
        Ru = "Песóк"
        Phon = "[pye-SOK]"
        Gender = "Masculino (м.р.)"
        Pt = "Areia"
        Grammar = "A vogal 'e' inicial é branda [pye]. Acento oxítono no 'o' final."
        TimerSec = 18
        Pos = @(144, 48)
        Deps = @("06_zemlya")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "08_topor"
        ItemId = "minecraft:wooden_axe"
        Ru = "Топóр"
        Phon = "[ta-POR]"
        Gender = "Masculino (м.р.)"
        Pt = "Machado de Madeira"
        Grammar = "Clássica redução vocálica russa: o primeiro 'o' átono reduz para som de [a]."
        TimerSec = 18
        Pos = @(0, 96)
        Deps = @("02_doski", "03_palka")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "09_kirka"
        ItemId = "minecraft:wooden_pickaxe"
        Ru = "Киркá"
        Phon = "[keer-KAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Picareta de Madeira"
        Grammar = "Substantivo feminino oxítono com tônica na última sílaba (-КА)."
        TimerSec = 18
        Pos = @(48, 96)
        Deps = @("02_doski", "03_palka")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "10_lopata"
        ItemId = "minecraft:wooden_shovel"
        Ru = "Лопáта"
        Phon = "[la-PAH-ta]"
        Gender = "Feminino (ж.р.)"
        Pt = "Pá de Madeira"
        Grammar = "O primeiro 'o' reduz para [a]. Tônica paroxítona em -ПА-."
        TimerSec = 18
        Pos = @(96, 96)
        Deps = @("02_doski", "03_palka")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "11_fakel"
        ItemId = "minecraft:torch"
        Ru = "Фáкел"
        Phon = "[FAH-kyel]"
        Gender = "Masculino (м.р.)"
        Pt = "Tocha"
        Grammar = "Origem germânica integrada ao russo. Tônica em ФА-. O 'е' final é suave."
        TimerSec = 18
        Pos = @(144, 96)
        Deps = @("03_palka")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "12_verstak"
        ItemId = "minecraft:crafting_table"
        Ru = "Верстáк"
        Phon = "[vyer-STAHK]"
        Gender = "Masculino (м.р.)"
        Pt = "Bancada de Trabalho"
        Grammar = "Consoante final dura 'к'. Tônica oxítona na sílaba final -СТАК."
        TimerSec = 18
        Pos = @(0, 144)
        Deps = @("02_doski")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "13_pech"
        ItemId = "minecraft:furnace"
        Ru = "Печь"
        Phon = "[Pyech']"
        Gender = "Feminino (ж.р.)"
        Pt = "Fornalha"
        Grammar = "Substantivo da 3ª declinação feminina terminado em sinal brando (ь). 'Ч' é sempre suave."
        TimerSec = 18
        Pos = @(48, 144)
        Deps = @("05_bulyzhnik")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "14_sunduk"
        ItemId = "minecraft:chest"
        Ru = "Сундýк"
        Phon = "[soon-DOOK]"
        Gender = "Masculino (м.р.)"
        Pt = "Baú"
        Grammar = "Vogal posterior 'у' tônica na última sílaba. Consoante 'к' dura no final."
        TimerSec = 18
        Pos = @(96, 144)
        Deps = @("02_doski")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "15_dver"
        ItemId = "minecraft:oak_door"
        Ru = "Дверь"
        Phon = "[Dvyer']"
        Gender = "Feminino (ж.р.)"
        Pt = "Porta"
        Grammar = "Monossílabo feminino com 'р' palatizado pelo sinal brando final."
        TimerSec = 18
        Pos = @(144, 144)
        Deps = @("02_doski")
    },
    @{
        Tier = "tier1_novice"
        TierNum = 1
        FileId = "16_steklo"
        ItemId = "minecraft:glass"
        Ru = "Стеклó"
        Phon = "[styek-LO]"
        Gender = "Neutro (с.р.)"
        Pt = "Vidro"
        Grammar = "Oxítona neutra terminada em -о pleno. O encontro 'ст' é sonoro."
        TimerSec = 18
        Pos = @(192, 144)
        Deps = @("07_pesok", "13_pech")
    },

    # --- TIER 2: APPRENTICE (18 itens) ---
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "17_khleb"
        ItemId = "minecraft:bread"
        Ru = "Хлеб"
        Phon = "[Khlyep]"
        Gender = "Masculino (м.р.)"
        Pt = "Pão"
        Grammar = "Dessonorização da consoante final em russo: a letra 'б' em fim de palavra soa como [p]."
        TimerSec = 12
        Pos = @(0, 0)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "18_voda"
        ItemId = "minecraft:water_bucket"
        Ru = "Водá"
        Phon = "[va-DAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Balde com Água / Água"
        Grammar = "Exemplo supremo de redução vocálica (ákanie): o 'о' átono vira [a], a tônica é no -ДА."
        TimerSec = 12
        Pos = @(48, 0)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "19_yabloko"
        ItemId = "minecraft:apple"
        Ru = "Я́блоко"
        Phon = "[YAH-bla-ka]"
        Gender = "Neutro (с.р.)"
        Pt = "Maçã"
        Grammar = "Inicia com a vogal iotizada 'я' [ya]. As duas letras 'о' seguintes são reduzidas para som de [a]."
        TimerSec = 12
        Pos = @(96, 0)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "20_pshenitsa"
        ItemId = "minecraft:wheat"
        Ru = "Пшенúца"
        Phon = "[pshe-NEE-tsa]"
        Gender = "Feminino (ж.р.)"
        Pt = "Trigo"
        Grammar = "Encontro consonantal 'пш' duro. A letra 'ц' soa como [ts]."
        TimerSec = 12
        Pos = @(0, 48)
        Deps = @("17_khleb")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "21_myaso"
        ItemId = "minecraft:beef"
        Ru = "Мя́со"
        Phon = "[MYAH-sa]"
        Gender = "Neutro (с.р.)"
        Pt = "Carne Crua"
        Grammar = "Substantivo neutro. 'М' palatizado por 'я'. O 'о' final reduz para [a]."
        TimerSec = 12
        Pos = @(48, 48)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "22_govyadina"
        ItemId = "minecraft:cooked_beef"
        Ru = "Говя́дина"
        Phon = "[ga-VYA-dee-na]"
        Gender = "Feminino (ж.р.)"
        Pt = "Carne Bovina / Bife"
        Grammar = "Sufixo de carne animal -ина. 'О' inicial reduzido para [a], tônica em -ВЯ-."
        TimerSec = 12
        Pos = @(96, 48)
        Deps = @("21_myaso")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "23_svinina"
        ItemId = "minecraft:porkchop"
        Ru = "Свинúна"
        Phon = "[svee-NEE-na]"
        Gender = "Feminino (ж.р.)"
        Pt = "Carne de Porco"
        Grammar = "Derivado de свинья (porco) com o sufixo -ина. Tônica em -НИ-."
        TimerSec = 12
        Pos = @(144, 48)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "24_ryba"
        ItemId = "minecraft:cod"
        Ru = "Р́ыба"
        Phon = "[RY-ba]"
        Gender = "Feminino (ж.р.)"
        Pt = "Peixe"
        Grammar = "A vogal dura 'ы' requer posicionamento posterior da língua. Tônica em РЫ-."
        TimerSec = 12
        Pos = @(192, 48)
        Deps = @("18_voda")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "25_sherst"
        ItemId = "minecraft:white_wool"
        Ru = "Шерсть"
        Phon = "[Sherst']"
        Gender = "Feminino (ж.р.)"
        Pt = "Lã"
        Grammar = "A consoante 'ш' é sempre dura no russo. O sinal brando final suaviza o 'т'."
        TimerSec = 12
        Pos = @(0, 96)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "26_krovat"
        ItemId = "minecraft:white_bed"
        Ru = "Кровáть"
        Phon = "[kra-VAHT']"
        Gender = "Feminino (ж.р.)"
        Pt = "Cama"
        Grammar = "Substantivo feminino da 3ª declinação. 'О' átono vira [a], 'т' palatizado no fim."
        TimerSec = 12
        Pos = @(48, 96)
        Deps = @("25_sherst")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "27_ugol"
        ItemId = "minecraft:coal"
        Ru = "У́голь"
        Phon = "[OO-gahl']"
        Gender = "Masculino (м.р.)"
        Pt = "Carvão"
        Grammar = "Tônica na primeira sílaba (У-голь). O 'л' é suavizado pelo sinal brando [l']."
        TimerSec = 12
        Pos = @(96, 96)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "28_vedro"
        ItemId = "minecraft:bucket"
        Ru = "Ведрó"
        Phon = "[vye-DRO]"
        Gender = "Neutro (с.р.)"
        Pt = "Balde"
        Grammar = "Substantivo neutro oxítono terminado em -о pleno. Primeira sílaba suave [vye]."
        TimerSec = 12
        Pos = @(144, 96)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "29_korova"
        ItemId = "minecraft:cow_spawn_egg"
        Ru = "Корóва"
        Phon = "[ka-RO-va]"
        Gender = "Feminino (ж.р.)"
        Pt = "Vaca"
        Grammar = "Primeiro 'o' reduzido para [a]. Tônica na sílaba central -РО-."
        TimerSec = 12
        Pos = @(0, 144)
        Deps = @("21_myaso")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "30_ovtsa"
        ItemId = "minecraft:sheep_spawn_egg"
        Ru = "Овцá"
        Phon = "[af-TSAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Ovelha"
        Grammar = "'О' inicial átono vira [a]. Letra 'ц' africada sonora [ts]. Tônica final."
        TimerSec = 12
        Pos = @(48, 144)
        Deps = @("25_sherst")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "31_svinya"
        ItemId = "minecraft:pig_spawn_egg"
        Ru = "Свинь́я"
        Phon = "[sveen-YAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Porco"
        Grammar = "Uso do sinal brando separador (разделительный мягкий знак) antes da vogal 'я'."
        TimerSec = 12
        Pos = @(96, 144)
        Deps = @("23_svinina")
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "32_kuritsa"
        ItemId = "minecraft:chicken"
        Ru = "Кýрица"
        Phon = "[KOO-ree-tsa]"
        Gender = "Feminino (ж.р.)"
        Pt = "Frango / Galinha"
        Grammar = "Terminação típica russa em -ица para fêmeas de animais. Tônica na primeira sílaba."
        TimerSec = 12
        Pos = @(144, 144)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "33_morkov"
        ItemId = "minecraft:carrot"
        Ru = "Моркóвь"
        Phon = "[mar-KOF']"
        Gender = "Feminino (ж.р.)"
        Pt = "Cenoura"
        Grammar = "Dessonorização da consoante 'в' final diante de sinal brando, soando como [f']."
        TimerSec = 12
        Pos = @(192, 144)
        Deps = @()
    },
    @{
        Tier = "tier2_apprentice"
        TierNum = 2
        FileId = "34_kartofel"
        ItemId = "minecraft:potato"
        Ru = "Картóфель"
        Phon = "[kar-TO-fyel']"
        Gender = "Masculino (м.р.)"
        Pt = "Batata"
        Grammar = "Substantivo coletivo masculino derivado do alemão. Tônica em -ТО-. 'Л' brando final."
        TimerSec = 12
        Pos = @(240, 144)
        Deps = @()
    },

    # --- TIER 3: JOURNEYMAN (18 itens) ---
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "35_zhelezo"
        ItemId = "minecraft:iron_ingot"
        Ru = "Желéзо"
        Phon = "[zhe-LYE-za]"
        Gender = "Neutro (с.р.)"
        Pt = "Barra de Ferro"
        Grammar = "A consoante 'ж' é sempre dura. O 'е' seguinte é brando [lye], e o 'о' final reduz."
        TimerSec = 8
        Pos = @(0, 0)
        Deps = @()
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "36_zoloto"
        ItemId = "minecraft:gold_ingot"
        Ru = "Зóлото"
        Phon = "[ZO-la-ta]"
        Gender = "Neutro (с.р.)"
        Pt = "Barra de Ouro"
        Grammar = "Três letras 'о', mas apenas a primeira é tônica! As duas últimas reduzem para som de [a]."
        TimerSec = 8
        Pos = @(48, 0)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "37_almaz"
        ItemId = "minecraft:diamond"
        Ru = "Алмáз"
        Phon = "[ahl-MAHS]"
        Gender = "Masculino (м.р.)"
        Pt = "Diamante"
        Grammar = "Dessonorização final: a consoante sonora 'з' em fim de palavra soa como o surdo [s]."
        TimerSec = 8
        Pos = @(96, 0)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "38_myech"
        ItemId = "minecraft:iron_sword"
        Ru = "Меч"
        Phon = "[Myech]"
        Gender = "Masculino (м.р.)"
        Pt = "Espada de Ferro"
        Grammar = "Monossílabo masculino forte. A letra 'ч' é pronunciada com suavidade inerente."
        TimerSec = 8
        Pos = @(0, 48)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "39_shchit"
        ItemId = "minecraft:shield"
        Ru = "Щит"
        Phon = "[Shcheet]"
        Gender = "Masculino (м.р.)"
        Pt = "Escudo"
        Grammar = "A letra russa 'щ' representa uma consoante fricativa palatal longa [ɕː] (shch suave)."
        TimerSec = 8
        Pos = @(48, 48)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "40_luk"
        ItemId = "minecraft:bow"
        Ru = "Лук"
        Phon = "[Look]"
        Gender = "Masculino (м.р.)"
        Pt = "Arco"
        Grammar = "Monossílabo curto. Curiosidade: 'лук' também significa 'cebola' em russo!"
        TimerSec = 8
        Pos = @(96, 48)
        Deps = @()
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "41_strela"
        ItemId = "minecraft:arrow"
        Ru = "Стрелá"
        Phon = "[strye-LAH]"
        Gender = "Feminino (ж.р.)"
        Pt = "Flecha"
        Grammar = "Oxítona feminina. Encontro consonantal 'стр' dinâmico e tônica em -ЛА."
        TimerSec = 8
        Pos = @(144, 48)
        Deps = @("40_luk")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "42_shlem"
        ItemId = "minecraft:iron_helmet"
        Ru = "Шлем"
        Phon = "[Shlyem]"
        Gender = "Masculino (м.р.)"
        Pt = "Capacete de Ferro"
        Grammar = "'Ш' sempre duro seguido de 'л' suavizado por 'е'."
        TimerSec = 8
        Pos = @(0, 96)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "43_nagrudnik"
        ItemId = "minecraft:iron_chestplate"
        Ru = "Нагрýдник"
        Phon = "[na-GROOD-neek]"
        Gender = "Masculino (м.р.)"
        Pt = "Peitoral de Ferro"
        Grammar = "Formado pelo prefixo na- (sobre) + грудь (peito) + sufixo de objeto -ник."
        TimerSec = 8
        Pos = @(48, 96)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "44_ponozhi"
        ItemId = "minecraft:iron_leggings"
        Ru = "Пóножи"
        Phon = "[PO-na-zhee]"
        Gender = "Plural (мн.ч.)"
        Pt = "Calças de Ferro"
        Grammar = "Termo de armadura clássica russa. 'Ж' sempre duro, sufixo plural em -и."
        TimerSec = 8
        Pos = @(96, 96)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "45_botinki"
        ItemId = "minecraft:iron_boots"
        Ru = "Ботúнки"
        Phon = "[ba-TEEN-kee]"
        Gender = "Plural (мн.ч.)"
        Pt = "Botas de Ferro"
        Grammar = "Substantivo plural regular para calçados de cano médio. Tônica em -ТИН-."
        TimerSec = 8
        Pos = @(144, 96)
        Deps = @("35_zhelezo")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "46_zombi"
        ItemId = "minecraft:zombie_head"
        Ru = "Зóмби"
        Phon = "[ZOM-bee]"
        Gender = "Masculino (м.р.)"
        Pt = "Zumbi"
        Grammar = "Substantivo de origem estrangeira indeclinável (несклоняемое существительное)."
        TimerSec = 8
        Pos = @(0, 144)
        Deps = @("38_myech")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "47_skelet"
        ItemId = "minecraft:skeleton_skull"
        Ru = "Скелéт"
        Phon = "[skye-LYET]"
        Gender = "Masculino (м.р.)"
        Pt = "Esqueleto"
        Grammar = "Oxítona terminada em dental dura 'т'. Consoante 'л' branda antes de 'е'."
        TimerSec = 8
        Pos = @(48, 144)
        Deps = @("38_myech")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "48_pauk"
        ItemId = "minecraft:spider_eye"
        Ru = "Паýк"
        Phon = "[pa-OOK]"
        Gender = "Masculino (м.р.)"
        Pt = "Aranha / Olho de Aranha"
        Grammar = "Hiato a-ú. A tônica recai com clareza sobre o 'у'."
        TimerSec = 8
        Pos = @(96, 144)
        Deps = @("38_myech")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "49_kriper"
        ItemId = "minecraft:gunpowder"
        Ru = "Крúпер"
        Phon = "[KREE-pyer]"
        Gender = "Masculino (м.р.)"
        Pt = "Creeper / Pólvora"
        Grammar = "Adaptação fonética direta ao cirílico do monstro ícone do Minecraft."
        TimerSec = 8
        Pos = @(144, 144)
        Deps = @("40_luk")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "50_kost"
        ItemId = "minecraft:bone"
        Ru = "Кость"
        Phon = "[Kost']"
        Gender = "Feminino (ж.р.)"
        Pt = "Osso"
        Grammar = "Encontro consonantal 'ст' palatizado pelo sinal brando final. Substantivo feminino."
        TimerSec = 8
        Pos = @(192, 144)
        Deps = @("47_skelet")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "51_nit"
        ItemId = "minecraft:string"
        Ru = "Нить"
        Phon = "[Neet']"
        Gender = "Feminino (ж.р.)"
        Pt = "Linha / Fio"
        Grammar = "Monossílabo com vogal 'и' palatizante e sinal brando final."
        TimerSec = 8
        Pos = @(240, 144)
        Deps = @("48_pauk")
    },
    @{
        Tier = "tier3_journeyman"
        TierNum = 3
        FileId = "52_povodok"
        ItemId = "minecraft:lead"
        Ru = "Поводóк"
        Phon = "[pa-va-DOK]"
        Gender = "Masculino (м.р.)"
        Pt = "Laço"
        Grammar = "Dupla redução vocálica: os dois primeiros 'o' átonos viram [a], restando o último tônico."
        TimerSec = 8
        Pos = @(288, 144)
        Deps = @("51_nit")
    },

    # --- TIER 4: MASTER (16 itens) ---
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "53_obsidian"
        ItemId = "minecraft:obsidian"
        Ru = "Обсидиáн"
        Phon = "[ap-see-dee-AHN]"
        Gender = "Masculino (м.р.)"
        Pt = "Obsidiana"
        Grammar = "Redução do 'о' inicial átono para [a]. Tônica na penúltima sílaba -ДИАН."
        TimerSec = 5
        Pos = @(0, 0)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "54_lava"
        ItemId = "minecraft:lava_bucket"
        Ru = "Лáва"
        Phon = "[LAH-va]"
        Gender = "Feminino (ж.р.)"
        Pt = "Balde de Lava / Lava"
        Grammar = "Paroxítona feminina suave. O primeiro 'а' é aberto e tônico."
        TimerSec = 5
        Pos = @(48, 0)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "55_zelye"
        ItemId = "minecraft:potion"
        Ru = "Зéлье"
        Phon = "[ZYE-lye]"
        Gender = "Neutro (с.р.)"
        Pt = "Poção"
        Grammar = "Substantivo neutro com sinal brando separador (ь) antes da vogal 'е'."
        TimerSec = 5
        Pos = @(96, 0)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "56_varochnaya_stoyka"
        ItemId = "minecraft:brewing_stand"
        Ru = "Вáрочная стóйка"
        Phon = "[VAH-rach-na-ya STOY-ka]"
        Gender = "Feminino (ж.р.)"
        Pt = "Suporte de Poções"
        Grammar = "Expressão composta com concordância de adjetivo feminino terminado em -ая."
        TimerSec = 5
        Pos = @(144, 0)
        Deps = @("55_zelye")
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "57_kniga"
        ItemId = "minecraft:book"
        Ru = "Кнúга"
        Phon = "[KNEE-ga]"
        Gender = "Feminino (ж.р.)"
        Pt = "Livro"
        Grammar = "Substantivo feminino clássico com 'г' sonoro brando antes de -а."
        TimerSec = 5
        Pos = @(0, 48)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "58_stol_zacharovaniya"
        ItemId = "minecraft:enchanting_table"
        Ru = "Стол зачаровáния"
        Phon = "[Stol za-cha-ra-VAH-nee-ya]"
        Gender = "Masculino (м.р.)"
        Pt = "Mesa de Encantamentos"
        Grammar = "Regência do caso genitivo neutro: зачарование torna-se зачарования."
        TimerSec = 5
        Pos = @(48, 48)
        Deps = @("53_obsidian", "57_kniga")
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "59_krasnyy_kamen"
        ItemId = "minecraft:redstone"
        Ru = "Крáсный кáмень"
        Phon = "[KRAHS-nyy KAH-myen']"
        Gender = "Masculino (м.р.)"
        Pt = "Redstone (Pedra Vermelha)"
        Grammar = "Concordância do adjetivo masculino -ый com o substantivo masculino камень."
        TimerSec = 5
        Pos = @(96, 48)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "60_porshen"
        ItemId = "minecraft:piston"
        Ru = "Пóршень"
        Phon = "[POR-shyen']"
        Gender = "Masculino (м.р.)"
        Pt = "Pistão"
        Grammar = "Consoante 'ш' dura seguida de 'e' e sinal brando no 'н'."
        TimerSec = 5
        Pos = @(144, 48)
        Deps = @("59_krasnyy_kamen")
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "61_razdatchik"
        ItemId = "minecraft:dispenser"
        Ru = "Раздáтчик"
        Phon = "[raz-DAHT-cheek]"
        Gender = "Masculino (м.р.)"
        Pt = "Ejetor / Distribuidor"
        Grammar = "Prefixo raz- (indica dispersão/distribuição) + radical do verbo dвать (dar)."
        TimerSec = 5
        Pos = @(192, 48)
        Deps = @("59_krasnyy_kamen")
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "62_oko_endera"
        ItemId = "minecraft:ender_eye"
        Ru = "О́ко Э́ндера"
        Phon = "[O-ka EN-dye-ra]"
        Gender = "Neutro (с.р.)"
        Pt = "Olho do Ender"
        Grammar = "'Око' é o vocábulo poético eslavo arcaico para 'olho'. 'Эндера' está no caso genitivo."
        TimerSec = 5
        Pos = @(0, 96)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "63_nezerit"
        ItemId = "minecraft:netherite_ingot"
        Ru = "Незерúт"
        Phon = "[nye-zye-REET]"
        Gender = "Masculino (м.р.)"
        Pt = "Barra de Netherita"
        Grammar = "Substantivo mineral moderno com acentuação oxítona em -ИТ."
        TimerSec = 5
        Pos = @(48, 96)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "64_sleza_gasta"
        ItemId = "minecraft:ghast_tear"
        Ru = "Слезá гáста"
        Phon = "[slye-ZAH GAH-sta]"
        Gender = "Feminino (ж.р.)"
        Pt = "Lágrima de Ghast"
        Grammar = "'Слеза' (lágrima) é oxítono; 'гаста' é o genitivo do mob masculino Гаст."
        TimerSec = 5
        Pos = @(96, 96)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "65_sterzhen_ifrita"
        ItemId = "minecraft:blaze_rod"
        Ru = "Стержéнь ифрúта"
        Phon = "[styer-ZHEN' eef-REE-ta]"
        Gender = "Masculino (м.р.)"
        Pt = "Vara de Blaze (Ifrit)"
        Grammar = "No Minecraft em russo, o Blaze é oficialmente traduzido como 'Ифрит' (gênio do fogo)."
        TimerSec = 5
        Pos = @(144, 96)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "66_dykhanie_drakona"
        ItemId = "minecraft:dragon_breath"
        Ru = "Дыхáние дракона"
        Phon = "[dy-KHAH-nee-ye dra-KO-na]"
        Gender = "Neutro (с.р.)"
        Pt = "Bafo de Dragão"
        Grammar = "Substantivo neutro abstrato terminado em -ие. 'Дракона' é genitivo de Дракон."
        TimerSec = 5
        Pos = @(192, 96)
        Deps = @("62_oko_endera")
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "67_nakovalnya"
        ItemId = "minecraft:anvil"
        Ru = "Наковáльня"
        Phon = "[na-ka-VAHL'-nya]"
        Gender = "Feminino (ж.р.)"
        Pt = "Bigorna"
        Grammar = "Derivado do verbo ковать (forjar). Prefixo na- e terminação instrumental feminina -льня."
        TimerSec = 5
        Pos = @(0, 144)
        Deps = @()
    },
    @{
        Tier = "tier4_master"
        TierNum = 4
        FileId = "68_puzyryok_opyta"
        ItemId = "minecraft:experience_bottle"
        Ru = "Пузырёк óпыта"
        Phon = "[poo-zy-RYOK O-py-ta]"
        Gender = "Masculino (м.р.)"
        Pt = "Frasco de Experiência"
        Grammar = "A letra 'ё' é SEMPRE tônica em russo (пузы-РЁК). 'Опыта' é genitivo de опыт."
        TimerSec = 5
        Pos = @(48, 144)
        Deps = @("58_stol_zacharovaniya")
    }
)

Write-Host "  Total de itens no curriculo: $($curriculum.Count)" -ForegroundColor Cyan

# Gerar arquivos JSON do Heracles
$tierCounts = @{
    "tier1_novice" = 0
    "tier2_apprentice" = 0
    "tier3_journeyman" = 0
    "tier4_master" = 0
}

foreach ($item in $curriculum) {
    $tierDir = Join-Path $heraclesQuestsDir $item.Tier
    $questJsonPath = Join-Path $tierDir "$($item.FileId).json"

    # Criar estrutura JSON do Heracles
    $questObj = [ordered]@{
        dependencies = $item.Deps
        settings = [ordered]@{
            hidden = $false
            unlockable = $true
        }
        display = [ordered]@{
            title = "Tier $($item.TierNum): $($item.Ru) ($($item.Pt))"
            subtitle = "Pronúncia: $($item.Phon) | $($item.Pt) • $($item.Gender)"
            description = @(
                "<#55FF55>Добро пожаловать в мир русского языка!</>",
                "",
                "Item: <gold>$($item.Ru)</gold> ($($item.Pt))",
                "Gênero Gramatical: <aqua>$($item.Gender)</aqua>",
                "Guia de Pronúncia: <yellow>$($item.Phon)</yellow>",
                "",
                "§eRegra Fonética & Gramática:§r",
                $item.Grammar,
                "",
                "§cDesafio de Voz (Mecânica Melty):§r",
                "Segure o item na mão. Você tem <gold>$($item.TimerSec) segundos</gold> para pronunciar <yellow>«$($item.Ru)»</yellow> no microfone para consolidar o vocabulário e não perder o item!",
                "",
                "§bRecompensa Educacional:§r +25 XP e registro no vocabulário pessoal."
            )
            icon = [ordered]@{
                type = "heracles:item"
                item = $item.ItemId
            }
            groups = [ordered]@{
                $item.Tier = [ordered]@{
                    position = $item.Pos
                }
            }
        }
        tasks = [ordered]@{
            "adquirir_$($item.FileId)" = [ordered]@{
                type = "heracles:item"
                item = $item.ItemId
                amount = 1
                collection = "AUTOMATIC"
                title = "Segure ou colete $($item.Ru) ($($item.Pt))"
            }
        }
        rewards = [ordered]@{
            "xp_recompensa" = [ordered]@{
                type = "heracles:xp"
                xptype = "POINTS"
                amount = 25
            }
        }
    }

    $jsonStr = $questObj | ConvertTo-Json -Depth 10
    [System.IO.File]::WriteAllText($questJsonPath, $jsonStr, $Utf8NoBom)
    $tierCounts[$item.Tier]++
}

Write-Host "  [OK] Tier 1 Novice: $($tierCounts['tier1_novice']) quests JSON criadas." -ForegroundColor Green
Write-Host "  [OK] Tier 2 Apprentice: $($tierCounts['tier2_apprentice']) quests JSON criadas." -ForegroundColor Green
Write-Host "  [OK] Tier 3 Journeyman: $($tierCounts['tier3_journeyman']) quests JSON criadas." -ForegroundColor Green
Write-Host "  [OK] Tier 4 Master: $($tierCounts['tier4_master']) quests JSON criadas." -ForegroundColor Green

# -------------------------------------------------------------------------
# ETAPA 4: Tooltips Dinamicos via KubeJS (Client-Side)
# -------------------------------------------------------------------------
Write-Host "`n[4/5] Gerando script de Tooltips Pedagogicos via KubeJS..." -ForegroundColor Yellow

$kubeJsLines = @()
$kubeJsLines += "// Russian Pedagogical Tooltips Script for Fabric 1.20.1 (KubeJS)"
$kubeJsLines += "// Executed client-side with zero multiplayer server impact."
$kubeJsLines += ""
$kubeJsLines += "ItemEvents.tooltip(event => {"
$kubeJsLines += "    const ruCurriculum = {"

foreach ($item in $curriculum) {
    $cleanRu = $item.Ru -replace "'", "\'"
    $cleanPhon = $item.Phon -replace "'", "\'"
    $cleanPt = $item.Pt -replace "'", "\'"
    $cleanGen = $item.Gender -replace "'", "\'"
    $cleanGrammar = $item.Grammar -replace "'", "\'"
    
    $kubeJsLines += "        '$($item.ItemId)': {"
    $kubeJsLines += "            ru: '$cleanRu',"
    $kubeJsLines += "            phon: '$cleanPhon',"
    $kubeJsLines += "            pt: '$cleanPt',"
    $kubeJsLines += "            gen: '$cleanGen',"
    $kubeJsLines += "            tier: $($item.TierNum),"
    $kubeJsLines += "            note: '$cleanGrammar'"
    $kubeJsLines += "        },"
}

$kubeJsLines += "    };"
$kubeJsLines += ""
$kubeJsLines += "    for (let id in ruCurriculum) {"
$kubeJsLines += "        let e = ruCurriculum[id];"
$kubeJsLines += "        event.addAdvanced(id, (item, advanced, text) => {"
$kubeJsLines += '            text.add(1, Text.of("[RU] " + e.ru).gold().bold(true));'
$kubeJsLines += '            text.add(2, Text.of("[Pronúncia] " + e.phon).yellow());'
$kubeJsLines += '            text.add(3, Text.of("[PT] " + e.pt + " • " + e.gen).aqua());'
$kubeJsLines += '            text.add(4, Text.of("[Nível] Tier " + e.tier + " (Fale em russo!)").darkGreen().italic(true));'
$kubeJsLines += '            if (e.note) {'
$kubeJsLines += '                text.add(5, Text.of("[Regra] " + e.note).gray());'
$kubeJsLines += '            }'
$kubeJsLines += "        });"
$kubeJsLines += "    }"
$kubeJsLines += "});"

$kubeJsScriptContent = $kubeJsLines -join "`r`n"
$kubeJsScriptPath = Join-Path $kubejsClientScriptsDir "russian_tooltips.js"
[System.IO.File]::WriteAllText($kubeJsScriptPath, $kubeJsScriptContent, $Utf8NoBom)
Write-Host "  [OK] kubejs/client_scripts/russian_tooltips.js gerado com 68 entradas." -ForegroundColor Green

# -------------------------------------------------------------------------
# ETAPA 5: Pacote de Recursos Bilingue (Bilingual Resource Pack Fallback)
# -------------------------------------------------------------------------
Write-Host "`n[5/5] Construindo Pacote de Recursos Bilingue (Resource Pack)..." -ForegroundColor Yellow

$packMcmetaContent = @"
{
  "pack": {
    "pack_format": 15,
    "description": "Minecraft Russian Pedagogical Learning Pack (Bilingual Tooltips & Phonetics)"
  }
}
"@
$packMcmetaPath = Join-Path $packDir "pack.mcmeta"
[System.IO.File]::WriteAllText($packMcmetaPath, $packMcmetaContent, $Utf8NoBom)

# Gerar ru_ru.json e pt_br.json bilingues para os 68 itens
$ruRuLang = [ordered]@{}
$ptBrLang = [ordered]@{}

foreach ($item in $curriculum) {
    # Normalizar chave de item/bloco
    $cleanKey = $item.ItemId.Replace("minecraft:", "")
    $langKeyItem = "item.minecraft.$cleanKey"
    $langKeyBlock = "block.minecraft.$cleanKey"

    $ruValue = "$($item.Ru) $($item.Phon) [PT: $($item.Pt)]"
    $ptValue = "$($item.Pt) [RU: $($item.Ru) $($item.Phon)]"

    $ruRuLang[$langKeyItem] = $ruValue
    $ruRuLang[$langKeyBlock] = $ruValue

    $ptBrLang[$langKeyItem] = $ptValue
    $ptBrLang[$langKeyBlock] = $ptValue
}

$ruRuJson = $ruRuLang | ConvertTo-Json -Depth 5
$ptBrJson = $ptBrLang | ConvertTo-Json -Depth 5

$ruRuPath = Join-Path $packDir "assets\minecraft\lang\ru_ru.json"
$ptBrPath = Join-Path $packDir "assets\minecraft\lang\pt_br.json"

[System.IO.File]::WriteAllText($ruRuPath, $ruRuJson, $Utf8NoBom)
[System.IO.File]::WriteAllText($ptBrPath, $ptBrJson, $Utf8NoBom)
Write-Host "  [OK] Russian_Bilingual_Educational_Pack gerado com ru_ru.json e pt_br.json." -ForegroundColor Green

# Atualizar options.txt para ativar o resource pack por padrão se options.txt existir
$optionsPath = Join-Path $instanceDir "options.txt"
if (Test-Path $optionsPath) {
    $optionsContent = [System.IO.File]::ReadAllText($optionsPath, [System.Text.Encoding]::UTF8)
    if ($optionsContent -notmatch "Russian_Bilingual_Educational_Pack") {
        if ($optionsContent -match "resourcePacks:\[(.*?)\]") {
            $existingPacks = $Matches[1]
            if ($existingPacks) {
                $newPacks = 'resourcePacks:[' + $existingPacks + ',"file/Russian_Bilingual_Educational_Pack"]'
            } else {
                $newPacks = 'resourcePacks:["vanilla","file/Russian_Bilingual_Educational_Pack"]'
            }
            $optionsContent = $optionsContent -replace "resourcePacks:\[.*?\]", $newPacks
            [System.IO.File]::WriteAllText($optionsPath, $optionsContent, $Utf8NoBom)
            Write-Host "  [OK] options.txt atualizado para carregar Russian_Bilingual_Educational_Pack." -ForegroundColor Green
        }
    }
}
# Criar versao empacotada .zip do Resource Pack
$zipPackPath = Join-Path $resourcePacksDir "Russian_Bilingual_Educational_Pack.zip"
Compress-Archive -Path (Join-Path $packDir "*") -DestinationPath $zipPackPath -Force
Write-Host "  [OK] Arquivo Russian_Bilingual_Educational_Pack.zip gerado para distribuicao." -ForegroundColor Green

# Sincronizar mods, config, kubejs e resourcepacks com as instancias do Launcher UltimMC
$launcherInstanceDirs = @(
    (Join-Path $BaseDir "Launcher\instances\Minecraft_Russo\.minecraft"),
    (Join-Path $BaseDir "Launcher\data\instances\Minecraft_Russo\.minecraft")
)

foreach ($lDir in $launcherInstanceDirs) {
    if (Test-Path $lDir) {
        robocopy (Join-Path $instanceDir "config") (Join-Path $lDir "config") /E /NFL /NDL /NJH /NJS | Out-Null
        robocopy (Join-Path $instanceDir "kubejs") (Join-Path $lDir "kubejs") /E /NFL /NDL /NJH /NJS | Out-Null
        robocopy (Join-Path $instanceDir "resourcepacks") (Join-Path $lDir "resourcepacks") /E /NFL /NDL /NJH /NJS | Out-Null
        Write-Host "  [OK] Sincronizado com Launcher: $lDir" -ForegroundColor Green
    }
}

Write-Host "`n======================================================================" -ForegroundColor Green
Write-Host " [CONCLUIDO] Setup do Milestone M3 finalizado com 100% de integridade!  " -ForegroundColor Green
Write-Host " - 10 Mods Educacionais e Font Engine em mods\" -ForegroundColor Green
Write-Host " - 68 Quests do Heracles em UTF-8 nos 4 Tiers" -ForegroundColor Green
Write-Host " - Modern UI com DirectWrite / FreeType / Segoe UI" -ForegroundColor Green
Write-Host " - Tooltips Bilingues no KubeJS e Resource Pack" -ForegroundColor Green
Write-Host "======================================================================" -ForegroundColor Green

exit 0
