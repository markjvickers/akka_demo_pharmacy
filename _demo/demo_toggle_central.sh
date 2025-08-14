#!/bin/bash

# Assumes that you previously started the pharmacy demo.
echo "Toggling state of the central service on demo-pharmacy..."

#store current project (to reset later)
stored_project=$(akka config get project)

echo "stored-project=${stored_project}"

#point to our project
akka config set project demo-pharmacy

get_central_status() {
    akka service get central | awk '/^Status:/ {print $2; exit}'
}

echo "▶️  Checking status of service 'central'..."

# 1. Call the function to get the current status
status=$(get_central_status)

echo "✅  Current service status is: $status"

# 2. Check the status and run the appropriate command
if [[ "$status" == "Ready" ]]; then
  echo "⏸️  Status is 'Running'. Pausing the service..."
  akka service pause central
else
  echo "▶️  Status is not 'Running'. Resuming the service..."
  akka service resume central
fi

#reset project to original
akka config set project ${stored_project}

echo "Done"
