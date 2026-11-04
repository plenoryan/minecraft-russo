[Net.ServicePointManager]::SecurityProtocol = [Net.SecurityProtocolType]::Tls12
$slugs = @(
    "appleskin",
    "entityculling",
    "inventory-sorter",
    "stack-to-nearby-chests",
    "omnimovement"
)

$results = @()

foreach ($slug in $slugs) {
    $url = "https://api.modrinth.com/v2/project/$slug/version?game_versions=[%221.20.1%22]&loaders=[%22fabric%22]"
    try {
        $versions = Invoke-RestMethod -Uri $url -Headers @{ "User-Agent" = "SayItModSurvey/1.0" }
        if ($versions.Count -gt 0) {
            $latest = $versions[0]
            $file = $latest.files | Where-Object { $_.primary }
            if (-not $file) { $file = $latest.files[0] }
            $results += [PSCustomObject]@{
                Slug = $slug
                Name = $latest.name
                Version = $latest.version_number
                Filename = $file.filename
                DirectUrl = $file.url
                SizeBytes = $file.size
                Dependencies = ($latest.dependencies | ForEach-Object { "$($_.dependency_type): $($_.project_id)" }) -join ", "
            }
        } else {
            $results += [PSCustomObject]@{
                Slug = $slug
                Name = "NO 1.20.1 FABRIC VERSION FOUND"
            }
        }
    } catch {
        $results += [PSCustomObject]@{
            Slug = $slug
            Name = "ERROR: " + $_.Exception.Message
        }
    }
}

$results | Format-Table -AutoSize
$results | ConvertTo-Json -Depth 3
