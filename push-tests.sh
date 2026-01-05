#!/bin/bash

IMAGE_NAME=nbank-tests
DOCKERHUB_USERNAME=vagra348
TAG=latest

if [ -z "$DOCKERHUB_TOKEN" ]; then
    echo "ERROR: Please set DOCKERHUB_TOKEN environment"
    echo "Example: export DOCKERHUB_TOKEN='<your-token>'"
    exit 1
fi

echo ">>> Login to Docker Hub with token"
echo $DOCKERHUB_TOKEN | docker login --username $DOCKERHUB_USERNAME --password-stdin

echo ">>> Tagging Image"
docker tag $IMAGE_NAME $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Sending Image to Docker Hub"
docker push $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG

echo ">>> Finished! Image is available as: docker pull $DOCKERHUB_USERNAME/$IMAGE_NAME:$TAG"