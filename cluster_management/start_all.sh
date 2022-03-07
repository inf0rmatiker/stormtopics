#!/bin/bash

function print_usage {
  echo -e "\nNAME\n\tstart_all.sh"
  echo -e "\nDESCRIPTION\n\tStarts all Apache Storm (Nimbus, workers) and Apache Zookeeper daemons."
  echo -e "\tNote: this script looks for the local ./workers file, ./nimbus file, and ./zookeeper file,"
  echo -e "\tcontaining the hosts of the respective servers, separated by newlines. See the .example files for reference."
  echo -e "\nUSAGE\n\tstart_all.sh"
}

[ -z "$STORM_HOME" ]   && echo "Please set STORM_HOME environment variable before continuing" && exit 1
[ ! -f "./workers" ]   && echo "No ./workers file found! Exiting" && exit 1
[ ! -f "./nimbus" ]    && echo "No ./nimbus file found! Exiting" && exit 1
[ ! -f "./zookeeper" ] && echo "No ./zookeeper file found! Exiting" && exit 1

ZOOKEEPER_HOST=$(cat zookeeper)
echo -e "Starting Zookeeper on $ZOOKEEPER_HOST"
ssh "$ZOOKEEPER_HOST" "supervisord -c $STORM_HOME/zk-supervisord.conf"

sleep 2

echo -e "Starting Nimbus on local machine ($(hostname))"
supervisord -c $STORM_HOME/nimbus-supervisord.conf

sleep 5

while read -r WORKER; do
  echo "Starting worker on $WORKER"
  ssh -n "$WORKER" "source ~/.zshrc && supervisord -c $STORM_HOME/worker-supervisord.conf"
  sleep 5
done < "./workers"

echo "Done"
