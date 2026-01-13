#!/bin/bash
cd "$(dirname "$0")" || exit 1

echo "Current directory: $(pwd)"

echo ">>> Stop Docker Compose"
docker compose down

echo ">>> Docker pull all browsers"
json_file="./config/browsers.json"
if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi
images=$(jq -r '.. | objects | select(.image) | .image' "$json_file")
for image in $images; do
    echo ">>> Pulling $image..."
    docker pull "$image"
done

echo ">>> Start Docker Compose"
docker compose up -d