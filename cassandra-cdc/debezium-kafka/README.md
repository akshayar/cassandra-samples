# Architectural Components
1. Apache Kafka Cluster.
2. Debezium process which runs on each of the Cassandra node, reads commit logs and pushes to Kafka. Debezium process is deployed on each Cassandra node and started as a separate process from Cassandra process. The process will create a topic for each table which will look like <topic-prefix>.keyspce.table.
3. Down stream data consumers. These could be Apache Kafka Sink Connector for ElasticSearch  or Apache Spark Process with Hudi etc. 

#  Deployment
## Apache Kafka Cluster
## Debezium Process on each Cassandra Node
