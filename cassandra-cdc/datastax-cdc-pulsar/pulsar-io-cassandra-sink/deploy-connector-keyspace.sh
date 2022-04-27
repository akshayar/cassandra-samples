#!/bin/bash
CONNECTOR_NAME=pulsar-io-cassandra-sink-connector
BUILD_BUCKET=akshaya-lambda-codes
CASSANDRA_URL=cassandra.us-east-1.amazonaws.com:9142
JAR_FILE_NAME=pulsar-io-cassandra-sink-1.0.0-SNAPSHOT.nar
PULSAR_HOME=$HOME/apache-pulsar-2.9.1/
PULSAR_DATA_TOPIC_NAME="persistent://public/default/data-pocdb1.customers"
CASSANDRA_KEY_SAPCE="tutorialkeyspace"
CASSANDRA_TABLE="customers"
#CASSANDRA_DC_NAME="OSS-dc0"
REGION="us-east-1"
SINK_CONNECTOR_NAME=pulsar-cassandra-sink-${CASSANDRA_KEY_SAPCE}-${CASSANDRA_TABLE}

echo $CASSANDRA_URL

mkdir -p $HOME/${CONNECTOR_NAME}
echo "Copying s3://${BUILD_BUCKET}/${JAR_FILE_NAME}"
cd $HOME/${CONNECTOR_NAME} ; rm -rf *.nar; aws s3 cp  s3://${BUILD_BUCKET}/${JAR_FILE_NAME} . ;
CASSANDRA_SINK_NAR_PATH=`pwd`/${JAR_FILE_NAME}
cd ${PULSAR_HOME}
pwd
echo "Delecting connector ${SINK_CONNECTOR_NAME}"
bin/pulsar-admin sink delete --name ${SINK_CONNECTOR_NAME}

echo "Creating connector ${SINK_CONNECTOR_NAME}"
bin/pulsar-admin sink create \
--archive ${CASSANDRA_SINK_NAR_PATH} \
--tenant public \
--namespace default \
--name ${SINK_CONNECTOR_NAME} \
--inputs ${PULSAR_DATA_TOPIC_NAME} \
--subs-position Earliest \
--sink-config "{
\"roots\":\"${CASSANDRA_URL}\",
\"keyspace\":\"${CASSANDRA_KEY_SAPCE}\",
\"tableName\":\"${CASSANDRA_TABLE}\",
\"region\":\"${REGION}\",
\"keyspacedb\":\"true\",
\"nullValueAction\":\"DELETE\"
}"
echo "Checking Status ${SINK_CONNECTOR_NAME}"
bin/pulsar-admin sink status --name ${SINK_CONNECTOR_NAME}

echo "Checkikng logs ${SINK_CONNECTOR_NAME}"
cd logs/functions/public/default/${SINK_CONNECTOR_NAME}/
tail -f  ${SINK_CONNECTOR_NAME}-0.log