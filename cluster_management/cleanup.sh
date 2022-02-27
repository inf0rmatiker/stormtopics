#!/bin/bash

function print_usage {
  echo -e "\nNAME\n\tcleanup.sh"
  echo -e "\nDESCRIPTION\n\tRemoves the temporary Apache Storm / Zookeeper directories."
  echo -e "\tNote: this script looks for the local ./workers file, ./nimbus file, and ./zookeeper file,"
  echo -e "\tcontaining the hosts of the respective servers, separated by newlines. See the .example files for reference."
  echo -e "\nUSAGE\n\tcleanup.sh"
}

[ ! -f "./workers" ]   && echo "No ./workers file found! Exiting" && exit 1
[ ! -f "./nimbus" ]    && echo "No ./nimbus file found! Exiting" && exit 1
[ ! -f "./zookeeper" ] && echo "No ./zookeeper file found! Exiting" && exit 1

USERNAME=$(whoami)
for NODE in $(cat nimbus); do
  echo "Cleaning $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "rm -rf /tmp/$USERNAME-storm"
done
for NODE in $(cat workers); do
  echo "Cleaning $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "rm -rf /tmp/$USERNAME-storm"
done
for NODE in $(cat zookeeper); do
  echo "Cleaning $USERNAME's Storm directory on $NODE:/tmp/$USERNAME-storm"
  ssh -n "$NODE" "rm -rf /tmp/$USERNAME-storm"
done

echo "Done"
