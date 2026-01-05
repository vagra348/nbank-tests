<# Предварительно должен быть установлен jq:
winget install jqlang.jq
#>
cd $PSScriptRoot
Write-Host "Current directory: $(Get-Location)"

Write-Host ">>> Stop Docker Compose"
docker compose down

Write-Host ">>> Docker pull all browsers"
$json_file = "./config/browsers.json"
if (-not (Test-Path $json_file)) {
    Write-Host ">>> File not found: $json_file"
    exit 1
}
$images = jq -r '.. | objects | select(.image) | .image' $json_file
foreach ($image in $images) {
    Write-Host ">>> Pulling $image..."
    docker pull $image
}

Write-Host ">>> Start Docker Compose"
docker compose up