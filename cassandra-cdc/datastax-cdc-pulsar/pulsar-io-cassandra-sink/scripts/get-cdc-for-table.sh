#!/bin/bash
PULSAR_HOME=$HOME/apache-pulsar-2.9.1
KEYSPACE_NAME=$1
TABLE_NAME=$2
EVENT_TOPIC="persistent://public/default/events-${KEYSPACE_NAME}.${TABLE_NAME}"
echo "Writers to events topic ${EVENT_TOPIC}"
./get-topic-details.sh ${EVENT_TOPIC}
DATA_TOPIC="persistent://public/default/data-${KEYSPACE_NAME}.${TABLE_NAME}"
echo "Readers/Writes to data topic ${DATA_TOPIC}"
./get-topic-details.sh ${DATA_TOPIC}
