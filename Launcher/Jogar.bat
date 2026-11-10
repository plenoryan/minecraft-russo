@echo off
chcp 65001 >nul
title Minecraft Russo - Carregando Jogo...
cls

echo ======================================================================
echo             MINECRAFT RUSSO - PACOTE DE APRENDIZADO PORTATIL
echo ======================================================================
echo.

:: 1. Auto-detectar diretorios base (suporta raiz ou subpasta Launcher)
if exist "%~dp0Launcher\UltimMC.exe" (
    set "BASE_DIR=%~dp0"
    set "LAUNCHER_DIR=%~dp0Launcher\"
) else if exist "%~dp0UltimMC.exe" (
    set "BASE_DIR=%~dp0..\"
    set "LAUNCHER_DIR=%~dp0"
) else (
    set "BASE_DIR=%~dp0"
    set "LAUNCHER_DIR=%~dp0Launcher\"
)

set "LAUNCHER_EXE=%LAUNCHER_DIR%UltimMC.exe"
set "BUNDLED_JAVA=%BASE_DIR%Java17\bin\javaw.exe"
set "DATA_DIR=%LAUNCHER_DIR%data"

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

:: 3. Verificar Java 17
if exist "%BUNDLED_JAVA%" (
    echo [OK] Java 17 portatil detectado com sucesso em:
    echo      "%BUNDLED_JAVA%"
) else (
    where javaw >nul 2>&1
    if errorlevel 1 (
        echo [AVISO] Java 17 portatil nao encontrado na pasta local Java17\.
        echo         Tentando usar o Java instalado no sistema...
        where java >nul 2>&1
        if errorlevel 1 (
            echo.
            echo [ERRO CRITICO] O Java 17 nao esta instalado no seu computador e nao foi
            echo               encontrado na pasta Java17.
            echo.
            echo Instale o Java 17 em: https://adoptium.net/temurin/releases/?version=17
            echo.
            echo Pressione qualquer tecla para sair.
            pause >nul
            exit /b 1
        )
    )
)

:: 4. Auto-configuracao dinamica do caminho do Java portatil no Launcher e instancias
set "JAVA_PATH_NORM=%BUNDLED_JAVA:\=/%"

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$j = $env:JAVA_PATH_NORM; if (-not $j) { $p = '%~dp0'.Replace('''', ''''''); $j = ($p + 'Java17/bin/javaw.exe').Replace('\', '/'); }" ^
    "$d = $env:DATA_DIR; if (-not $d) { $d = '%~dp0'.Replace('''', '''''') + 'Launcher\data'; }" ^
    "$l = $env:LAUNCHER_DIR; if (-not $l) { $l = '%~dp0'.Replace('''', '''''') + 'Launcher\'; }" ^
    "$configs = @(" ^
    "    (Join-Path $d 'ultimmc.cfg')," ^
    "    (Join-Path $l 'ultimmc.cfg')," ^
    "    (Join-Path $d 'instances\Minecraft_Russo\instance.cfg')," ^
    "    (Join-Path $l 'instances\Minecraft_Russo\instance.cfg')" ^
    ");" ^
    "foreach ($cfg in $configs) {" ^
    "    if (Test-Path $cfg) {" ^
    "        $c = Get-Content $cfg -Encoding UTF8;" ^
    "        if ($c -match '^JavaPath=') {" ^
    "            $c = $c -replace '^JavaPath=.*', ('JavaPath=' + $j);" ^
    "        } else {" ^
    "            $c += ('JavaPath=' + $j);" ^
    "        }" ^
    "        [System.IO.File]::WriteAllLines($cfg, $c, [System.Text.UTF8Encoding]::new($false));" ^
    "    }" ^
    "};" ^
    "$accounts = @(" ^
    "    (Join-Path $d 'accounts.json')," ^
    "    (Join-Path $l 'accounts.json')" ^
    ");" ^
    "foreach ($acc in $accounts) {" ^
    "    if (Test-Path $acc) {" ^
    "        $a = Get-Content $acc -Raw -Encoding UTF8;" ^
    "        $a = $a -replace '\"type\":\s*\"Offline\"', '\"type\": \"dummy\"';" ^
    "        [System.IO.File]::WriteAllText($acc, $a, [System.Text.UTF8Encoding]::new($false));" ^
    "    }" ^
    "}" >nul 2>&1

:: 5. Iniciar o Jogo diretamente no Minecraft_Russo
echo.
echo [INICIANDO] Abrindo o inicializador e carregando o modpack Minecraft_Russo...
echo Bom jogo e bons estudos em russo!
echo.

if exist "%DATA_DIR%\instances\Minecraft_Russo" (
    start "" "%LAUNCHER_EXE%" --dir "%DATA_DIR%" -l Minecraft_Russo %*
) else (
    start "" "%LAUNCHER_EXE%" -l Minecraft_Russo %*
)

exit /b 0
