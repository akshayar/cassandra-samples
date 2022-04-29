## 1. Deploy Elastic Search Cluster
1. Single node cluster using docker.
2. SSH to the Amazon Linux 2 EC2 node where you want to deploy and install docker
```shell
sudo yum update -y
sudo amazon-linux-extras install docker
sudo service docker start
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user
```
3. Reboot the node and SSH back in.
```shell
docker run -d -p 9200:9200 -p 9300:9300     -e "discovery.type=single-node"     docker.elastic.co/elasticsearch/elasticsearch:7.13.3

curl -s http://localhost:9200 
```
## 2.  Create Apache Pulsar Elastic Search Sink Connector
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
ES_URL=http://10.0.138.240:9200
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
         \"elasticSearchUrl\":\"$ES_URL\",
          \"indexName\":\"pocdb.customers\",
         \"keyIgnore\":\"false\",
         \"schemaEnable\":\"true\"
}"
bin/pulsar-admin sink status --name es-sink-pocdb-customers
```

# Test the solution end to end
1. Run sample program to insert data in Cassandra.
2. Validate that CDC logs are created and consumed
```shell
## SSH to Cassandra node and run
 watch -n 1 ls /var/lib/cassandra/cdc_raw/
### You should see CommitLog files for brief period before they are consumed. 
```
3. Validate that CDC events are pushed to Apache Pulsar events topic
```shell
### SSH to Apache Pulsar Node
bin/pulsar-client --url pulsar://10.0.134.80:6650 consume events-pocdb.customers -s "first-subscription"  -n 100
```
4. Validate that Cassandra Source Connector is pushing data to data topic.
```shell
### SSH to Apache Pulsar Node
bin/pulsar-client --url pulsar://10.0.134.80:6650 consume data-pocdb.customers -s "first-subscription"  -n 100
```
5. Validate that ES sink connector is pushing data to ES.
```shell
## ec2-3-87-65-10.compute-1.amazonaws.com is the node where ES is installed
http://ec2-3-87-65-10.compute-1.amazonaws.com:9200/pocdb.customers/_search?q=gmail.updated
```
