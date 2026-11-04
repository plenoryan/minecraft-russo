$jarPath = "F:\Jogos\Mine\Launcher\data\libraries\com\mojang\minecraft\1.20.1\minecraft-1.20.1-client.jar"
$zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
$count = $zip.Entries.Count
Write-Host "Total entries: $count"
$sample = $zip.Entries | Select-Object -First 20 | ForEach-Object { $_.FullName }
$sample | ForEach-Object { Write-Host $_ }
$zip.Dispose()
