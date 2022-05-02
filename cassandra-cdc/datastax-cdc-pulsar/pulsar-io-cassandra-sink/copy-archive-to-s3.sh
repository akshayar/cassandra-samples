#!/bin/bash
S3_PATH=$1
if [ -z "$S3_PATH" ]
then
  S3_PATH=akshaya-lambda-codes/pulsar-sink
fi
aws s3 cp target/pulsar-io-cassandra-sink-*.nar s3://$S3_PATH/
aws s3 cp --recursive  deploy/ s3://$S3_PATH/deploy/
aws s3 cp --recursive config/ s3://$S3_PATH/config/
aws s3 cp --recursive scripts/ s3://$S3_PATH/scripts/

