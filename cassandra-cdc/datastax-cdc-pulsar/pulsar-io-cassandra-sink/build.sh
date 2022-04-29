#!/bin/bash
~/code/tools/apache-maven-3.8.1/bin/mvn clean package -DskipTests -Dusername=${CASSANDRA_USER_NAME} -Dpassword=${CASSANDRA_PASSWORD}


