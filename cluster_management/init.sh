#!/bin/bash

function print_usage {
  echo -e "\nNAME\n\tinit.sh"
  echo -e "\nDESCRIPTION\n\tEstablishes the temporary Apache Storm / Zookeeper directories."
  echo -e "\tNote: this script looks for the local ./workers file, ./nimbus file, and ./zookeeper file,"
  echo -e "\tcontaining the hosts of the respective servers, separated by newlines. See the .example files for reference."
  echo -e "\nUSAGE\n\tinit.sh"
}

[ ! -f "./workers" ]   && echo "No ./workers file found! Exiting" && exit 1
[ ! -f "./nimbus" ]    && echo "No ./nimbus file found! Exiting" && exit 1
[ ! -f "./zookeeper" ] && echo "No ./zookeeper file found! Exiting" && exit 1

USERNAME=$(whoami)
for NODE in $(cat nimbus); do
  echo "Creating $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "mkdir /tmp/$USERNAME-storm"
done
for NODE in $(cat workers); do
  echo "Creating $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "mkdir /tmp/$USERNAME-storm"
done
for NODE in $(cat zookeeper); do
  echo "Creating $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "mkdir -p /tmp/$USERNAME-storm /tmp/zookeeper_$USERNAME/data"
done

echo "Done"
