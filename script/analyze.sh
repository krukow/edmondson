#!/bin/bash

set -e

if ! command -v jupyter &> /dev/null
then
    echo "jupyter command could not be found"
    exit
fi

./script/go.sh

echo "Generating html report from: $1"
jupyter nbconvert "$1" --execute --allow-errors --to html --output report.html --output-dir results/html --HTMLExporter.exclude_input=True --HTMLExporter.exclude_input_prompt=True

ls results/html
