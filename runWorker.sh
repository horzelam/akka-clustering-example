#!/usr/bin/env bash
mvn exec:java -Dexec.mainClass="com.simple.worker.WorkerApp" -Dconfig.resource=worker/reference.conf

