#!/bin/bash

set -e

COVERAGE_FILE="${1:-swagger-coverage-results.json}"
MIN_COVERAGE="${2:-50}"

if [ ! -f "$COVERAGE_FILE" ]; then
    echo "File $COVERAGE_FILE not found!"
    exit 1
fi

if ! command -v jq &> /dev/null; then
    echo "❌ jq is not installed. Please install jq and try again."
    exit 1
fi

ALL_COUNT=$(jq '.conditionStatisticsMap.DefaultStatusConditionPredicate.allCount' "$COVERAGE_FILE")
COVERED_COUNT=$(jq '.conditionStatisticsMap.DefaultStatusConditionPredicate.coveredCount' "$COVERAGE_FILE")

if [ "$ALL_COUNT" == "null" ] || [ "$COVERED_COUNT" == "null" ]; then
    echo "Data was not extracted"
    exit 1
fi

PERCENTAGE=$((COVERED_COUNT * 100 / ALL_COUNT))

echo "API stats:"
echo "Operations: $ALL_COUNT"
echo "Covered operations: $COVERED_COUNT"
echo "Coverage percent: $PERCENTAGE%"
echo "Min coverage: $MIN_COVERAGE%"

if [ "$PERCENTAGE" -lt "$MIN_COVERAGE" ]; then
    echo "FAIL: API coverage ($PERCENTAGE%) < ($MIN_COVERAGE%)"
    exit 1
else
    echo "SUCCESS: API coverage ($PERCENTAGE%) meets the requirements"
    exit 0
fi