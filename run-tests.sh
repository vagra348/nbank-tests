#!/bin/bash

IMAGE_NAME=nbank-tests
TEST_PROFILE=${1:-api}
TIMESTAMP=$(date +"%Y%m%d_%H%M")
TEST_OUTPUT_DIR=./test-output/$TIMESTAMP

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
  -e APIBASEURI=http://172.23.112.1 \
  -e UIBASEURL=http://172.23.112.1 \
$IMAGE_NAME > "$TEST_OUTPUT_DIR/logs/run.log" 2>&1

echo ">>> Tests finished"
echo "Log-file: $TEST_OUTPUT_DIR/logs/run.log"
echo "Tests results: $TEST_OUTPUT_DIR/results"
echo "Report: $TEST_OUTPUT_DIR/report"