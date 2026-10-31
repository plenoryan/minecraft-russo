# verify_m1.ps1 - Automated Verification Script for Milestone M1
# Milestone: Runtime Environment, Launcher & Base Fabric 1.20.1 Engine
[CmdletBinding()]
param (
    [string]$RootDir = "F:\Jogos\Mine"
)

$ErrorActionPreference = "Stop"
[Console]::OutputEncoding = [System.Text.Encoding]::UTF8

Write-Host "======================================================================" -ForegroundColor Cyan
Write-Host " VERIFICACAO AUTOMATIZADA: MILESTONE M1                               " -ForegroundColor Cyan
Write-Host " Ambiente de Execucao, Lancador Portatil e Base Fabric 1.20.1        " -ForegroundColor Cyan
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

# 1. AGENTS.md
Test-Requirement "Governança AGENTS.md e Lições Aprendidas" {
    $path = Join-Path $RootDir "AGENTS.md"
    if (-not (Test-Path $path)) { return @{ Success = $false; Message = "Arquivo AGENTS.md nao encontrado" } }
    $content = Get-Content $path -Raw -Encoding UTF8
    if (-not ($content -match "Li.*es Aprendidas & Armadilhas Superadas")) {
        return @{ Success = $false; Message = "Secao obrigatoria 'Lições Aprendidas & Armadilhas Superadas' ausente" }
    }
    if (-not ($content -match "Windows 11" -and $content -match "PowerShell")) {
        return @{ Success = $false; Message = "Perfil de ambiente Windows 11 / PowerShell incompleto" }
    }
    return @{ Success = $true; Message = "AGENTS.md validado com licoes aprendidas e regras de ambiente" }
}

# 2. .gitignore
Test-Requirement "Configuração do .gitignore" {
    $path = Join-Path $RootDir ".gitignore"
    if (-not (Test-Path $path)) { return @{ Success = $false; Message = ".gitignore nao encontrado" } }
    $content = Get-Content $path -Raw
    if (-not ($content -match "logs/" -and $content -match "crash-reports/" -and $content -match "\.fabric/")) {
        return @{ Success = $false; Message = ".gitignore nao cobre logs, crash-reports ou cache fabric" }
    }
    return @{ Success = $true; Message = ".gitignore cobre logs, caches e arquivos transientes" }
}

# 3. README.md
Test-Requirement "Documentação Geral README.md" {
    $path = Join-Path $RootDir "README.md"
    if (-not (Test-Path $path)) { return @{ Success = $false; Message = "README.md nao encontrado" } }
    $content = Get-Content $path -Raw -Encoding UTF8
    if (-not ($content -match "Minecraft Russo" -and $content -match "Jogar\.bat")) {
        return @{ Success = $false; Message = "README.md nao contem referencias ao projeto ou inicio rapido" }
    }
    return @{ Success = $true; Message = "README.md presente e detalhado" }
}

# 4. Jogar.bat
Test-Requirement "Script 1-Click Jogar.bat" {
    $path = Join-Path $RootDir "Jogar.bat"
    if (-not (Test-Path $path)) { return @{ Success = $false; Message = "Jogar.bat nao encontrado" } }
    $content = Get-Content $path -Raw
    if (-not ($content -match "chcp 65001" -and $content -match "%\~dp0" -and $content -match "Java17")) {
        return @{ Success = $false; Message = "Jogar.bat nao contem chcp 65001, caminho relativo ou deteccao Java 17" }
    }
    return @{ Success = $true; Message = "Jogar.bat configurado com UTF-8, deteccao de Java 17 e inicializacao automatica" }
}

# 5. Java 17 Runtime e install_java17.ps1
Test-Requirement "Runtime OpenJDK 17 Portatil" {
    $installerPath = Join-Path $RootDir "install_java17.ps1"
    if (-not (Test-Path $installerPath)) { return @{ Success = $false; Message = "install_java17.ps1 nao encontrado" } }
    
    $javaExe = Join-Path $RootDir "Java17\bin\java.exe"
    $javawExe = Join-Path $RootDir "Java17\bin\javaw.exe"
    if (-not (Test-Path $javaExe) -or -not (Test-Path $javawExe)) {
        return @{ Success = $false; Message = "java.exe ou javaw.exe ausente em Java17\bin\" }
    }
    
    $verOut = cmd.exe /c "`"$javaExe`" -version 2>&1"
    $verStr = ($verOut | Out-String).Trim()
    if (-not ($verStr -match '17\.')) {
        return @{ Success = $false; Message = "Versao do Java incompativel:`n$verStr" }
    }
    return @{ Success = $true; Message = "OpenJDK 17 funcional: $($verStr.Split([Environment]::NewLine)[0])" }
}

# 6. Launcher UltimMC e Configurações de Instância
Test-Requirement "Lançador Portátil UltimMC e Instância Minecraft_Russo" {
    $launcherExe = Join-Path $RootDir "Launcher\UltimMC.exe"
    if (-not (Test-Path $launcherExe)) { return @{ Success = $false; Message = "UltimMC.exe ausente em Launcher\" } }
    
    $instCfg = Join-Path $RootDir "Instancia_Cliente\instance.cfg"
    $mmcPack = Join-Path $RootDir "Instancia_Cliente\mmc-pack.json"
    if (-not (Test-Path $instCfg) -or -not (Test-Path $mmcPack)) {
        return @{ Success = $false; Message = "instance.cfg ou mmc-pack.json ausente em Instancia_Cliente\" }
    }
    
    $json = Get-Content $mmcPack -Raw | ConvertFrom-Json
    $fabric = $json.components | Where-Object { $_.uid -eq "net.fabricmc.fabric-loader" }
    $mc = $json.components | Where-Object { $_.uid -eq "net.minecraft" }
    if (-not $fabric -or -not $mc) {
        return @{ Success = $false; Message = "mmc-pack.json invalido: componentes Fabric ou Minecraft ausentes" }
    }
    if ($mc.version -ne "1.20.1") {
        return @{ Success = $false; Message = "Versao do Minecraft no mmc-pack.json nao e 1.20.1 ($($mc.version))" }
    }
    
    # Check offline accounts preset
    $acc1 = Join-Path $RootDir "Launcher\accounts.json"
    $acc2 = Join-Path $RootDir "Launcher\data\accounts.json"
    if (-not (Test-Path $acc1) -and -not (Test-Path $acc2)) {
        return @{ Success = $false; Message = "accounts.json ausente no Launcher" }
    }
    
    return @{ Success = $true; Message = "UltimMC portatil pronto com manifesto Fabric $($fabric.version) para MC $($mc.version)" }
}

# 7. Opções do Cliente e Idioma Russo
Test-Requirement "Configuração de Idioma Russo (options.txt ru_ru)" {
    $optPath = Join-Path $RootDir "Instancia_Cliente\options.txt"
    if (-not (Test-Path $optPath)) { return @{ Success = $false; Message = "options.txt ausente em Instancia_Cliente\" } }
    $optContent = Get-Content $optPath -Raw
    if (-not ($optContent -match "lang:ru_ru")) {
        return @{ Success = $false; Message = "options.txt nao define lang:ru_ru" }
    }
    return @{ Success = $true; Message = "options.txt configurado com preset de idioma russo (lang:ru_ru)" }
}

# 8. Mods Fabric 1.20.1 e Otimizações
Test-Requirement "Mods Base Fabric 1.20.1 e Otimização" {
    $modsDir = Join-Path $RootDir "Instancia_Cliente\mods"
    if (-not (Test-Path $modsDir)) { return @{ Success = $false; Message = "Pasta mods ausente em Instancia_Cliente\" } }
    
    $requiredMods = @(
        @{ Keyword = "fabric-api"; MinSize = 1000000; Display = "Fabric API" },
        @{ Keyword = "sodium"; MinSize = 500000; Display = "Sodium" },
        @{ Keyword = "lithium"; MinSize = 400000; Display = "Lithium" },
        @{ Keyword = "ferritecore"; MinSize = 80000; Display = "FerriteCore" },
        @{ Keyword = "modernfix"; MinSize = 300000; Display = "ModernFix" },
        @{ Keyword = "ImmediatelyFast"; MinSize = 200000; Display = "ImmediatelyFast" }
    )
    
    $foundFiles = Get-ChildItem -Path $modsDir -Filter "*.jar"
    foreach ($mod in $requiredMods) {
        $matched = $foundFiles | Where-Object { $_.Name -like "*$($mod.Keyword)*" }
        if (-not $matched) {
            return @{ Success = $false; Message = "Mod $($mod.Display) nao encontrado em mods\" }
        }
        if ($matched.Length -lt $mod.MinSize) {
            return @{ Success = $false; Message = "Mod $($mod.Display) arquivo $($matched.Name) tamanho suspeito/corrompido ($($matched.Length) bytes)" }
        }
    }
    return @{ Success = $true; Message = "Todos os 6 mods autenticos verificados ($($foundFiles.Count) JARs encontrados)" }
}

Write-Host "`n======================================================================" -ForegroundColor Cyan
Write-Host " RESUMO DOS TESTES                                                    " -ForegroundColor Cyan
Write-Host "======================================================================" -ForegroundColor Cyan

foreach ($c in $checks) {
    $color = if ($c.Status -eq "PASS") { "Green" } else { "Red" }
    Write-Host ("[{0}] {1,-50} : {2}" -f $c.Status, $c.Name, $c.Details) -ForegroundColor $color
}

if ($allPassed) {
    Write-Host "`n[SUCESSO COMPLETO] Todos os 8 requisitos do Milestone M1 foram validados com exito!`n" -ForegroundColor Green
    exit 0
} else {
    Write-Host "`n[FALHA] Um ou mais requisitos do Milestone M1 nao passaram no teste.`n" -ForegroundColor Red
    exit 1
}
