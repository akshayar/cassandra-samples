#!/bin/bash
if [ -z "$MAVEN_HOME" ]
then
  mvn clean package -DskipTests
else
  $MAVEN_HOME/bin/mvn clean package -DskipTests
fi


#~/code/tools/apache-maven-3.8.1/bin/mvn clean package -DskipTests -Dusername=${CASSANDRA_USER_NAME} -Dpassword=${CASSANDRA_PASSWORD}

