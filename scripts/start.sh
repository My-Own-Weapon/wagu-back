#!/bin/bash

# Run the new Docker container
sudo docker run -d --name github-actions-demo-new --rm --env-file ./.env -p 8081:8080 $DOCKERHUB_USERNAME/github-actions-demo:latest