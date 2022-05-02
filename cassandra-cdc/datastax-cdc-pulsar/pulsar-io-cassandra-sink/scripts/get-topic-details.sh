#!/bin/bash
PULSAR_HOME=$HOME/apache-pulsar-2.9.1
TOPIC_NAME=$1 #persistent://public/default/data-pocdb1.customers
echo "Subcribers for topic ${TOPIC_NAME}"
${PULSAR_HOME}/bin/pulsar-admin topics subscriptions ${TOPIC_NAME}
echo "Stats for topic ${TOPIC_NAME}"
${PULSAR_HOME}/bin/pulsar-admin topics stats ${TOPIC_NAME}