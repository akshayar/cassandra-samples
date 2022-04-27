#!/bin/bash
~/code/tools/apache-maven-3.8.1/bin/mvn clean package -DskipTests -Dusername=${CASSANDRA_USER_NAME} -Dpassword=${CASSANDRA_PASSWORD}

S3_BUCKET=$1
if [ -z "$S3_BUCKET" ]
then
  S3_BUCKET=akshaya-lambda-codes
fi
aws s3 cp target/pulsar-io-cassandra-sink-*.nar s3://$S3_BUCKET/


