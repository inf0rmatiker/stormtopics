#!/bin/bash

function print_usage {
  echo -e "USAGE:\n\t./submit.sh <topology> <results_file>"
  echo -e "\ttopology: [parallel, linear]"
  echo -e "\tresults_file: absolute path to results file\n"
  echo -e "EXAMPLE:\n\t./submit.sh linear /s/chopin/b/grad/cacaleb"
}

[ $# -ne 2 ] && print_usage && exit 1

TOPOLOGY_TYPE=$1
RESULTS_FILE=$2

mvn clean install package && storm jar target/stormtopics-1.0-SNAPSHOT-jar-with-dependencies.jar \
  twitter.TwitterTopology \
  "$TOPOLOGY_TYPE" \
  "$RESULTS_FILE"