@echo off
chcp 65001 >nul
title UltimMC Launcher - Modpack Russo
set "SCRIPT_DIR=%~dp0"
set "BASE_DIR=%~dp0"
set "LAUNCHER_EXE=%BASE_DIR%Launcher\UltimMC.exe"

:: Auto-configuracao dinamica do caminho do Java portatil no ultimmc.cfg e instance.cfg
if exist "%BASE_DIR%Launcher\setup_java.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%BASE_DIR%Launcher\setup_java.ps1" -BaseDir "%BASE_DIR%"
) else if exist "%SCRIPT_DIR%Launcher\setup_java.ps1" (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%SCRIPT_DIR%Launcher\setup_java.ps1" -BaseDir "%SCRIPT_DIR%"
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -Command "& { param($b) $j=($b+'Java17\bin\javaw.exe').Replace('\','/'); @((Join-Path $b 'Launcher\data\ultimmc.cfg'),(Join-Path $b 'Launcher\ultimmc.cfg'),(Join-Path $b 'Launcher\data\instances\Minecraft_Russo\instance.cfg'),(Join-Path $b 'Launcher\instances\Minecraft_Russo\instance.cfg')) | ForEach-Object { if(Test-Path -LiteralPath $_){ $c=[System.Collections.Generic.List[string]](Get-Content -LiteralPath $_ -Encoding UTF8); $h=$false; for($i=0;$i -lt $c.Count;$i++){ if($c[$i] -match '^JavaPath='){$c[$i]='JavaPath='+$j; $h=$true;} elseif($c[$i] -match '^AnalyticsSeen='){$c[$i]='AnalyticsSeen=2';} }; if(-not $h){$c.Add('JavaPath='+$j);}; if($_ -like '*ultimmc.cfg' -and ($c -notmatch '^AnalyticsSeen=')){$c.Add('AnalyticsSeen=2');}; [System.IO.File]::WriteAllLines($_,$c,[System.Text.UTF8Encoding]::new($false)) } } }" "%BASE_DIR%"
)

start "" "%LAUNCHER_EXE%" --dir "%BASE_DIR%Launcher\data"
exit /b 0
