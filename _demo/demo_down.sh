#!/bin/bash
# Assumes that you previously started the pharmacy demo.
echo "Tearing down the pharmacy demo..."

#store current project (to reset later)
stored_project=$(akka config get project)

echo "stored-project=${stored_project}"

#point to our project
akka config set project demo-pharmacy

# This function lists all Akka routes and then unexposes each one
# by parsing the hostname from the output.
unexpose_all_routes() {
  akka routes list | awk '
    NR > 1 {
      if ($2 != "") {
        print "--> Unexposing service from service/hostname: " $1 "/" $2
        system("akka service unexpose " $1 " " $2)
      }
    }
  '
}

tear_down_stores() {
  # Read the JSON file
  stores_json=$(cat ../config/stores.json)

  # Process each store
  while IFS= read -r store; do
    pharmacy_id=$(echo "$store" | jq -r '.pharmacyId')

    echo "🏪 Tearing Down Store $pharmacy_id"
    echo "store-$pharmacy_id" | akka service delete store-$pharmacy_id --hard

  done < <(echo "$stores_json" | jq -c '.stores[]')
}

unexpose_all_routes
echo "central" | akka service delete central --hard
tear_down_stores

#reset project to original
akka config set project ${stored_project}

echo "Done"
