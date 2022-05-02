#!/bin/bash
PULSAR_HOME=$HOME/apache-pulsar-2.9.1
echo "All Sources"
${PULSAR_HOME}/bin/pulsar-admin sources list
echo "All Sinks"
${PULSAR_HOME}/bin/pulsar-admin sinks list