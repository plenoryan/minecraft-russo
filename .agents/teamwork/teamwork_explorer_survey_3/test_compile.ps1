$javac = "F:\Jogos\Mine\Java17\bin\javac.exe"

# Gather all library jars from Launcher/data/libraries and Servidor_Dedicado/libraries
$libJars = Get-ChildItem -Path "F:\Jogos\Mine\Launcher\data\libraries", "F:\Jogos\Mine\Servidor_Dedicado\libraries" -Filter "*.jar" -Recurse | Select-Object -ExpandProperty FullName

$cpList = @(
    "F:\Jogos\Mine\Launcher\data\instances\Minecraft_Russo\.minecraft\.fabric\remappedJars\minecraft-1.20.1-0.16.14\client-intermediary.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\sayitenglish-fabric-1.20.1-1.1.0.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\Jade-1.20-Fabric-11.13.3.jar",
    "F:\Jogos\Mine\Instancia_Cliente\mods\fabric-api-0.92.12+1.20.1.jar"
) + $libJars

$cp = ($cpList | Select-Object -Unique) -join ";"

$outDir = Join-Path $env:TEMP "sayit_compile_verify"
if (Test-Path $outDir) { Remove-Item -Recurse -Force $outDir }
New-Item -ItemType Directory -Force -Path $outDir | Out-Null

$srcFiles = Get-ChildItem -Path "F:\Jogos\Mine\src" -Filter "*.java" -Recurse | Select-Object -ExpandProperty FullName

Write-Host "Compiling $($srcFiles.Count) Java files..."
& $javac -encoding UTF-8 -cp $cp -d $outDir $srcFiles
if ($LASTEXITCODE -eq 0) {
    Write-Host "SUCCESS! 100% Clean Compilation!"
    $classes = Get-ChildItem -Path $outDir -Filter "*.class" -Recurse
    Write-Host "Total compiled classes: $($classes.Count)"
} else {
    Write-Host "FAILED with exit code $LASTEXITCODE"
}
