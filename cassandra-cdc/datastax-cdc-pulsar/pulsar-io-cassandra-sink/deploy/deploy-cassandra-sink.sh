#!/bin/bash
. ./deploy-setup.sh

echo Creating ${SINK_CONNECTOR_NAME} $CASSANDRA_URL ${CONNECTOR_HOME}
echo "Copying s3://${BUILD_BUCKET}/${NAR_FILE_NAME}"
mkdir -p ${CONNECTOR_HOME}
cd ${CONNECTOR_HOME} ; rm -rf *.nar; aws s3 cp  s3://${BUILD_BUCKET}/${NAR_FILE_NAME} . ;
CASSANDRA_SINK_NAR_PATH=`pwd`/${NAR_FILE_NAME}


echo "Delecting connector ${SINK_CONNECTOR_NAME}"
cd ${PULSAR_HOME}
pwd
bin/pulsar-admin sink delete --name ${SINK_CONNECTOR_NAME}
sleep 10

echo "Creating connector ${SINK_CONNECTOR_NAME}"
cd ${PULSAR_HOME}
pwd
bin/pulsar-admin sink create \
--sink-config-file "${SINK_CONFIG_FILE_PATH}"

sleep 10
echo "Checking Status ${SINK_CONNECTOR_NAME}"
bin/pulsar-admin sink status --name ${SINK_CONNECTOR_NAME}
sleep 10
echo "Checkikng logs ${SINK_CONNECTOR_NAME}"
cd logs/functions/public/default/${SINK_CONNECTOR_NAME}/
tail -f  ${SINK_CONNECTOR_NAME}-0.log
