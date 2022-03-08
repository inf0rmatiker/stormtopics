# stormtopics

Detecting the Most Popular Topics from Live Twitter Message
Streams using the Lossy Counting Algorithm with Apache Storm. 
This project is the result of a team effort on CS535: Big Data's Assignment 2 project.

Authors: [Caleb Carlson](https://github.com/inf0rmatiker), [Shuo Liu](https://github.com/ShuoLiuSS)

Project is written in Java, and uses Apache Storm for data processing and Zookeeper/SystemD for process supervision.

## Usage

### Apache Storm Cluster Setup

Cluster management scripts can be found in `cluster_management/`
- Shutdown any existing supervisors, nimbus, and zookeeper: `./shutdown.sh`
- Clean up any previous artifacts, log and pid files, etc: `./cleanup.sh`
- Initialize log directories and data directories: `./init.sh`
- Start Zookeeper, the Nimbus process, and any Worker processes remotely: `./start_all.sh`

### Launching the Twitter Topology on the Cluster

- `./submit.sh parallel` for parallel topology, or `./submit.sh linear` for linear topology.
- Monitor the logs of the workers via `tail -f $STORM_HOME/logs/workers-artifacts/<topology_id>/<id>/worker.log`
- Monitor the results logfile via `tail -f ~/results_<topology>.txt`

### Killing the Twitter Topology Running on the Cluster

- Use `./kill.sh`, this will wait for 12 seconds before killing the topology, and invoking the Bolts' `cleanup()` methods.

