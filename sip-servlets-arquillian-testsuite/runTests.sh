#!/bin/bash

#Use the Fail-At-End flag
export MAVEN_OPTS="-Xmx2048m -XX:MaxPermSize=512m"
mvn -fae test
