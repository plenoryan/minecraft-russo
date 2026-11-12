@echo off
chcp 65001 >nul
title Minecraft Russo - Carregando Jogo...
cls

echo ======================================================================
echo             MINECRAFT RUSSO - PACOTE DE APRENDIZADO PORTATIL
echo ======================================================================
echo.

:: 1. Auto-detectar diretorios base (suporta raiz ou subpasta Launcher)
set "SCRIPT_DIR=%~dp0"
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

:: 1.1 Suporte a argumento rapido de troca de nome (--nome ou /nome)
if /I "%~1"=="--nome" (
    if exist "%~dp0Mudar_Nome.bat" (
        call "%~dp0Mudar_Nome.bat" %~2
    ) else if exist "%~dp0..\Mudar_Nome.bat" (
        call "%~dp0..\Mudar_Nome.bat" %~2
    )
    shift
    shift
)
if /I "%~1"=="/nome" (
    if exist "%~dp0Mudar_Nome.bat" (
        call "%~dp0Mudar_Nome.bat" %~2
    ) else if exist "%~dp0..\Mudar_Nome.bat" (
        call "%~dp0..\Mudar_Nome.bat" %~2
    )
    shift
    shift
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

:: 4. Auto-configuracao dinamica do caminho do Java portatil no ultimmc.cfg e instance.cfg
set "JAVA_PATH_NORM=%BUNDLED_JAVA:\=/%"

if exist "%BASE_DIR%Launcher\setup_java.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%BASE_DIR%Launcher\setup_java.ps1" -BaseDir "%BASE_DIR%"
) else if exist "%LAUNCHER_DIR%setup_java.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%LAUNCHER_DIR%setup_java.ps1" -BaseDir "%BASE_DIR%"
) else if exist "%~dp0setup_java.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0setup_java.ps1" -BaseDir "%BASE_DIR%"
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "& { param($b) $j=($b+'Java17\bin\javaw.exe').Replace('\','/'); @((Join-Path $b 'Launcher\data\ultimmc.cfg'),(Join-Path $b 'Launcher\ultimmc.cfg'),(Join-Path $b 'Launcher\data\instances\Minecraft_Russo\instance.cfg'),(Join-Path $b 'Launcher\instances\Minecraft_Russo\instance.cfg')) | ForEach-Object { if(Test-Path -LiteralPath $_){ $c=[System.Collections.Generic.List[string]](Get-Content -LiteralPath $_ -Encoding UTF8); $h=$false; for($i=0;$i -lt $c.Count;$i++){ if($c[$i] -match '^JavaPath='){$c[$i]='JavaPath='+$j; $h=$true;} elseif($c[$i] -match '^AnalyticsSeen='){$c[$i]='AnalyticsSeen=2';} }; if(-not $h){$c.Add('JavaPath='+$j);}; if($_ -like '*ultimmc.cfg' -and ($c -notmatch '^AnalyticsSeen=')){$c.Add('AnalyticsSeen=2');}; [System.IO.File]::WriteAllLines($_,$c,[System.Text.UTF8Encoding]::new($false)) } } }" "%BASE_DIR%"
)

:: 5. Exibir informacoes do jogador e iniciar o Jogo
echo.
powershell -NoProfile -ExecutionPolicy Bypass -Command "& { param($d) $acc=Join-Path $d 'accounts.json'; if(Test-Path -LiteralPath $acc){ try{ $j=Get-Content -LiteralPath $acc -Raw -Encoding UTF8 | ConvertFrom-Json; $u=$j.accounts[0].ygg.extra.userName; Write-Host ('[JOGADOR] Perfil ativo: ' + $u) -ForegroundColor Yellow; Write-Host '          (Para mudar seu nome, feche o jogo e execute Mudar_Nome.bat)' -ForegroundColor DarkGray; }catch{} } }" "%DATA_DIR%"

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
