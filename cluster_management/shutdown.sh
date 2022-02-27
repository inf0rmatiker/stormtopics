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

echo -e "Shutting down Storm Workers..."
while read -r WORKER; do
  echo "Shutting down worker on $WORKER"
  ssh -n "$WORKER" "WORKER_PID=\$(ps -aux | grep \"[o]rg.apache.storm.daemon.supervisor.Supervisor\") && echo \"Found worker process \$WORKER_PID\" && kill \$WORKER_PID || echo \"Didn't find any workers!\""
  echo "Shutting down supervisord process on $WORKER"
  ssh -n "$WORKER" "WORKER_PID=\$(ps -aux | grep \"/bin/supervisord\") && echo \"Found supervisord process \$WORKER_PID\" && kill \$WORKER_PID || echo \"Didn't find any supervisord processes!\""
done < "./workers"

echo -e "Shutting down Storm Nimbus on localhost..."
while read -r WORKER; do
  NIMBUS_PID=$(ps -aux | grep \"[o]rg.apache.storm.daemon.supervisor.Supervisor\")
done < "./workers"