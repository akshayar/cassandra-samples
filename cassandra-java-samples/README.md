## Build
```
./build.sh 
```
## Fake Data Creater
```
#export JAR_PATH=<jar-path>
export JAR_PATH=target/cassandra-java-samples.jar
## Delat in ms
export DELAY=2
## Count
export COUNT=100
#./fake-crud.sh <cassandra-conf-path> <keyspace>
./fake-crud.sh src/main/resources/source.conf pocdb
```

## Compare Records By Id 
```
#export JAR_PATH=<jar-path>
export JAR_PATH=target/cassandra-java-samples.jar
#./comare-records.sh -sconf <source-cassandra-conf> -skey <source-keyspace> -stab <source-table> \
#-tconf <target-cassandra-conf> -tkey <target-keyspace> -ttab <target-table> \
#-id <id>

./comare-records.sh -sconf src/main/resources/source.conf -skey pocdb -stab customers \
-tconf src/main/resources/target.conf -tkey pocdb1 -ttab customers \
-id 9532

```

## Compare Records By Id File
```
#export JAR_PATH=<jar-path>
export JAR_PATH=target/cassandra-java-samples.jar
#./comare-records.sh -sconf <source-cassandra-conf> -skey <source-keyspace> -stab <source-table> \
#-tconf <target-cassandra-conf> -tkey <target-keyspace> -ttab <target-table> \
#-idfile <id-file>

./comare-records.sh -sconf src/main/resources/source.conf -skey pocdb -stab customers \
-tconf src/main/resources/target.conf -tkey pocdb1 -ttab customers \
-idfile id.txt

```

1. Id file content look like . Only long id with name id supported in the table.
```
878
2754
6421
2791
327
4402
1308
```