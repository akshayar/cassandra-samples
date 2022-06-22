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
bin/pulsar-admin source delete --name ${SOURCE_CONNECTOR_NAME}
bin/pulsar-admin source create \
--name ${SOURCE_CONNECTOR_NAME} \
--archive ${PULSAR_HOME}/cassandra-source-connector/cassandra-source-connectors-1.0.1/pulsar-cassandra-source-1.0.1.nar \
--tenant public \
--namespace default \
--destination-topic-name ${DATA_TOPIC_NAME} \
--parallelism 1 \
--source-config '{
             "events.topic": "persistent://'${EVENT_TOPIC_NAME}'",
             "keyspace": "'${KEYSPACE}'",
             "table": "'${TABLE_NAME}'",
             "contactPoints": "'${CASSANDRA_SERVER}'",
             "port": '${CASSANDRA_PORT}',
             "loadBalancing.localDc": "OSS-dc0",
             "auth.provider": "PLAIN",
             "auth.username": "'${USER_NAME}'",
             "auth.password": "'${PASSWORD}'"
}'

bin/pulsar-admin source status --name ${SOURCE_CONNECTOR_NAME}
