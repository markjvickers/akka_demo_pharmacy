#!/bin/bash
# Assumes that you have previously installed the AKKA CLI and gh CLI, and authorized each.
echo "Starting the pharmacy demo..."

#store current project to reset later
stored_project=$(akka config get project)

#point to our project
akka config set project demo-pharmacy

# only do this if we need to deploy central
# You need to wait until this step is finished before proceeding
gh workflow run central
echo "Waiting for a few minutes until central is deployed..."

#
# Checks if an Akka service is in the "Ready" state.
#
# @param {string} service_name The name of the service to check.
# @param {number} [max_attempts=60] Optional: The maximum number of times to check.
# @returns {0} for success (service is Ready), {1} for failure (timeout).
#
is_service_ready() {
  local service_name="$1"
  local max_attempts="${2:-60}" # Default to 60 attempts if not provided
  local delay=1                 # Delay in seconds between attempts

  if [[ -z "$service_name" ]]; then
    echo "Error: Service name not provided." >&2
    return 1
  fi

  echo "⏳ Waiting for service '$service_name' to become Ready..."

  for (( i=1; i<=max_attempts; i++ )); do
    # Get the status of the specific service
    # grep finds the line for the service
    # awk extracts the 4th column (STATUS)
    local status
    status=$(akka service list | grep "^$service_name\b" | awk '{print $4}')

    if [[ "$status" == "Ready" ]]; then
      echo "✅ Service '$service_name' is Ready."
      return 0 # Success
    fi

    echo "  Attempt $i/$max_attempts: Status is '$status'. Retrying in ${delay}s..."
    sleep "$delay"
  done

  echo "❌ Timed out after $max_attempts attempts. Service '$service_name' is not Ready."
  return 1 # Failure
}

if is_service_ready "central" 200; then

  echo "Proceeding with deployment..."
  akka service expose central

  # this deploys each of the stores
  # Again, you'll need to wait here
  gh workflow run stores

  echo "Waiting for the stores are deployed..."

  if is_service_ready "store-101" 200; then
    akka service expose store-101
  else
      echo "Failed to confirm service readiness. Aborting."
      exit 1
  fi

  if is_service_ready "store-102" 200; then
    akka service expose store-102
  else
      echo "Failed to confirm service readiness. Aborting."
      exit 1
  fi

  # identify the routes, and open them in the browser
  routes=$(akka route list)
  echo "$routes" | awk 'NR > 1 {print $2}' | while read -r hostname; do
    if [ -n "$hostname" ]; then
      url="https://${hostname}"
      echo "🚀 Opening: ${url}"
      open "$url"
    fi
  done

else
  echo "Failed to confirm service readiness. Aborting."
  exit 1
fi

# reset current project
akka config set project ${stored_project}
