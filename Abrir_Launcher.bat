@echo off
chcp 65001 >nul
title UltimMC Launcher - Modpack Russo
set "SCRIPT_DIR=%~dp0"
set "BASE_DIR=%~dp0"
set "LAUNCHER_EXE=%BASE_DIR%Launcher\UltimMC.exe"

:: Auto-cura de juncoes e caminhos
if not exist "%BASE_DIR%Launcher\Java17" (
    mklink /J "%BASE_DIR%Launcher\Java17" "%BASE_DIR%Java17" >nul 2>&1
)
if not exist "%BASE_DIR%Launcher\data\Java17" (
    mklink /J "%BASE_DIR%Launcher\data\Java17" "%BASE_DIR%Java17" >nul 2>&1
)

powershell -NoProfile -ExecutionPolicy Bypass -Command ^
    "$b = $env:BASE_DIR; if (-not $b) { $b = $env:SCRIPT_DIR; };" ^
    "$j = ($b + 'Java17\bin\javaw.exe').Replace('\', '/');" ^
    "@((Join-Path $b 'Launcher\data\ultimmc.cfg'), (Join-Path $b 'Launcher\ultimmc.cfg')) | ForEach-Object { if (Test-Path -LiteralPath $_) { (Get-Content -LiteralPath $_) -replace '^JavaPath=.*', ('JavaPath=' + $j) | Set-Content -LiteralPath $_ } }" >nul 2>&1

start "" "%LAUNCHER_EXE%" --dir "%BASE_DIR%Launcher\data"
exit /b 0
