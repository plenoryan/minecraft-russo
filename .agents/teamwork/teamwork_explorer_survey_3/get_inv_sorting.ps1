$url = "https://api.modrinth.com/v2/project/inventory-sorting/version?game_versions=[%221.20.1%22]&loaders=[%22fabric%22]"
$versions = Invoke-RestMethod -Uri $url -Headers @{ "User-Agent" = "SayItModSurvey/1.0" }
$latest = $versions[0]
[PSCustomObject]@{
    Slug = "inventory-sorting"
    Name = $latest.name
    Version = $latest.version_number
    Filename = $latest.files[0].filename
    DirectUrl = $latest.files[0].url
    SizeBytes = $latest.files[0].size
    Dependencies = ($latest.dependencies | ForEach-Object { "$($_.dependency_type): $($_.project_id)" }) -join ", "
} | Format-List
