cd $HOME/apache-pulsar-2.9.1/
bin/pulsar-admin source delete --name cassandra-customers

bin/pulsar-admin source create \
--name cassandra-customers \
--archive /home/ec2-user/cassandra-source-connectors/cassandra-source-connectors-1.0.1/pulsar-cassandra-source-1.0.1.nar \
--tenant public \
--namespace default \
--destination-topic-name public/default/data-pocdb1.customers \
--parallelism 1 \
--source-config '{
"events.topic": "persistent://public/default/events-pocdb1.customers",
"keyspace": "pocdb",
"table": "customers",
"contactPoints": "10.0.168.88",
"port": 9042,
"loadBalancing.localDc": "OSS-dc0",
"auth.provider": "PLAIN",
"auth.username": "cassandra",
"auth.password": "cassandra"
}'

bin/pulsar-admin source status --name cassandra-customers





cd $HOME/pulsar-sink-cassandra ; rm -rf *.nar; aws s3 cp  s3://akshaya-lambda-codes/pulsar-sink-cassandra-1.0.0-SNAPSHOT.nar . ;
CASSANDRA_URL=10.0.131.64:9042
CASSANDRA_SINK_NAR_PATH=/home/ec2-user/pulsar-sink-cassandra/pulsar-sink-cassandra-1.0.0-SNAPSHOT.nar
cd $HOME/apache-pulsar-2.9.1/
bin/pulsar-admin sink delete --name cassandra-sink-customers 
echo $CASSANDRA_URL
bin/pulsar-admin sink create \
--archive ${CASSANDRA_SINK_NAR_PATH} \
--tenant public \
--namespace default \
--name cassandra-sink-customers \
--inputs "persistent://public/default/data-pocdb1.customers" \
--subs-position Earliest \
--sink-config "{
\"roots\":\"$CASSANDRA_URL\",
\"keyspace\":\"pulsar_test_keyspace\",
\"columnFamily\":\"pulsar_test_table\",
\"keyname\":\"key\",
\"columnName\":\"col\",
\"nullValueAction\":\"DELETE\"
}"
bin/pulsar-admin sink status --name cassandra-sink-customers
cd logs/functions/public/default/cassandra-sink-customers/
tail -f  cassandra-sink-customers-0.log


FILE_SINK_PATH=/home/ec2-user/filesink/pulsar-io-cassandra-2.9.1.nar
cd $HOME/apache-pulsar-2.9.1/
bin/pulsar-admin sink delete --name file-sink-pocdb-customers
bin/pulsar-admin sink create \
--archive $FILE_SINK_PATH \
--tenant public \
--namespace default \
--name file-sink-pocdb-customers \
--inputs "persistent://public/default/data-pocdb.customers" \
--subs-position Earliest \
--sink-config "{
}"
bin/pulsar-admin sink status --name file-sink-pocdb-customers