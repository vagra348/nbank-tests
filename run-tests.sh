#!/bin/bash

IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

# Динамическое определение IP хоста, спасибо гпт, но у меня нет технической возможности проверить,
# работает ли на других системах, поэтому пока оставлю так
detect_host_ip() {
    # Проверяем, в WSL ли мы
    if grep -q -i microsoft /proc/version 2>/dev/null; then
        # WSL2: получаем IP шлюза
        echo "$(ip route | grep default | awk '{print $3}')"
    elif [[ "$OSTYPE" == "darwin"* ]]; then
        # macOS
        echo "host.docker.internal"
    elif [[ "$OSTYPE" == "msys" || "$OSTYPE" == "cygwin" ]]; then
        # Windows Git Bash/Cygwin
        echo "host.docker.internal"
    else
        # Linux (не WSL)
        # Пытаемся получить gateway сети docker0
        local gateway=$(docker network inspect bridge -f '{{range .IPAM.Config}}{{.Gateway}}{{end}}' 2>/dev/null)
        if [ -n "$gateway" ]; then
            echo "$gateway"
        else
            echo "172.17.0.1"  # fallback
        fi
    fi
}

HOST_IP=$(detect_host_ip)
echo ">>> Detected host IP: $HOST_IP"

echo ">>> Build running"
docker build -t $IMAGE_NAME .

mkdir -p "$TEST_OUTPUT_DIR/logs"
mkdir -p "$TEST_OUTPUT_DIR/results"
mkdir -p "$TEST_OUTPUT_DIR/report"

echo ">>> Tests running"
docker run --rm \
  -v "$TEST_OUTPUT_DIR/logs":/app/logs \
  -v "$TEST_OUTPUT_DIR/results":/app/target/surefire-reports \
  -v "$TEST_OUTPUT_DIR/report":/app/target/site \
  -e TEST_PROFILE="$TEST_PROFILE" \
  -e APIBASEURI=http://$HOST_IP \
  -e UIBASEURL=http://$HOST_IP \
$IMAGE_NAME > "$TEST_OUTPUT_DIR/logs/run.log" 2>&1

echo ">>> Tests finished"
echo ">>> Log-file: $TEST_OUTPUT_DIR/logs/run.log"
echo ">>> Tests results: $TEST_OUTPUT_DIR/results"
echo ">>> Report: $TEST_OUTPUT_DIR/report"
