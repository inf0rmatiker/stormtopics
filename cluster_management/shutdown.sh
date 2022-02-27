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

USERNAME=$(whoami)

echo -e "Shutting down Storm Workers..."
while read -r WORKER; do
  echo "Shutting down worker on $WORKER"
  ssh -n "$WORKER" "WORKER_PID=\$(cat /tmp/$USERNAME-storm/supervisord.pid) && echo \"Found worker process \$WORKER_PID\" && kill \$WORKER_PID || echo \"Didn't find any workers!\""
done < "./workers"

echo -e "Shutting down Storm Nimbus on local machine ($(hostname))"
NIMBUS_PID=$(cat /tmp/$USERNAME-storm/supervisord.pid)
[ "$NIMBUS_PID" != "" ] && echo "Found Nimbus process $NIMBUS_PID" && kill $NIMBUS_PID || echo "Didn't find any Nimbus instance!"

ZOOKEEPER_HOST=$(cat zookeeper)
echo -e "Shutting down Zookeeper on $ZOOKEEPER_HOST"
ssh -n "$ZOOKEEPER_HOST" "ZK_PID=\$(cat /tmp/$USERNAME-storm/supervisord.pid) && echo \"Found Zookeeper process \$ZK_PID\" && kill \$ZK_PID || echo \"Didn't find any Zookeeper instance!\""
