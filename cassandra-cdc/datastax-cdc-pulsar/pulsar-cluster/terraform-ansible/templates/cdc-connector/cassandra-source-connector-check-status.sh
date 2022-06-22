#!/bin/bash
CASSANDRA_SERVER=${1}
CASSANDRA_PORT=${2}
KEYSPACE=${3}
TABLE_NAME=${4}
USER_NAME=${5}
PASSWORD=${6}
PULSAR_HOME=/opt/pulsar
EVENT_TOPIC_NAME=public/default/events-${KEYSPACE}.${TABLE_NAME}
DATA_TOPIC_NAME=public/default/data-${KEYSPACE}.${TABLE_NAME}

cd ${PULSAR_HOME}

SOURCE_CONNECTOR_NAME=cassandra-source-${KEYSPACE}-${TABLE_NAME}

bin/pulsar-admin source status --name ${SOURCE_CONNECTOR_NAME}
