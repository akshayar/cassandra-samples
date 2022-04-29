# Overview
This solution consumes CDC records inserted into Apache Pulsar with Datastax CDC for Apache Cassandra (https://github.com/datastax/cdc-apache-cassandra). 
It consumes the records captured and ingests them to either a target Cassandra DB or a Amazon Keyspaces DB. It could be used for Data replication. 
# Build
1. Build code
```
export CASSANDRA_USER_NAME=<Cassandra User Name>
export CASSANDRA_PASSWORD=<Cassandra Password>
export S3_BUCKET_PATH=<bucket name>/<path>
cd <project-root>
./build-and-copy.sh ${S3_BUCKET_PATH}
```
2. This will create the NAR file and copy that to S3 bucket path you specify. 

# Deployment
## Pulsar Sink Configuration
```json
{
  "roots":"${CASSANDRA_URL}",
  "keyspace":"${CASSANDRA_KEY_SAPCE}",
  "tableName":"${CASSANDRA_TABLE}",
  "dcName":"${CASSANDRA_DC_NAME}", 
  "keyspacedb": "true/false", --Default false, Chose true for keyspaces DB
  "nullValueAction":"IGNORE/DELETE/FAIL", --Default IGNORE
  "bulkEnabled": "true/false",  -- Default false
  "malformedDocAction": "IGNORE/WARN/FAIL" --Default FAIL
}
```
### Example Config for  Cassandra
```json
{
  "roots":"10.0.131.64:9042",
  "keyspace":"target",
  "tableName":"customers",
  "dcName":"OSS-dc0",
  "nullValueAction":"DELETE"
}
```
### Example Config for Keyspaces DB
```json
{
  "roots":"cassandra.us-east-1.amazonaws.com:9142",
  "keyspace":"target",
  "tableName":"customers",
  "region":"us-east-1",
  "keyspacedb":"true", 
  "nullValueAction":"DELETE"
}
```
## Pre-requisite
1. Create target keyspace and table.
2. Create user id and password. 
3. Create build with user id and password. Right now the provision to provide the id/password at runtime is not there.
4. Setup Datastax CDC for source Apache Cassandra. Refer [link](../README.md). Ensure that data topic for table with CDC configurations are created. 
## Deployment Steps
1. SSH to Apache Pulsar machine. The instruction set works when connector is deployed on Apache Pulsar cluster. 
2. Download deployment sctips.
```shell
mkdir -p ${HOME}/pulsar-io-cassandra-sink-connector
cd ${HOME}/pulsar-io-cassandra-sink-connector
export S3_BUCKET_PATH=<bucket name>/<path>
aws s3 cp --recursive --include "*.sh" s3://${S3_BUCKET_PATH}/ .
```
3. Update deployment properties
```shell
cat << EOF > deploy-setup.sh
export BUILD_BUCKET=${S3_BUCKET_PATH}
export CASSANDRA_URL=cassandra.us-east-1.amazonaws.com:9142
export NAR_FILE_NAME=pulsar-io-cassandra-sink-1.0.0-SNAPSHOT.nar
export PULSAR_HOME=$HOME/apache-pulsar-2.9.1
export PULSAR_DATA_TOPIC_NAME="persistent://public/default/data-pocdb1.customers"
export CASSANDRA_KEY_SAPCE="tutorialkeyspace"
export CASSANDRA_TABLE="customers"
export CASSANDRA_DC_NAME="OSS-dc0"
export REGION="us-east-1"
EOF

chmod +x *.sh
```
4. Run deployment script of Cassandra or Keyspaces connector
```shell
./deploy-cassandra-sink.sh

##./deploy-keyspaces-sink.sh
```

## Validate Data in target connector

## To Do
1. User Id/Password is provided at the build time.
2. Provison to provide id/password at runtime using secret manager or other method. 
3. Deployment steps for Pulsar connector on a machine separate from Apache Pulsar connector. 
4. Test for performance. 
5. Test for resilience. 

