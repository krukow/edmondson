#!/bin/bash

set -e

echo "Cleaning build"
rm -rf target
mkdir target
# Build uberjar
clojure -X:depstar:jupyter uberjar :jar target/Edmondson-standalone.jar
