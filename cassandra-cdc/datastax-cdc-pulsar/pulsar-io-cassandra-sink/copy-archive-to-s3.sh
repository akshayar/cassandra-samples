#!/bin/bash
S3_PATH=$1
if [ -z "$S3_PATH" ]
then
  S3_PATH=akshaya-lambda-codes/pulsar-sink
fi
aws s3 cp target/pulsar-io-cassandra-sink-*.nar s3://$S3_PATH/
aws s3 cp  deploy-setup.sh s3://$S3_PATH/
aws s3 cp  deploy-keyspaces-sink.sh s3://$S3_PATH/
aws s3 cp  deploy-cassandra-sink.sh s3://$S3_PATH/


