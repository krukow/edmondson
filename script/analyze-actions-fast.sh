#!/bin/bash

set -e

echo "Copy workspace"
cp -r work workspace
cd workspace

echo "Installing template"
cp templates/nbconvert_template_altair.tpl /opt/conda/share/jupyter/nbconvert/templates/lab/nbconvert_template_altair.tpl
cat templates/custom.css >> /opt/conda/share/jupyter/nbconvert/templates/lab/static/index.css

echo "Generating html report"
export NUM_PARTICIPANTS="$(cat ./num_participants.txt)"
export TEAM_NAME="$(cat ./team_name.txt)"
export RESULTS_URL="$(cat ./results_url.txt)"
notebook="$(cat ./report_notebook.txt)"
notebook_basename="examples/google_sheets/$(basename $notebook)"
echo "Executing notebook: $notebook_basename"
jupyter nbconvert "$notebook_basename" --execute --allow-errors --to html --output report.html --output-dir results/html --template lab/nbconvert_template_altair.tpl --HTMLExporter.exclude_input=True --HTMLExporter.exclude_input_prompt=True

echo "Uploading results"
/usr/local/bin/clojure -X:upload :folder-url '"'$(cat upload_folder_url.txt)'"' :file-path '"results/html/report.html"'
