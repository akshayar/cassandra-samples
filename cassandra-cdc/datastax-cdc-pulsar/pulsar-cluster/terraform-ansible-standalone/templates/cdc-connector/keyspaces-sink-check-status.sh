#!/bin/bash
cd {{ pulsar_home }}/pulsar-io-cassandra-sink-connector/
. ./keyspaces-sink-deploy-config.sh
export KEYSPACE_NAME=${1}
export KEYSPACES_TABLE_NAME=${2}
export SINK_CONNECTOR_NAME=pulsar-keyspaces-sink-${KEYSPACE_NAME}-${KEYSPACES_TABLE_NAME}

echo Checking status of  ${SINK_CONNECTOR_NAME} ${KEYSPACES_URL} ${CONNECTOR_HOME}
bin/pulsar-admin sink status --name ${SINK_CONNECTOR_NAME}