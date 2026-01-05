<# Команда для пуша с использованием учётных данных и тэга образа
$env:DOCKERHUB_USERNAME="<docker-username>";`
$env:DOCKERHUB_TOKEN="<dckr_pat_...>";`
./push-tests.ps1 -Tag "<image-tag>"
#>

param(
    [string]$Tag = "latest"
)

$IMAGE_NAME = "nbank-tests"
$DOCKERHUB_USERNAME = $env:DOCKERHUB_USERNAME
$DOCKERHUB_TOKEN = $env:DOCKERHUB_TOKEN

Write-Host ">>> Login to Docker Hub with token"
echo $DOCKERHUB_TOKEN | docker login --username $DOCKERHUB_USERNAME --password-stdin

Write-Host ">>> Tagging Image"
Write-Host $Tag
docker tag $IMAGE_NAME $DOCKERHUB_USERNAME/$IMAGE_NAME`:$Tag

Write-Host ">>> Sending Image to Docker Hub"
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME`:$Tag

Write-Host ">>> Finished! Image is available as: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME`:$Tag" -ForegroundColor Green