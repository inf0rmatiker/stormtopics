#!/bin/bash

git fetch && git pull && mvn clean install package
