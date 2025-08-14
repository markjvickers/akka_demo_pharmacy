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

unexpose_all_routes

# tear down all project routes
# akka routes list
# akka service unexpose demo-pharmacy route
#
# tear down central
# tear down all stores
#akka service unexpose central
#akka service delete central --hard

#reset project to original
akka config set project ${stored_project}

echo "Done"
