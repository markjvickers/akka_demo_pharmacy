#!/bin/bash
# Assumes that you previously started the pharmacy demo.
echo "Tearing down the pharmacy demo..."

#store current context to set later
stored-context=$(akka config current-context)

#point context to our project
akka config use-context demo-pharmacy

#reset context to original
akka config use-context ${stored-context}

echo "Done"
