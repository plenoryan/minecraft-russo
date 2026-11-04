[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12

$mods = @(
    @{ slug = "omnimovement"; filename = "omnimovement-fabric-1.0+mc1.20.1.jar" },
    @{ slug = "inventory-sorter"; filename = "InventorySorter-1.9.0-1.20.jar" },
    @{ slug = "stack-to-nearby-chests"; filename = "stack-to-nearby-chests-mc1.20.1-0.5.10.jar" },
    @{ slug = "entityculling"; filename = "entityculling-fabric-1.11.3-mc1.20.1.jar" },
    @{ slug = "krypton"; filename = "krypton-0.2.3.jar" },
    @{ slug = "appleskin"; filename = "appleskin-fabric-mc1.20.1-2.5.2.jar" }
)

$results = @()
foreach ($m in $mods) {
    $slug = $m.slug
    $targetFile = $m.filename
    $url = "https://api.modrinth.com/v2/project/$slug/version?game_versions=%5B%221.20.1%22%5D&loaders=%5B%22fabric%22%5D"
    $versions = Invoke-RestMethod -Uri $url -Headers @{ "User-Agent" = "SayItModSurvey/1.0" }
    $fileObj = $null
    foreach ($ver in $versions) {
        foreach ($f in $ver.files) {
            if ($f.filename -eq $targetFile) {
                $fileObj = $f
                break
            }
        }
        if ($fileObj) { break }
    }
    if (-not $fileObj -and $versions.Count -gt 0) {
        $fileObj = $versions[0].files[0]
    }
    $results += [PSCustomObject]@{
        Slug = $slug
        Filename = $fileObj.filename
        Url = $fileObj.url
        Size = $fileObj.size
        Sha512 = $fileObj.hashes.sha512
        Sha1 = $fileObj.hashes.sha1
    }
}

$results | Format-List
$results | ConvertTo-Json -Depth 4 | Out-File -FilePath "F:\Jogos\Mine\.agents\teamwork\worker_m2\hashes.json" -Encoding utf8
