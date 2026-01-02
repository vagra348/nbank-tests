$IMAGE_NAME = "nbank-tests"
$TEST_PROFILE = $args[0]
if (-not $TEST_PROFILE) { $TEST_PROFILE = "api" }
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmm"
$TEST_OUTPUT_DIR = ".\test-output\$TIMESTAMP"
$HOST_IP = "host.docker.internal"

Write-Host ">>> Build running"
docker build -t $IMAGE_NAME .

Remove-Item -Recurse -Force $TEST_OUTPUT_DIR -ErrorAction SilentlyContinue
New-Item -ItemType Directory -Path "$TEST_OUTPUT_DIR\logs" -Force
New-Item -ItemType Directory -Path "$TEST_OUTPUT_DIR\results" -Force
New-Item -ItemType Directory -Path "$TEST_OUTPUT_DIR\report" -Force

Write-Host ">>> Tests running"
$dockerArgs = @(
    "run",
    "--rm",
    "--add-host=host.docker.internal:host-gateway",
    "-v", "${PWD}\$TEST_OUTPUT_DIR\logs:/app/logs",
    "-v", "${PWD}\$TEST_OUTPUT_DIR\results:/app/target/surefire-reports",
    "-v", "${PWD}\$TEST_OUTPUT_DIR\report:/app/target/site",
    "-e", "TEST_PROFILE=$TEST_PROFILE",
    "-e", "APIBASEURI=http://${HOST_IP}:4111",
    "-e", "UIBASEURL=http://${HOST_IP}:3000",
    $IMAGE_NAME
)
$output = & docker @dockerArgs 2>&1
$output | Out-File -FilePath "$TEST_OUTPUT_DIR\logs\run.log" -Encoding UTF8

Write-Host ">>> Tests finished"
Write-Host "Log-file: $TEST_OUTPUT_DIR\logs\run.log"
Write-Host "Tests results: $TEST_OUTPUT_DIR\results"
Write-Host "Report: $TEST_OUTPUT_DIR\report"