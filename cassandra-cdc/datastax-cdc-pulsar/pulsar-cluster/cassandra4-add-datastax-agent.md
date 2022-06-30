# Update Cassandra to install change agent for Apache Cassandra
1. Deploy DataStax change agent for Apache Cassandra and enable capture of CDC. This process will start pushing changes from Cassandra to events-<keyspace-name>.<table-name> topic on Apache Pulsar. 
2. Upload SSH keys to Cloud9 and change permission to 400.
3. Create cassandra.ini file that lists all Cassandra servers.  
4. Run following commands to install DataStax agent and restart Cassandra service - 
```shell
cd ${AWS_DEPLOYMENT_HOME}
export CASSANDRA_INI_FILE=<path to init file>
export CASSANDRA_KEY_FILE=<path-key-file>
chmod 400 ${CASSANDRA_KEY_FILE}
ansible-playbook   --user='ubuntu'   --inventory=${CASSANDRA_INI_FILE} --extra-vars='{"ansible_ssh_private_key_file":"'${CASSANDRA_KEY_FILE}'","pulsar_service_url":"'${PULSAR_SERVICE_VALUE}'"}'  ../cassandra-cluster-update-cdc-agent.yaml
```
