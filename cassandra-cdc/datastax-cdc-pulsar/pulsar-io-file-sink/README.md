# Overview
This solution consumes CDC records inserted into Apache Pulsar with Datastax CDC for Apache Cassandra (https://github.com/datastax/cdc-apache-cassandra) and writes them into a file. 

# Build
1. Build code
```
./build-and-copy.sh ${S3_BUCKET_PATH}
```
2. This will create the NAR file and copy that to S3 bucket path you specify. 

# Deployment
## Pulsar Sink Configuration
```yaml
archive: ${CASSANDRA_SINK_NAR_PATH}
tenant: public
namespace: default
name: ${SINK_CONNECTOR_NAME}
inputs:
- ${PULSAR_DATA_TOPIC_NAME}
subs-position: Earliest
configs:
    fileJson : "${JSON_OUTPUT_FILE}"
    fileAvro : "${GENERIC_AVRO_FILE}"

```

## Pre-requisite
1. Setup Datastax CDC for source Apache Cassandra. Refer [link](../README.md). Ensure that data topic for table with CDC configurations are created. 

## Deployment 
### One Time Configuration
1. SSH to Apache Pulsar machine. The instruction set works when connector is deployed on Apache Pulsar cluster. 
2. Download deployment scripts.
```shell
export BUILD_BUCKET=akshaya-lambda-codes/pulsar-sink
export NAR_FILE_NAME=pulsar-io-file-sink-1.0.0-SNAPSHOT.nar
export PULSAR_HOME=$HOME/apache-pulsar-2.9.1
export CONNECTOR_HOME=${HOME}/pulsar-io-file-sink-connector

echo Creating ${SINK_CONNECTOR_NAME} ${JSON_OUTPUT_FILE} ${GENERIC_AVRO_FILE}
echo "Copying s3://${BUILD_BUCKET}/${NAR_FILE_NAME}"
mkdir -p ${CONNECTOR_HOME}
cd ${CONNECTOR_HOME} ; rm -rf *.nar; aws s3 cp  s3://${BUILD_BUCKET}/${NAR_FILE_NAME} . ;
CASSANDRA_SINK_NAR_PATH=`pwd`/${NAR_FILE_NAME}

```

### Deploy and run
1. Deploy and run
```shell
$PULSAR_HOME/bin/pulsar-admin topics list public/default

export PULSAR_DATA_TOPIC_NAME=persistent://public/default/data-demo.cyclist_stats
export JSON_OUTPUT_FILE=${CONNECTOR_HOME}/json.txt
export GENERIC_AVRO_FILE=${CONNECTOR_HOME}/avro.txt
export SINK_CONNECTOR_NAME=file-sink-cassandra
cat << EOF > deploy-config.yaml
archive: ${CASSANDRA_SINK_NAR_PATH}
tenant: public
namespace: default
name: ${SINK_CONNECTOR_NAME}
inputs:
  - ${PULSAR_DATA_TOPIC_NAME}
subs-position: Earliest
configs:
  fileJson: "${JSON_OUTPUT_FILE}"
  fileAvro: "${GENERIC_AVRO_FILE}"
EOF

cat deploy-config.yaml

echo "Delecting connector ${SINK_CONNECTOR_NAME}"
$PULSAR_HOME/bin/pulsar-admin sink delete --name ${SINK_CONNECTOR_NAME}
sleep 10

echo "Creating connector ${SINK_CONNECTOR_NAME}"
$PULSAR_HOME/bin/pulsar-admin sink create \
--sink-config-file "${CONNECTOR_HOME}/deploy-config.yaml"

sleep 10
echo "Checking Status ${SINK_CONNECTOR_NAME}"
$PULSAR_HOME/bin/pulsar-admin sink status --name ${SINK_CONNECTOR_NAME}
sleep 10
echo "Checkikng logs ${SINK_CONNECTOR_NAME}"

tail -f  $PULSAR_HOME/logs/functions/public/default/${SINK_CONNECTOR_NAME}/${SINK_CONNECTOR_NAME}-0.log
```