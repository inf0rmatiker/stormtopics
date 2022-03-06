#!/bin/bash

mvn clean install package && storm jar target/stormtopics-1.0-SNAPSHOT.jar WordCountTopology
