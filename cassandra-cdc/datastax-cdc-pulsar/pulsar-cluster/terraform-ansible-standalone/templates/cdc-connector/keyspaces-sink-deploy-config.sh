export PULSAR_HOME={{ pulsar_home }}
export CONNECTOR_HOME={{ pulsar_home }}/pulsar-io-cassandra-sink-connector
export SINK_CONFIG_FILE_PATH={{ pulsar_home }}/pulsar-io-cassandra-sink-connector/config/sink-config.yaml
export KEYSPACE_LOADER_CONFIG={{ pulsar_home }}/pulsar-io-cassandra-sink-connector/config/application-keyspace-secret-manager.conf
export TRUSTSTORE_FILE_PATH={{ pulsar_home }}/pulsar-io-cassandra-sink-connector/truststore/cassandra_truststore.jks
export NAR_FILE_NAME={{ keyspaces_sink_nar_file }}
export KEYSPACES_SINK_NAR_PATH={{ pulsar_home }}/pulsar-io-cassandra-sink-connector/{{ keyspaces_sink_nar_file }}
export TRUSTSTORE_PASSWORD={{ truststore_password }} #<truststore password>
## Keyspace Config
export REGION={{ region }}
export KEYSPACES_URL=cassandra.{{ region }}.amazonaws.com:9142