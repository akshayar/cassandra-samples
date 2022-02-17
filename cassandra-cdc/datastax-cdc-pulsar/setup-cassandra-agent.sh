mdkir cassandra-agent
cd cassandra-agent
wget https://downloads.datastax.com/cdc-apache-cassandra/cassandra-source-agents-1.0.1.tar
tar -xvf cassandra-source-agents-1.0.1.tar
cd cassandra-source-agents-1.0.1/
AGENT_LIB_PATH=`pwd`/agent-c3-pulsar-1.0.1-all.jar
PULSAR_CONFIG=pulsarServiceUrl=pulsar://10.0.134.80:6650
AGENT_CONFIG="-javaagent:$AGENT_LIB_PATH=$PULSAR_CONFIG"

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


cp $HOME/cassandra.service $HOME/cassandra.service.bak
cp cassandra.service.pulsaragent $HOME/cassandra.service
sudo cp $HOME/cassandra.service /etc/systemd/system/cassandra.service
sudo mkdir -p /usr/share/oss/bin/../data/cdc
sudo chown cassandra /usr/share/oss/bin/../data/cdc
sudo systemctl daemon-reload
sudo service cassandra stop
sudo service cassandra start