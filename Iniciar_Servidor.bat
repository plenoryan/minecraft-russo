@echo off
chcp 65001 >nul
title Servidor Dedicado - Minecraft Russo (Amigos)
cls

echo ======================================================================
echo           SERVIDOR DEDICADO - MINECRAFT RUSSO (OFFLINE/AMIGOS)
echo ======================================================================
echo.

set "BASE_DIR=%~dp0"
set "SERVER_DIR=%BASE_DIR%Servidor_Dedicado"
set "BUNDLED_JAVA=%BASE_DIR%Java17\bin\java.exe"

cd /d "%SERVER_DIR%"

:: 1. Selecionar Java Executable
if exist "%BUNDLED_JAVA%" (
    set "JAVA_CMD=%BUNDLED_JAVA%"
    echo [OK] Usando Java 17 embutido em: "%BUNDLED_JAVA%"
) else (
    set "JAVA_CMD=java"
    echo [OK] Usando comando Java do sistema.
)

:: 2. Validar EULA
if exist "eula.txt" (
    findstr /i "eula=true" eula.txt >nul
    if %errorlevel% neq 0 (
        echo eula=true > eula.txt
        echo [INFO] EULA aceito automaticamente.
    )
) else (
    echo eula=true > eula.txt
    echo [INFO] eula.txt criado com sucesso.
)

:: 3. Validar Configuracoes Offline no server.properties
if exist "server.properties" (
    findstr /i "online-mode=false" server.properties >nul
    if %errorlevel% neq 0 (
        echo [AVISO] Corrigindo server.properties para online-mode=false...
        powershell -NoProfile -Command "(Get-Content server.properties) -replace 'online-mode=true', 'online-mode=false' | Set-Content server.properties"
    )
    findstr /i "enforce-secure-profile=false" server.properties >nul
    if %errorlevel% neq 0 (
        echo [AVISO] Desativando enforce-secure-profile para contas offline...
        powershell -NoProfile -Command "(Get-Content server.properties) -replace 'enforce-secure-profile=true', 'enforce-secure-profile=false' | Set-Content server.properties"
    )
)

:: 4. Executar Servidor Fabric com Flags G1GC Aikar
echo.
echo [INICIANDO SERVIDOR] Alocando 2GB a 4GB de RAM...
echo Pressione Ctrl+C a qualquer momento para desligar o servidor com seguranca.
echo.

"%JAVA_CMD%" -Xms2G -Xmx4G -XX:+UseG1GC -XX:+ParallelRefProcEnabled -XX:MaxGCPauseMillis=200 -XX:+UnlockExperimentalVMOptions -XX:+DisableExplicitGC -XX:+AlwaysPreTouch -XX:G1NewSizePercent=30 -XX:G1MaxNewSizePercent=40 -XX:G1ReservePercent=20 -XX:G1HeapWastePercent=5 -XX:G1MixedGCCountTarget=4 -XX:InitiatingHeapOccupancyPercent=15 -XX:G1MixedGCLiveThresholdPercent=90 -XX:G1RSetUpdatingPauseTimePercent=5 -XX:SurvivorRatio=32 -XX:+PerfDisableSharedMem -XX:MaxTenuringThreshold=1 -jar fabric-server-launch.jar nogui

echo.
echo ======================================================================
echo O servidor foi encerrado.
echo ======================================================================
pause