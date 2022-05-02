# Overview
This solution consumes CDC records inserted into Apache Pulsar with Datastax CDC for Apache Cassandra (https://github.com/datastax/cdc-apache-cassandra). 
It consumes the records captured and ingests them to either a target Cassandra DB or a Amazon Keyspaces DB. It could be used for Data replication. 
# Build
1. Build code
```
export CASSANDRA_USER_NAME=<Cassandra User Name>
export CASSANDRA_PASSWORD=<Cassandra Password>
export S3_BUCKET_PATH=<bucket name>/<path>
cd <project-root>
./build-and-copy.sh ${S3_BUCKET_PATH}
```
2. This will create the NAR file and copy that to S3 bucket path you specify. 

# Deployment
## Pulsar Sink Configuration
```yaml
archive: ${CASSANDRA_SINK_NAR_PATH}
tenant: public
namespace: default
name: ${SINK_CONNECTOR_NAME}
inputs:
- ${PULSAR_DATA_TOPIC_NAME}
subs-position: Earliest
configs:
    roots : "${CASSANDRA_URL}"
    keyspace : "${CASSANDRA_KEY_SAPCE}"
    tableName : "${CASSANDRA_TABLE}"
    configFilePath: "${KEYSPACE_LOADER_CONFIG}"
    nullValueAction: "IGNORE/DELETE/FAIL", --Default IGNORE
    bulkEnabled: "true/false",  -- Default false
    malformedDocAction: "IGNORE/WARN/FAIL" --Default FAIL

```

### Example Config for Keyspaces DB
```yaml
archive: ${CASSANDRA_SINK_NAR_PATH}
tenant: public
namespace: default
name: ${SINK_CONNECTOR_NAME}
inputs:
- ${PULSAR_DATA_TOPIC_NAME}
subs-position: Earliest
configs:
    roots : "${CASSANDRA_URL}"
    keyspace : "${CASSANDRA_KEY_SAPCE}"
    tableName : "${CASSANDRA_TABLE}"
    configFilePath: "${KEYSPACE_LOADER_CONFIG}"
    nullValueAction: "DELETE"
```
## Pre-requisite
1. Create target keyspace and table.
2. Create user id and password. 
3. Create build with user id and password. The user id and password can be provided in plaintext or using secret manager in Cassandra config file. Refer [application-keyspace-user.conf](./config/cassandra-loader/application-keyspace-user.conf) for plaintext user config and [application-keyspace-secret-manager.conf](./config/cassandra-loader/application-keyspace-secret-manager.conf) config through secrets manager. For secrets manager create secret with username and password keys. Note down the name of secret you created. For example the secret created for this example is -
   ```json
    {
    "ARN": "arn:aws:secretsmanager:us-east-1:ACCOUNT:secret:keyspace-df3HNX",
    "Name": "keyspace",
    "SecretString": "{\"username\":\"USERNAME\",\"password\":\"PASSWORD\"}"
    }
    ```

4. Setup Datastax CDC for source Apache Cassandra. Refer [link](../README.md). Ensure that data topic for table with CDC configurations are created. 
5. Configure trust store
```shell
mkdir -p  $HOME/keyspace/truststore
cd $HOME/keyspace/truststore
curl https://certs.secureserver.net/repository/sf-class2-root.crt -O
```
```shell

openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der
keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der
```


## Deployment 
### One Time Configuration
1. SSH to Apache Pulsar machine. The instruction set works when connector is deployed on Apache Pulsar cluster. 
2. Download deployment sctips.
```shell
mkdir -p ${HOME}/pulsar-io-cassandra-sink-connector
cd ${HOME}/pulsar-io-cassandra-sink-connector
export S3_BUCKET_PATH=<bucket name>/<path>
aws s3 cp --recursive  s3://${S3_BUCKET_PATH}/ .
ls
```
3. Update deployment properties

```shell
cd ${HOME}/pulsar-io-cassandra-sink-connector
export CONNECTOR_HOME=${HOME}/pulsar-io-cassandra-sink-connector
export CASSANDRA_KEY_SAPCE=tutorialkeyspace
export CASSANDRA_TABLE=customers
export NAR_FILE_NAME=pulsar-io-cassandra-sink-1.0.0-SNAPSHOT.nar
cat << EOF > deploy/deploy-setup.sh
export BUILD_BUCKET=${S3_BUCKET_PATH}
export CONNECTOR_HOME=${CONNECTOR_HOME}
export SINK_CONFIG_FILE_PATH=${CONNECTOR_HOME}/config/sink-config.yaml
export KEYSPACE_LOADER_CONFIG=${CONNECTOR_HOME}/config/application-keyspace-secret-manager.conf
export NAR_FILE_NAME=${NAR_FILE_NAME}
CASSANDRA_SINK_NAR_PATH=${CONNECTOR_HOME}/${NAR_FILE_NAME}
export PULSAR_HOME=$HOME/apache-pulsar-2.9.1
## Cassandra CDC Data Topic
export PULSAR_DATA_TOPIC_NAME=persistent://public/default/data-pocdb1.customers
export CASSANDRA_URL=cassandra.us-east-1.amazonaws.com:9142
export CASSANDRA_KEY_SAPCE=${CASSANDRA_KEY_SAPCE}
export CASSANDRA_TABLE=${CASSANDRA_TABLE}
export CASSANDRA_DC_NAME=OSS-dc0
export REGION=us-east-1
export TRUSTSTORE_FILE_PATH=$HOME/keyspace/truststore/cassandra_truststore.jks
export TRUSTSTORE_PASSWORD=password #<truststore password>
export SINK_CONNECTOR_NAME=pulsar-cassandra-sink-${CASSANDRA_KEY_SAPCE}-${CASSANDRA_TABLE}
EOF
cat deploy/deploy-setup.sh
chmod +x deploy/*.sh
```
```shell
export SECRET_NAME=keyspace
cd ${HOME}/pulsar-io-cassandra-sink-connector
. ${CONNECTOR_HOME}/deploy/deploy-setup.sh
cat << EOF > config/application-keyspace-secret-manager.conf
datastax-java-driver {
        basic.contact-points = ["cassandra.us-east-1.amazonaws.com:9142"]
        basic.load-balancing-policy {
            class = DefaultLoadBalancingPolicy
            local-datacenter = us-east-1
        }
        advanced {
            auth-provider{
                    class = SecretsManagerAuthProvider
                    aws-region = us-east-1
                    aws-secret = ${SECRET_NAME}
            }
            ssl-engine-factory {
                class = DefaultSslEngineFactory
                truststore-path = "${TRUSTSTORE_FILE_PATH}"
                truststore-password = "password"
            }
        }
        basic.request.consistency = LOCAL_QUORUM 
    }
EOF
cat ${CONNECTOR_HOME}/config/application-keyspace-secret-manager.conf

```
```shell
. ${CONNECTOR_HOME}/deploy/deploy-setup.sh
cat << EOF > config/sink-config.yaml
archive: ${CASSANDRA_SINK_NAR_PATH}
tenant: public
namespace: default
name: ${SINK_CONNECTOR_NAME}
inputs:
- ${PULSAR_DATA_TOPIC_NAME}
subs-position: Earliest
configs:
  roots : "${CASSANDRA_URL}"
  keyspace : "${CASSANDRA_KEY_SAPCE}"
  tableName : "${CASSANDRA_TABLE}"
  configFilePath: "${KEYSPACE_LOADER_CONFIG}"
  nullValueAction: "DELETE"
EOF
cat ${CONNECTOR_HOME}/config/sink-config.yaml

```
### Deploy and run
1. Deploy and run
```shell
cd ${CONNECTOR_HOME}/deploy
./deploy-keyspaces-sink.sh
##./deploy-cassandra-sink.sh


```

## Validate Data in target connector



## To Do
1. User Id/Password is provided at the build time. *DONE*
2. Provide for id/password at runtime using secret manager or other method. *DONE*
3. Functionality
   - Test with sinks to multiple targets. 10
   - Find metrics above source and sink connector. 1 
   - Alert on Source connector dying. 3
   - Alert on Sink connector dying. 3
   - Alert on Sink/Source Connector failing. 3
   - A day's /hour's report on work done - records received/ingested etc. 1
   - Password in secret of source cassandra. 5
   - Find source connector for a cassandra DB and table. 1
   - Find sink connector for a cassandra DB and table. 1
   - Find sink connector which are writing to a keyspace DB / table. 1
   - Find all source and sink connectors. 1
   - CI/CD Integration with Code Pipeline and Code Build. 1
4. Resilience
   - Test on multi node Apache Pulsar Cluster. 2
   - Deployment steps for Pulsar connector on a machine separate from Apache Pulsar connector. 2
   - Test for resilience 2
     - Source cassandra dies, 
     - An Apache pulsar node dies, 
     - Source connector dies, 
     - Sink connector dies, 
     - Apache Pulsar cluster dies , 
     - Target Keyspace not available. 
5. Monitoring and Metrics 3
   - How many records were received
   - How many were ingested
   - End to end latency
   - Source Connector capacity CPU/Memory/Latency
   - Sink Connector capacity CPU/Memory/Latenct
6. Performance Testing 4
   Test for performance.
6. Add support for monitoring and metrics. Add documentation around monitoring and metrics. 

## Additonal Information
1. Dashboard | https://github.com/streamnative/apache-pulsar-grafana-dashboard/blob/master/gettingStarted.md

