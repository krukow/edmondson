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

echo "Building"
./script/build.sh

echo "Installing"
set +e
/usr/local/bin/clojure -A:jupyter -m clojupyter.cmdline remove-install edmondson
set -e
/usr/local/bin/clojure -A:jupyter -m clojupyter.cmdline install --ident edmondson --jarfile target/Edmondson-standalone.jar
