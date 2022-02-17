# Architectural Components
1. DataStax Agent on each node that pushes CDC events to Apache Pulsar cluster. The agent is deployed on each Cassandra node and started in the Cassandra process as an agent. The agent will create a topic for each table which will look like events-keyspce.table.  
2. Apache Pulsar source connector for Cassandra. The source connector will listern to messages on events-keyspce.table and push CDC message with data to data-keyspce.table topic. Now this data can be consumed by downstream components like sink connectors. 
3. Down stream data consumers. These could be Apache Pulsar Sink Connector for ElasticSearch which will be demoed here or Apache Spark Process with Hudi etc. 

#  Deployment
## DataStax Agent Deployment
### Cassandra 3
NOTE: The instructions below work for Cassandra cluster installed from Datastax Cassandra Marketplace configuration. 
1. SSH to Cassandra node and execute following 
```shell
mdkir cassandra-agent
cd cassandra-agent
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-agents-1.0.1.tar
tar -xvf cassandra-source-agents-1.0.1.tar
cd cassandra-source-agents-1.0.1/
AGENT_LIB_PATH=`pwd`/agent-c3-pulsar-1.0.1-all.jar
PULSAR_CONFIG=pulsarServiceUrl=pulsar://10.0.134.80:6650
AGENT_CONFIG="-javaagent:$AGENT_LIB_PATH=$PULSAR_CONFIG"
```
2. Create cassandra.service file to add agent configuration. 
```shell
cat << EOF > cassandra.service.pulsaragent
[Unit]
Description=Cassandra Service

[Service]
Environment=JVM_EXTRA_OPTS="$AGENT_CONFIG"
Type=simple
PIDFile=/usr/share/oss/PID
ExecStartPre=/sbin/swapoff -a
ExecStart=/usr/share/oss/bin/cassandra  -p /usr/share/oss/PID

WorkingDirectory=/usr/share/oss
Restart=no
TimeoutStopSec=60
TimeoutStartSec=120
User=cassandra

[Install]
WantedBy=multi-user.target
EOF
```
3. Copy new cassandra.service file to home directory and system services. 
```shell
cp $HOME/cassandra.service $HOME/cassandra.service.bak
cp cassandra.service.pulsaragent $HOME/cassandra.service
sudo cp $HOME/cassandra.service /etc/systemd/system/cassandra.service
```
4. Create data directory which the agent uses and add permissions.
```shell
sudo mkdir -p /usr/share/oss/bin/../data/cdc
sudo chown cassandra /usr/share/oss/bin/../data/cdc
sudo systemctl daemon-reload
```
5. Restart Cassandra
```shell
sudo service cassandra stop
sudo service cassandra start
```
### Cassandra 4
## Apache Pulsar Single Node Cluster
1. Download the package and extract
```shell
wget https://archive.apache.org/dist/pulsar/pulsar-2.9.1/apache-pulsar-2.9.1-bin.tar.gz
tar xvfz apache-pulsar-2.9.1-bin.tar.gz
cd apache-pulsar-2.9.1
```
2. Update `conf/standalone.conf` and change `advertisedAddress` to private IP of the ndoe. 
   ```shell
   advertisedAddress=10.0.134.80
   ````
3. Start node in standalone mode. 
```shell
nohup bin/pulsar standalone &
```
## Apache Pulsar Source Connector
1. Download Source Connector on Apache Pulsar node (or any other EC2 node).
```shell
mkdir cassandra-source-connector
cd cassandra-source-connector
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-connectors-1.0.1.tar
tar -xvf cassandra-source-connectors-1.0.1.tar

```
2. Download Apache Pulsar package if you are deploying the connector on EC2 node other than Apache Pulsar single node cluster host. The command below are validated on Apache Cluster node. 
3. Create Cassandra Source Connector
```shell
cd $HOME/apache-pulsar-2.9.1/
bin/pulsar-admin source delete --name cassandra-customers  

bin/pulsar-admin source create \
--name cassandra-customers \
--archive /home/ec2-user/cassandra-source-connectors/cassandra-source-connectors-1.0.1/pulsar-cassandra-source-1.0.1.nar \
--tenant public \
--namespace default \
--destination-topic-name public/default/data-pocdb.customers \
--parallelism 1 \
--source-config '{
             "events.topic": "persistent://public/default/events-pocdb.customers",
             "keyspace": "pocdb",
             "table": "customers",
             "contactPoints": "10.0.131.46",
             "port": 9042,
             "loadBalancing.localDc": "OSS-dc0",
             "auth.provider": "PLAIN",
             "auth.username": "cassandra",
             "auth.password": "cassandra"
}'

bin/pulsar-admin source status --name cassandra-customers

```

## Apache Pulsar Elastic Search Sink Connector
1. Download Sink Connector on Apache Pulsar node (or any other EC2 node).
```shell
mkdir pulsar-sink-es
cd pulsar-sink-es/
curl "https://dlcdn.apache.org/pulsar/pulsar-2.9.1/connectors/pulsar-io-elastic-search-2.9.1.nar" -o pulsar-io-elastic-search-2.9.1.
nar
ES_SINK_PATH=`pwd`/pulsar-io-elastic-search-2.9.1.nar
echo $ES_SINK_PATH
```
2. Download Apache Pulsar package if you are deploying the connector on EC2 node other than Apache Pulsar single node cluster host. The command below are validated on Apache Cluster node.
3. Create Elastic Search Sink Connector
```shell
cd $HOME/apache-pulsar-2.9.1/
bin/pulsar-admin sink delete --name es-sink-pocdb-customers  
echo $ES_SINK_PATH
bin/pulsar-admin sink create \
--archive $ES_SINK_PATH \
--tenant public \
--namespace default \
--name es-sink-pocdb-customers \
--inputs "persistent://public/default/data-pocdb.customers" \
--subs-position Earliest \
--sink-config "{
         \"elasticSearchUrl\":\"http://10.0.138.240:9200\",
          \"indexName\":\"pocdb.customers\",
         \"keyIgnore\":\"false\",
         \"schemaEnable\":\"true\"
}"
bin/pulsar-admin sink status --name es-sink-pocdb-customers
```

