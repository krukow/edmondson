#!/bin/bash

set -e

SOURCE="${BASH_SOURCE[0]}"
while [ -h "$SOURCE" ]; do # resolve $SOURCE until the file is no longer a symlink
  DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
  SOURCE="$(readlink "$SOURCE")"
  [[ $SOURCE != /* ]] && SOURCE="$DIR/$SOURCE" # if $SOURCE was a relative symlink, we need to resolve it relative to the path where the symlink file was located
done
DIR="$( cd -P "$( dirname "$SOURCE" )" >/dev/null 2>&1 && pwd )"
if [ "$(uname -s)" = 'Linux' ]; then
    BUILD_DIR=$(readlink -f "$DIR/..")
else
    BUILD_DIR="$DIR/.."
fi
cd $BUILD_DIR
# Build uberjar
cd $BUILD_DIR

opts="-Dmaven.wagon.httpconnectionManager.ttlSeconds=25 -Dmaven.wagon.http.retryHandler.count=3"

if [ "$1" == "docker" ];
then
    MAVEN_OPTS=$opts /usr/local/bin/clojure -X:depstar:jupyter uberjar :jar target/Edmondson-standalone.jar 1>/dev/null
    echo "target/Edmondson-standalone.jar"
else
    MAVEN_OPTS=$opts /usr/local/bin/clojure -T:build uber
fi
