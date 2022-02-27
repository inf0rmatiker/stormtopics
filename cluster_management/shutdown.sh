#!/bin/bash

function print_usage {
  echo -e "\nNAME\n\tshutdown.sh"
  echo -e "\nDESCRIPTION\n\tShuts down all Apache Storm (Nimbus, workers) and Apache Zookeeper daemons."
  echo -e "\tNote: this script looks for the local ./workers file, ./nimbus file, and ./zookeeper file,"
  echo -e "\tcontaining the hosts of the respective servers, separated by newlines. See the .example files for reference."
  echo -e "\nUSAGE\n\tshutdown.sh"
}

[ ! -f "./workers" ]   && echo "No ./workers file found! Exiting" && exit 1
[ ! -f "./nimbus" ]    && echo "No ./nimbus file found! Exiting" && exit 1
[ ! -f "./zookeeper" ] && echo "No ./zookeeper file found! Exiting" && exit 1

