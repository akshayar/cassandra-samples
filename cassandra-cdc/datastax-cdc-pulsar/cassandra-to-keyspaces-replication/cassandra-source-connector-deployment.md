# Deploy Datastax Source connector for Apache Cassandra
1. Deploy Datastax Source connector for Apache Pulsar which consumes events-<keyspace-name>.<table-name> topic and pushes changed data to data-<keyspace-name>.<table-name> topic.
2. Update parameters files terraform-ansible/parameters/cassandra-config.json. 
3. Check if event topic is created and consuming event. The event topic name should look like - "persistent://public/default/events-<keyspace>.<table>"

```
cd ~/environment/pulsar-client
bin/pulsar-admin --admin-url $PULSAR_ADMIN_URL  topics list public/default

```
4. Run following commands to deploy Datastax source connector for Apache Pulsar - 
```shell
cd ${AWS_DEPLOYMENT_HOME}
TF_STATE=./ TF_KEY_NAME=private_ip  ansible-playbook   --user='ec2-user' --inventory=~/environment/terraform-inventory --extra-vars="@../parameters/cassandra-config.json"  ../cassandra-source-connector-deploy.yaml

TF_STATE=./ TF_KEY_NAME=private_ip  ansible-playbook   --user='ec2-user' --inventory=~/environment/terraform-inventory --extra-vars="@../parameters/cassandra-config.json"  ../cassandra-source-connector-create.yaml

```
4. Check if data topic is created. 
```
cd ~/environment/pulsar-client
bin/pulsar-admin --admin-url $PULSAR_ADMIN_URL  topics list public/default

```