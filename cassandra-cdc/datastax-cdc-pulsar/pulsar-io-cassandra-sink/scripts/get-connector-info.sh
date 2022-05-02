#!/bin/bash
PULSAR_HOME=$HOME/apache-pulsar-2.9.1

echo "Stats for connectors "
${PULSAR_HOME}/bin/pulsar-admin functions-worker function-stats | jq
echo "Workers in the cluster "
${PULSAR_HOME}/bin/pulsar-admin functions-worker get-cluster | jq

