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
