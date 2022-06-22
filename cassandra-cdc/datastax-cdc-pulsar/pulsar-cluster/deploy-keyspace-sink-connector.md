# Deploy Pulsar sink connector for Keyspaces database
1. Deploy Pulsar sink connector for Keyspaces database. The connector will start pushing changes captured on data-<keyspace-name>.<table-name> topic to Keyspaces database.
2. Prepare Keyspaces database.
    1. Create target keyspace and table.
    ```shell
      aws keyspaces create-keyspace --keyspace-name <keyspace-name>
    ```
    2. Generate service specific credentials. Refer https://docs.aws.amazon.com/keyspaces/latest/devguide/programmatic.credentials.ssc.html . 
    ```shell
    USER_NAME=<user-name>
    aws iam create-user --user-name ${USER_NAME}
   
    SECRET_STRING=`aws iam create-service-specific-credential \
    --user-name ${USER_NAME} \
    --service-name cassandra.amazonaws.com --query ServiceSpecificCredential`
    SERVICE_USER_NAME=`echo $SECRET_STRING | jq -r  .ServiceUserName`
    SERVICE_USER_PASSWORD=`echo $SECRET_STRING | jq -r  .ServicePassword`
    ```
   3. Create secret in AWS Secret Manager. 
    ```shell
     aws secretsmanager create-secret \
        --name keyspace-secret \
        --description "Keyspace Secret for Keyspace Sink Connector." \
        --secret-string "{\"ServiceUserName\":\"$SERVICE_USER_NAME\",\"ServicePassword\":\"${SERVICE_USER_PASSWORD}\"}"
    ```
3. Create IAM permissions to write to keyspace table.
4. Deploy Keyspace Sink Connector. 
   1. Create truststore. 
   ```shell
   mkdir ../keystore ; cd ../keystore
   chmod +x ../templates/cdc-connector/*.sh
   ../templates/cdc-connector/keyspaces-sink-setup-truststore.sh 
   ```
   2. 


