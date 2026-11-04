Add-Type -AssemblyName System.IO.Compression.FileSystem
$target = "class_310.class"
Get-ChildItem -Path "F:\Jogos\Mine" -Filter "*.jar" -Recurse | ForEach-Object {
    $jarPath = $_.FullName
    try {
        $zip = [System.IO.Compression.ZipFile]::OpenRead($jarPath)
        $found = $zip.Entries | Where-Object { $_.Name -like "*class_310*" }
        if ($found) {
            Write-Host "FOUND in $jarPath : $($found.FullName)"
        }
        $zip.Dispose()
    } catch {}
}
