#!/usr/bin/env bash
#this has to be run 2 times to form a cluster
mvn exec:java -Dexec.mainClass="com.simple.master.MasterApp" -Dconfig.resource=master/reference.conf -Dexec.args="2552"

