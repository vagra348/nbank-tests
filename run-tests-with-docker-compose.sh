#!/bin/bash

echo ">>> Starting test infrastructure"
./infra/docker_compose/restart-docker.sh

echo ">>> Starting tests"
./run-tests.sh

echo ">>> Stopping test infrastructure"
docker compose -f "./infra/docker_compose/docker-compose.yml" down

echo ">>> Finished. Press any key to exit..."
read -n 1 -s