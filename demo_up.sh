#!/bin/bash
# Assumes that you have previously installed the AKKA CLI and gh CLI, and authorized each.
echo "Starting the pharmacy demo..."

# only do this if we need to deploy central
# You need to wait until this step is finished before proceeding
gh workflow run central
echo "Waiting for a few minutes until central is deployed..."
sleep 200

#expose the central service host
akka service expose central

# this deploys each of the stores
# Again, you'll need to wait here
gh workflow run stores

# Wait for the stores to be deployed
echo "Waiting for a few minutes until the stores are deployed..."
sleep 200

# Expose each of the store services
akka service expose store-101
akka service expose store-102

# identify the routes, and open them in the browser
routes=$(akka route list)
echo "$routes" | awk 'NR > 1 {print $2}' | while read -r hostname; do
  if [ -n "$hostname" ]; then
    url="https://${hostname}"
    echo "🚀 Opening: ${url}"
    open "$url"
  fi
done
