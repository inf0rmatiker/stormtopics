#!/bin/bash

function print_usage {
  echo -e "\nNAME\n\tstdouts.sh"
  echo -e "\nDESCRIPTION\n\tRemoves the temporary Apache Storm / Zookeeper directories."
  echo -e "\tNote: this script looks for the local ./workers file, ./nimbus file, and ./zookeeper file,"
  echo -e "\tcontaining the hosts of the respective servers, separated by newlines. See the .example files for reference."
  echo -e "\nUSAGE\n\tstdouts.sh"
}

[ ! -f "./workers" ]   && echo "No ./workers file found! Exiting" && exit 1
[ ! -f "./nimbus" ]    && echo "No ./nimbus file found! Exiting" && exit 1
[ ! -f "./zookeeper" ] && echo "No ./zookeeper file found! Exiting" && exit 1

USERNAME=$(whoami)
for NODE in $(cat workers); do
  echo "WORKER LOGS"
  ssh "$NODE" "cat /tmp/storm_worker*.log"
done


echo "Done"
