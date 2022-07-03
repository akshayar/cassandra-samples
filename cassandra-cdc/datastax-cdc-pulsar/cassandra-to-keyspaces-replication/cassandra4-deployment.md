# Deploying Cassandra
1. For Apache Cassandra deployment we are following this [link](https://aws-quickstart.github.io/quickstart-datastax-oss/).  
2. Refer to the [overview](https://aws-quickstart.github.io/quickstart-datastax-oss/#_overview) and ensure that the deployment is supported in your region. 
3. Use quick start [link](https://aws-quickstart.github.io/quickstart-datastax-oss/#_launch_the_quick_start) to launch Cassandra 4 cluster into an existing VPC or a new VPC.
4. Vaidate the deployment by connecting to a node of the cluster through bastion node ( jump box) and executing `nodetool status`
5. Upload SSH keys to Cloud9 and change permission to 400.
6. Create cassandra.ini file that lists all Cassandra servers.
5. Enable CDC by modifying /usr/share/oss/conf/cassandra.yaml and adding/updating following properties. 
```shell
cdc_enabled: false
cdc_total_space_in_mb: 4096
cdc_free_space_check_interval_ms: 250
cdc_raw_directory: /var/lib/cassandra/cdc_raw
```
6. Run following ansible commands to enable CDC
```shell
cd ${AWS_DEPLOYMENT_HOME}
export CASSANDRA_INI_FILE=cassandra.ini
cat << EOF > ${CASSANDRA_INI_FILE}
[cassandra]
<cassandra-1>
<cassandra-2>
<cassandra-3>
EOF
export CASSANDRA_KEY_FILE=<path-key-file>
chmod 400 ${CASSANDRA_KEY_FILE}
ansible-playbook   --user='ubuntu'   --inventory=${CASSANDRA_INI_FILE} --extra-vars='{"ansible_ssh_private_key_file":"'${CASSANDRA_KEY_FILE}'"}'  ../cassandra-cluster-enable-cdc.yaml
```

