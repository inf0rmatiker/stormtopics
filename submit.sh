#!/bin/bash

mvn clean install build && storm jar target/stormtopics-1.0-SNAPSHOT.jar WordCountTopology
