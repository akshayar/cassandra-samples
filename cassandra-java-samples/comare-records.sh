#!/bin/bash
if [ -z "$JAR_PATH" ]
then
  JAR_PATH=target/cassandra-java-samples.jar
fi
echo "java -cp ${JAR_PATH} com.cassandra.samples.CompareRecords $@"
java -cp ${JAR_PATH} com.cassandra.samples.CompareRecords $@