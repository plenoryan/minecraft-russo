$url = "https://api.modrinth.com/v2/search?query=inventory%20sorting&facets=[[%22versions:1.20.1%22],[%22categories:fabric%22]]"
$res = Invoke-RestMethod -Uri $url -Headers @{ "User-Agent" = "SayItModSurvey/1.0" }
$res.hits | Select-Object -First 10 | ForEach-Object {
    [PSCustomObject]@{
        Slug = $_.slug
        Title = $_.title
        Author = $_.author
        Downloads = $_.downloads
        Description = $_.description
    }
} | Format-Table -AutoSize
