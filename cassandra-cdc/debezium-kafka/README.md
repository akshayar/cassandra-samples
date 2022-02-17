# Architectural Components
1. Apache Kafka Cluster.
2. Debezium process which runs on each of the Cassandra node, reads commit logs and pushes to Kafka. Debezium process is deployed on each Cassandra node and started as a separate process from Cassandra process. The process will create a topic for each table which will look like <topic-prefix>.keyspce.table.
3. Down stream data consumers. These could be Apache Kafka Sink Connector for ElasticSearch  or Apache Spark Process with Hudi etc. 

#  Deployment
## Apache Kafka Cluster
1. Install docker
```shell
 sudo amazon-linux-extras install docker
 sudo service docker start
 sudo usermod -a -G docker ec2-user
 sudo chkconfig docker on
 sudo reboot
```
2. Install Docker Compose
```shell
sudo curl -L https://github.com/docker/compose/releases/download/1.22.0/docker-compose-$(uname -s)-$(uname -m) -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose
docker-compose version

```
3. Deploy and start Kafka.
```shell
mkdir kafka
cd kafka/
export NODE_IP_ADDRESS=<private IP>
cat << EOF > docker-compose.yml
---
version: '3'

services:
  zookeeper:
    image: confluentinc/cp-zookeeper:6.0.0
    hostname: zookeeper
    container_name: zookeeper
    ports:
      - "32181:32181"
    environment:
      ZOOKEEPER_CLIENT_PORT: 32181
      ZOOKEEPER_TICK_TIME: 2000

  kafka:
    image: confluentinc/cp-enterprise-kafka:6.0.0
    hostname: kafka
    container_name: kafka
    depends_on:
      - zookeeper
    ports:
      - "9092:9092"
    environment:
      KAFKA_BROKER_ID: 1
      KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:32181'
      KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
      KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://kafka:29092,PLAINTEXT_HOST://$NODE_IP_ADDRESS:9092
      KAFKA_INTER_BROKER_LISTENER_NAME: PLAINTEXT
      KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
      KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
      KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
      KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
    working_dir: /data
    volumes:
      - ./data:/data

  schema-registry:
    image: confluentinc/cp-schema-registry:6.0.0
    depends_on:
      - zookeeper
      - kafka
    environment:
      SCHEMA_REGISTRY_HOST_NAME: schema-registry
      SCHEMA_REGISTRY_KAFKASTORE_CONNECTION_URL: zookeeper:32181
    ports:
      - "8081:8081"
EOF
nohup docker-compose up &
```
4. Validate that Kafka works
```shell
docker-compose -f docker-compose.yml exec kafka /kafka/bin/kafka-topics.sh --list --bootstrap-server kafka:9092
```
## Debezium Process on each Cassandra Node
1. Download Debezium jar. 
```shell
mkdir debezium
cd debezium/
wget https://repo1.maven.org/maven2/io/debezium/debezium-connector-cassandra/1.8.0.Final/debezium-connector-cassandra-1.8.0.Final-jar-with-dependencies.jar
```
2. Copy cassandra.yaml and do local modification which will be used to run the debezium process. Update endpoint_snitch and set it to SimpleSnitch `endpoint_snitch: SimpleSnitch`. Refer https://groups.google.com/g/debezium/c/CadC1959t-M 
```shell
cp /usr/share/oss/conf/cassandra.yaml cassandra-backup.yaml
cp /usr/share/oss/conf/cassandra.yaml cassandra-tricked.yaml
## Edit cassandra-tricked.yaml and set - endpoint_snitch: SimpleSnitch
```
3. Create Debezium configuration.
```shell
cd $HOME/debezium
KAFKA_BOOT_STRAP_SERVER=ip-10-0-131-0.ec2.internal:9092
cat << EOF > config.properties
connector.name=test_connector
commit.log.relocation.dir=/data/cassandra/lib/cassandra/debezium/relocation/
http.port=8000

cassandra.config=/home/cassandra/debezium/cassandra-tricked.yaml
cassandra.hosts=127.0.0.1
cassandra.port=9042

kafka.producer.bootstrap.servers=$KAFKA_BOOT_STRAP_SERVER
kafka.producer.retries=3
kafka.producer.retry.backoff.ms=1000
kafka.topic.prefix=test_prefix

key.converter=org.apache.kafka.connect.json.JsonConverter
value.converter=org.apache.kafka.connect.json.JsonConverter

offset.backing.store.dir=/data/cassandra/lib/cassandra/debezium/offsets

snapshot.consistency=ONE
snapshot.mode=ALWAYS
EOF
```
4. Create Log configuration
```shell
cat << EOF > log4j.properties
log4j.rootLogger=INFO, A1

log4j.appender.A1=org.apache.log4j.ConsoleAppender

log4j.appender.A1.layout=org.apache.log4j.PatternLayout
log4j.appender.A1.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n
EOF
```
4. Create start up script
```shell
cat << EOF > startup-script.sh
#!/bin/sh
export DEBEZIUM_HOME=`pwd`

nohup java -Dlog4j.debug -Dlog4j.configuration=file:$DEBEZIUM_HOME/log4j.properties -jar $DEBEZIUM_HOME/debezium-connector-cassandra.jar $DEBEZIUM_HOME/config.properties > /var/log/cassandra/nohup.out 2>&1 &

echo "view logs at /var/log/cassandra/nohup.out"
EOF
```
5. Run startup script
```shell
sudo su cassandra
chmod +x startup-script.sh
./startup-script.sh
tail -f /var/log/cassandra/nohup.out
```