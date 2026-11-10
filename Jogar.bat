@echo off
chcp 65001 >nul
title Minecraft Russo - Carregando Jogo...
cls

echo ======================================================================
echo             MINECRAFT RUSSO - PACOTE DE APRENDIZADO
echo ======================================================================
echo.

:: 1. Definir diretorio base
set "SCRIPT_DIR=%~dp0"
set "BASE_DIR=%~dp0"
set "LAUNCHER_EXE=%BASE_DIR%Launcher\UltimMC.exe"
set "BUNDLED_JAVA=%BASE_DIR%Java17\bin\javaw.exe"

:: 2. Verificar presenca do Launcher
if not exist "%LAUNCHER_EXE%" (
    echo [ERRO] O executavel do inicializador nao foi encontrado em:
    echo        "%LAUNCHER_EXE%"
    echo.
    echo Certifique-se de extrair o pacote completo com a pasta Launcher.
    echo Pressione qualquer tecla para sair.
    pause >nul
    exit /b 1
)

:: 3. Verificar Java 17 (FIXED: Usando if errorlevel 1 para avaliacao correta em tempo de execucao)
if exist "%BUNDLED_JAVA%" (
    echo [OK] Java 17 portatil detectado com sucesso em Java17\bin\javaw.exe.
) else (
    where javaw >nul 2>&1
    if errorlevel 1 (
        echo [AVISO] Java 17 portatil nao encontrado na pasta local Java17\.
        echo         Tentando usar o Java instalado no sistema...
        where java >nul 2>&1
        if errorlevel 1 (
            echo.
            echo [ERRO CRITICO] O Java nao esta instalado no seu computador e nao foi
            echo               encontrado na pasta Java17.
            echo.
            echo Execute install_java17.ps1 para baixar o Java 17 portatil ou instale:
            echo https://adoptium.net/temurin/releases/?version=17
            echo.
            echo Pressione qualquer tecla para sair.
            pause >nul
            exit /b 1
        )
    )
)

:: 4. Auto-cura de juncoes NTFS portateis e sincronizacao de Java
set "INST_DOT_MC=%BASE_DIR%Launcher\data\instances\Minecraft_Russo\.minecraft"
if exist "%INST_DOT_MC%" (
    if not exist "%INST_DOT_MC%\mods" (
        mklink /J "%INST_DOT_MC%\mods" "%BASE_DIR%Instancia_Cliente\mods" >nul 2>&1
    )
    if not exist "%INST_DOT_MC%\config" (
        mklink /J "%INST_DOT_MC%\config" "%BASE_DIR%Instancia_Cliente\config" >nul 2>&1
    )
    if not exist "%INST_DOT_MC%\CustomSkinLoader" (
        mklink /J "%INST_DOT_MC%\CustomSkinLoader" "%BASE_DIR%Instancia_Cliente\CustomSkinLoader" >nul 2>&1
    )
    if not exist "%INST_DOT_MC%\resourcepacks" (
        mklink /J "%INST_DOT_MC%\resourcepacks" "%BASE_DIR%Instancia_Cliente\resourcepacks" >nul 2>&1
    )
    if not exist "%INST_DOT_MC%\shaderpacks" (
        mklink /J "%INST_DOT_MC%\shaderpacks" "%BASE_DIR%Instancia_Cliente\shaderpacks" >nul 2>&1
    )
    if not exist "%INST_DOT_MC%\kubejs" (
        mklink /J "%INST_DOT_MC%\kubejs" "%BASE_DIR%Instancia_Cliente\kubejs" >nul 2>&1
    )
)

if not exist "%BASE_DIR%Launcher\Java17" (
    mklink /J "%BASE_DIR%Launcher\Java17" "%BASE_DIR%Java17" >nul 2>&1
)
if not exist "%BASE_DIR%Launcher\data\Java17" (
    mklink /J "%BASE_DIR%Launcher\data\Java17" "%BASE_DIR%Java17" >nul 2>&1
)

:: Auto-configuracao dinamica do caminho do Java portatil no Launcher e contas offline
set "JAVA_PATH_NORM=%BUNDLED_JAVA:\=/%"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$b = $env:BASE_DIR; if (-not $b) { $b = $env:SCRIPT_DIR; };" ^
    "$j = $env:JAVA_PATH_NORM; if (-not $j) { $j = ($b + 'Java17\bin\javaw.exe').Replace('\', '/'); };" ^
    "$configs = @(" ^
    "    (Join-Path $b 'Launcher\data\ultimmc.cfg')," ^
    "    (Join-Path $b 'Launcher\ultimmc.cfg')," ^
    "    (Join-Path $b 'Launcher\data\instances\Minecraft_Russo\instance.cfg')," ^
    "    (Join-Path $b 'Launcher\instances\Minecraft_Russo\instance.cfg')," ^
    "    (Join-Path $b 'Instancia_Cliente\instance.cfg')" ^
    ");" ^
    "foreach ($cfg in $configs) {" ^
    "    if (Test-Path -LiteralPath $cfg) {" ^
    "        $c = Get-Content -LiteralPath $cfg -Encoding UTF8;" ^
    "        if ($c -match '^JavaPath=') {" ^
    "            $c = $c -replace '^JavaPath=.*', ('JavaPath=' + $j);" ^
    "        } else {" ^
    "            $c += ('JavaPath=' + $j);" ^
    "        }" ^
    "        [System.IO.File]::WriteAllLines($cfg, $c, [System.Text.UTF8Encoding]::new($false));" ^
    "    }" ^
    "};" ^
    "$accounts = @(" ^
    "    (Join-Path $b 'Launcher\data\accounts.json')," ^
    "    (Join-Path $b 'Launcher\accounts.json')" ^
    ");" ^
    "foreach ($acc in $accounts) {" ^
    "    if (Test-Path -LiteralPath $acc) {" ^
    "        $a = Get-Content -LiteralPath $acc -Raw -Encoding UTF8;" ^
    "        $a = $a -replace '\"type\":\s*\"Offline\"', '\"type\": \"dummy\"';" ^
    "        [System.IO.File]::WriteAllText($acc, $a, [System.Text.UTF8Encoding]::new($false));" ^
    "    }" ^
    "}" >nul 2>&1

:: 5. Iniciar o Jogo
echo.
echo [INICIANDO] Abrindo o inicializador e carregando o modpack Minecraft_Russo...
echo Bom jogo e bons estudos em russo!
echo.

if exist "%BASE_DIR%Launcher\data\instances\Minecraft_Russo" (
    start "" "%LAUNCHER_EXE%" --dir "%BASE_DIR%Launcher\data" --launch "Minecraft_Russo"
) else (
    start "" "%LAUNCHER_EXE%" --launch "Minecraft_Russo"
)

exit /b 0
