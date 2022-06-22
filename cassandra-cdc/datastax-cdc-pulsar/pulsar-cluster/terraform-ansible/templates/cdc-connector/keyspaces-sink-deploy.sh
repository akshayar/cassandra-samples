#!/bin/bash
cd {{ pulsar_home }}/pulsar-io-cassandra-sink-connector/
. ./keyspaces-sink-deploy-config.sh
export KEYSPACE_NAME=${1}
export KEYSPACES_TABLE_NAME=${2}
export PULSAR_DATA_TOPIC_NAME=${3}
export TOPIC_READ_FROM=${4}

export SINK_CONNECTOR_NAME=pulsar-keyspaces-sink-${KEYSPACE_NAME}-${KEYSPACES_TABLE_NAME}

echo Creating ${SINK_CONNECTOR_NAME} ${KEYSPACES_URL} ${CONNECTOR_HOME}
mkdir -p ${CONNECTOR_HOME}/config/
envsubst < ${CONNECTOR_HOME}/keyspaces-sink-config.yaml > ${CONNECTOR_HOME}/config/keyspaces-sink-config-${KEYSPACES_TABLE_NAME}.yaml

export SINK_CONFIG_FILE_PATH=${CONNECTOR_HOME}/config/keyspaces-sink-config-${KEYSPACES_TABLE_NAME}.yaml

cat ${SINK_CONFIG_FILE_PATH}

echo "Delecting connector ${SINK_CONNECTOR_NAME}"
${PULSAR_HOME}/bin/pulsar-admin sink delete --name ${SINK_CONNECTOR_NAME}
sleep 1

echo "Creating connector ${SINK_CONNECTOR_NAME}"
${PULSAR_HOME}/bin/pulsar-admin sink create \
--sink-config-file "${SINK_CONFIG_FILE_PATH}"

sleep 10
echo "Checking Status ${SINK_CONNECTOR_NAME}"
${PULSAR_HOME}/bin/pulsar-admin sink status --name ${SINK_CONNECTOR_NAME}
sleep 10
echo "Checkikng logs ${SINK_CONNECTOR_NAME}"
#tail -f   ${PULSAR_HOME}/logs/functions/public/default/${SINK_CONNECTOR_NAME}/${SINK_CONNECTOR_NAME}-0.log
