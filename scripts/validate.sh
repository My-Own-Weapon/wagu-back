#!/bin/bash

# Check if the new application is running
if curl -s http://localhost:8081 > /dev/null; then
  # Stop the old container
  docker stop github-actions-demo || true

  # Rename the new container
  docker rename github-actions-demo-new github-actions-demo

  # Remove unused Docker resources
  docker system prune -f

  echo "Application is running"
  exit 0
else
  # Stop the new container if the validation failed
  docker stop github-actions-demo-new || true

  echo "Application is not running"
  exit 1
fi
