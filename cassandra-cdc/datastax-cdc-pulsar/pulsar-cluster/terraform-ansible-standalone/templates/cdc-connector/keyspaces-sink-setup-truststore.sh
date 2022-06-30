#!/bin/bash
curl https://certs.secureserver.net/repository/sf-class2-root.crt -O
openssl x509 -outform der -in sf-class2-root.crt -out temp_file.der
keytool -import -alias cassandra -keystore cassandra_truststore.jks -file temp_file.der
