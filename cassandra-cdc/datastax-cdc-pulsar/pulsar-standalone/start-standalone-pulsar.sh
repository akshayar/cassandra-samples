#!/bin/bash
cd /home/ec2-user/apache-pulsar-2.9.1
nohup bin/pulsar standalone &
sleep 10
status=`bin/pulsar-admin brokers healthcheck`
if [ ${status} == ok ]
then
  echo started successfully
  exit 0
else
  echo start failed
  exit 1
fi

