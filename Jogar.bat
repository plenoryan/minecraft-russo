@echo off
chcp 65001 >nul
title Minecraft Russo - Carregando Jogo...
cls

echo ======================================================================
echo             MINECRAFT RUSSO - PACOTE DE APRENDIZADO
echo ======================================================================
echo.

:: 1. Definir diretorio base
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
)

if not exist "%BASE_DIR%Launcher\Java17" (
    mklink /J "%BASE_DIR%Launcher\Java17" "%BASE_DIR%Java17" >nul 2>&1
)
if not exist "%BASE_DIR%Launcher\data\Java17" (
    mklink /J "%BASE_DIR%Launcher\data\Java17" "%BASE_DIR%Java17" >nul 2>&1
)

:: Auto-configuracao dinamica do caminho do Java portatil no Launcher e contas offline
powershell -NoProfile -ExecutionPolicy Bypass -Command "$j = ('%BASE_DIR%Java17/bin/javaw.exe' -replace '\\','/'); @('%BASE_DIR%Launcher\data\ultimmc.cfg', '%BASE_DIR%Launcher\ultimmc.cfg', '%BASE_DIR%Launcher\data\instances\Minecraft_Russo\instance.cfg', '%BASE_DIR%Launcher\instances\Minecraft_Russo\instance.cfg', '%BASE_DIR%Instancia_Cliente\instance.cfg') | ForEach-Object { if (Test-Path $_) { (Get-Content $_) -replace '^JavaPath=.*', ('JavaPath=' + $j) | Set-Content $_ } }; @('%BASE_DIR%Launcher\data\accounts.json', '%BASE_DIR%Launcher\accounts.json') | ForEach-Object { if (Test-Path $_) { (Get-Content $_) -replace '\"type\":\s*\"Offline\"', '\"type\": \"dummy\"' | Set-Content $_ } }" >nul 2>&1

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
