#!/bin/bash
if [ -z "$JAR_PATH" ]
then
  JAR_PATH=target/cassandra-java-samples.jar
fi

echo "java -Ddelay=$DELAY -Dcount=$COUNT  -cp ${JAR_PATH} com.cassandra.samples.FakeProductInserter $@"
java -Ddelay=$DELAY -Dcount=$COUNT -cp  ${JAR_PATH} com.cassandra.samples.FakeProductInserter $@