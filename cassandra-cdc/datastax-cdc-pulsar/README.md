# Architectural Components
1. Apache Pulsar Cluster.
2. DataStax Agent on each node that pushes CDC events to Apache Pulsar cluster. The agent is deployed on each Cassandra node and started in the Cassandra process as an agent. The agent will create a topic for each table which will look like events-keyspce.table.
3. Apache Pulsar source connector for Cassandra. The source connector will listern to messages on events-keyspce.table and push CDC message with data to data-keyspce.table topic. Now this data can be consumed by downstream components like sink connectors. 
4. Down stream data consumers. These could be Apache Pulsar Sink Connector for ElasticSearch which will be demoed here or Apache Spark Process with Hudi etc. 

# Pre-requisite
1. Apache Cassandra cluster is deployed and CDC is enabled on each node. 
2. schema.sql is run which creates the test scheme where CDC is enables for required tables. 
3. For Cassandra 3 ensure that CommitLogs are flushed frequently.
3.a  Add a cron job to each Cassandra node. The cron runs every 1 min in below example which is not a recommended config for production load. 
```shell
## SSH to each cassandra node and add following cron or similar. The cron below is for each min. 
*/1* * * * * /usr/share/oss/bin/nodetool flush >> /home/ubuntu/cronlog 2>&1
```
3.b Modify /usr/share/oss/conf/cassandra.yaml and set values of commitlog_segment_size_in_mb and commitlog_total_space_in_mb artificially low. This is not a recommended config for production load. 
```shell
commitlog_segment_size_in_mb: 1
commitlog_total_space_in_mb: 256
```

#  Deployment
## 1. Apache Pulsar  Cluster
1. The steps below show the process for Single Node Apache Pulsar Cluster
2. Download the package and extract
```shell
wget https://archive.apache.org/dist/pulsar/pulsar-2.9.1/apache-pulsar-2.9.1-bin.tar.gz
tar xvfz apache-pulsar-2.9.1-bin.tar.gz
cd apache-pulsar-2.9.1
```
3. Update `conf/standalone.conf` and change `advertisedAddress` to private IP of the ndoe.
   ```shell
   advertisedAddress=10.0.134.80
   ````
4. Start node in standalone mode.
```shell
nohup bin/pulsar standalone &
```
## 2. DataStax Agent Deployment on each Cassandra Node
### Cassandra 3
1. The instructions below work for Cassandra cluster installed from Datastax Cassandra Marketplace configuration. 
2. Run the steps below on each of the Cassandra node. 
3. SSH to Cassandra node and execute following. Use the IP address of Apache Pulsar node created above. 
```shell
export PULSAR_URL=pulsar://<PULSAR-NODE-IP>:6650
mdkir cassandra-agent
cd cassandra-agent
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-agents-1.0.1.tar
tar -xvf cassandra-source-agents-1.0.1.tar
cd cassandra-source-agents-1.0.1/
AGENT_LIB_PATH=`pwd`/agent-c3-pulsar-1.0.1-all.jar
PULSAR_CONFIG=pulsarServiceUrl=$PULSAR_URL
AGENT_CONFIG="-javaagent:$AGENT_LIB_PATH=$PULSAR_CONFIG"
```
4. Create cassandra.service file to add agent configuration. 
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
5. Copy new cassandra.service file to home directory and system services. 
```shell
cp $HOME/cassandra.service $HOME/cassandra.service.bak
cp cassandra.service.pulsaragent $HOME/cassandra.service
sudo cp $HOME/cassandra.service /etc/systemd/system/cassandra.service
```
6. Create data directory which the agent uses and add permissions.
```shell
sudo mkdir -p /usr/share/oss/bin/../data/cdc
sudo chown cassandra /usr/share/oss/bin/../data/cdc
sudo systemctl daemon-reload
```
7. Restart Cassandra
```shell
sudo service cassandra stop
sudo service cassandra start
```
8. Verify that Agent is stared. 
```shell
grep -i agent /var/log/cassandra/system.log
### See a log statement like following 
### INFO  [main] 2022-02-16 15:44:58,913 Agent.java:92 - CDC agent started
```

### Cassandra 4
1. The instructions below work for Cassandra cluster installed from Datastax Cassandra Marketplace configuration.
2. Run the steps below on each of the Cassandra node.
3. SSH to Cassandra node and execute following. Use the IP address of Apache Pulsar node created above.
```shell
export PULSAR_URL=pulsar://10.0.134.80:6650
mdkir cassandra-agent
cd cassandra-agent
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-agents-1.0.1.tar
tar -xvf cassandra-source-agents-1.0.1.tar
cd cassandra-source-agents-1.0.1/
AGENT_LIB_PATH=`pwd`/agent-c4-pulsar-1.0.1-all.jar
PULSAR_CONFIG=pulsarServiceUrl=$PULSAR_URL
AGENT_CONFIG="-javaagent:$AGENT_LIB_PATH=$PULSAR_CONFIG"
```
4. Create cassandra.service file to add agent configuration.
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
5. Copy new cassandra.service file to home directory and system services.
```shell
cp $HOME/cassandra.service $HOME/cassandra.service.bak
cp cassandra.service.pulsaragent $HOME/cassandra.service
sudo cp $HOME/cassandra.service /etc/systemd/system/cassandra.service
```
6. Create data directory which the agent uses and add permissions.
```shell
sudo mkdir -p /usr/share/oss/bin/../data/cdc
sudo chown cassandra /usr/share/oss/bin/../data/cdc
sudo systemctl daemon-reload
```
7. Restart Cassandra
```shell
sudo service cassandra stop
sudo service cassandra start
```
8. Verify that Agent is stared.
```shell
grep -i agent /var/log/cassandra/system.log
### See a log statement like following 
### INFO  [main] 2022-02-16 15:44:58,913 Agent.java:92 - CDC agent started
```
## 3. Apache Pulsar Source Connector
1. Download Source Connector on Apache Pulsar node (or any other EC2 node).
```shell
mkdir -p $HOME/cassandra-source-connector
cd $HOME/cassandra-source-connector
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-connectors-1.0.1.tar
tar -xvf cassandra-source-connectors-1.0.1.tar

```
2. Download Apache Pulsar package if you are deploying the connector on EC2 node other than Apache Pulsar single node cluster host. The command below are validated on Apache Cluster node. 
3. Create Cassandra Source Connector
```shell
cd $HOME/apache-pulsar-2.9.1/
export KEYSPACE=pocdb1
bin/pulsar-admin source delete --name cassandra-customers-pocdb1  
bin/pulsar-admin source create \
--name cassandra-customers-pocdb1 \
--archive $HOME//cassandra-source-connectors/cassandra-source-connectors-1.0.1/pulsar-cassandra-source-1.0.1.nar \
--tenant public \
--namespace default \
--destination-topic-name public/default/data-pocdb1.customers \
--parallelism 1 \
--source-config '{
             "events.topic": "persistent://public/default/events-pocdb1.customers",
             "keyspace": "pocdb1",
             "table": "customers",
             "contactPoints": "10.0.131.64",
             "port": 9042,
             "loadBalancing.localDc": "OSS-dc0",
             "auth.provider": "PLAIN",
             "auth.username": "cassandra",
             "auth.password": "cassandra"
}'

bin/pulsar-admin source status --name cassandra-customers-pocdb1
tail -f $HOME/apache-pulsar-2.9.1/logs/functions/public/default/cassandra-customers-pocdb1/cassandra-customers-pocdb1-0.log
```
### End to end Solution with ElasticSearch Sink
[link](elasticsearch-sink.md)
### End to end Solution with Cassandra Sink
