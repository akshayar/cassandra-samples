/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.io.cassandra.dao;

import java.io.File;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.pulsar.client.api.schema.GenericObject;
import org.apache.pulsar.functions.api.Record;
import org.apache.pulsar.io.cassandra.CassandraSinkConfig;
import org.apache.pulsar.io.cassandra.JsonConverter;

import com.amazonaws.util.StringUtils;
import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.DriverExecutionProfile;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.SimpleStatement;
import com.datastax.oss.driver.api.querybuilder.QueryBuilder;
import com.datastax.oss.driver.api.querybuilder.relation.Relation;
import com.datastax.oss.driver.internal.core.auth.SecretsManagerAuthProvider;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * @author rawaaksh
 *
 */
@Slf4j
public class CassandraDao implements AbstractCassandraDao {
    private CassandraSinkConfig cassandraSinkConfig;
    private CqlSession session;
    PreparedStatement insertUpdate;
    Map<Set<String>,PreparedStatement> delete =new HashMap<>();
    
    private static final String CASSANDRA_UPDATE_QUERY="INSERT INTO {0}.{1} JSON :payload";


    
    public CassandraDao(CassandraSinkConfig cassandraSinkConfig) {
    	log.info("Creating Cassandra connection with {}",cassandraSinkConfig);
        this.cassandraSinkConfig = cassandraSinkConfig;
        createClient();
    }
    
    @Override
	public void close() {
    	session.close();
    }

    @Override
	public void upsertRecord(Record<GenericObject> record, Pair<JsonNode, JsonNode> idAndDoc) {
        try {
			log.info("Upsert Record key:{},value:{}",idAndDoc.getLeft(),idAndDoc.getRight());
			JsonNode finalJson=JsonConverter.createSingle(idAndDoc.getLeft(),idAndDoc.getRight());
			session.execute(insertUpdate.bind().setString("payload",   finalJson.toString()));
			record.ack();
		} catch (Exception e) {
			log.error("Failed while inserting id:{}",idAndDoc.getLeft(),e);
			record.fail();
			throw e;
		}
        
    }

    @Override
	public void bulkDelete(Record<GenericObject> record, JsonNode left) {
        log.info("Bulk Delete Record key:{}",left);
    }

    @Override
	public void bulkUpsert(Record<GenericObject> record, Pair<JsonNode, JsonNode> idAndDoc) {
        log.info("Bulk Upsert Record key:{},value:{}",idAndDoc.getLeft(),idAndDoc.getRight());
    }

	@Override
	public void deleteRecord(Record<GenericObject> record, JsonNode left) {
		try {
			log.info("Delete Record key:{}", left);
			Set<String> idFieldNames = new HashSet<>();
			left.fieldNames().forEachRemaining(idFieldNames::add);

			if (!delete.containsKey(idFieldNames)) {
				PreparedStatement deleteP = createPreparedStatementForDelete(idFieldNames);
				delete.put(idFieldNames, deleteP);
			}

			PreparedStatement preparedStatement = delete.get(idFieldNames);

			BoundStatement boundStatement = preparedStatement.bind();
			for (String fn : idFieldNames) {
				boundStatement = setValue(boundStatement, left.get(fn), fn + "col");
			}

			session.execute(boundStatement);
			record.ack();
		} catch (Exception e) {
			e.printStackTrace();
			log.error("Failed while inserting id:{}", left, e);
			record.fail();
			throw e;
		}
	}

	private PreparedStatement createPreparedStatementForDelete(Set<String> idFieldNames) {
		List<Relation> list=new ArrayList<>();
		idFieldNames.stream().forEach(fn->{
			list.add(Relation.column(fn).isEqualTo(QueryBuilder.bindMarker(fn+"col")));
		});
		
		
		SimpleStatement statement=QueryBuilder.deleteFrom("customers").where(list).build();
		log.info("statement:"+statement.getQuery());
		PreparedStatement deleteP=session.prepare(statement);
		return deleteP;
	}

    private BoundStatement setValue(BoundStatement boundStatement, JsonNode node,String name){
        log.info("node:"+node+",name:"+name);
        BoundStatement result=null;
        if(node.isBigDecimal()){
            result=boundStatement.setBigDecimal(name,node.decimalValue());
        }else if(node.isBigInteger()){
            result=boundStatement.setBigInteger(name,node.bigIntegerValue());
        }else if(node.isBinary()){
            //boundStatement.setbyte(name,node.bigIntegerValue());
        }else if(node.isBoolean()){
            result=boundStatement.setBoolean(name,node.booleanValue());
        }else if(node.isDouble()){
            result=boundStatement.setDouble(name,node.doubleValue());
        }else if(node.isFloat()){
            result=boundStatement.setFloat(name,node.floatValue());
        }else if(node.isInt()){
            result=boundStatement.setInt(name,node.intValue());
        }else if(node.isLong()){
            result=boundStatement.setLong(name,node.longValue());
        }else if(node.isShort()){
            result=boundStatement.setShort(name,node.shortValue());
        }else if(node.isTextual()){
            result=boundStatement.setString(name,node.textValue());
        }else{
            result=boundStatement.setString(name,node.textValue());
        }
        return result;
    }
    private void createClient() {

		log.info("Starting Initializing for roots:{},datacenter:{},keyspace:{},table:{},region:{},configPath:{}", cassandraSinkConfig.getRoots(), cassandraSinkConfig.getDcName(),
				cassandraSinkConfig.getKeyspace(), cassandraSinkConfig.getTableName(),cassandraSinkConfig.getRegion(),cassandraSinkConfig.getConfigFilePath());

        String[] hosts = Optional.ofNullable(cassandraSinkConfig.getRoots()).map(s->s.split(",")).orElse(new String[0]);
		CqlSessionBuilder sessionBuilder = Arrays.stream(hosts).reduce(CqlSession.builder(), (sessionb, host) -> {
			String[] hostPort = host.split(":");
			log.info("Host name:{}, port:{}",hostPort[0],hostPort[1]);
			return sessionb.addContactPoint(new InetSocketAddress(hostPort[0], Integer.valueOf(hostPort[1])));
		}, (b1, b2) -> {
			return Optional.ofNullable(b1).orElse(b2);
		});

		session = Optional.ofNullable(cassandraSinkConfig.getConfigFilePath())
				.filter(file -> !StringUtils.isNullOrEmpty(file))
				.map(file -> {
					log.info("Building session from config file :{}",file);
					Optional<JsonNode> credentials=Optional.empty();//Commenting to short circuit//getSecretFromConfig(file);
					if(credentials.isPresent()) {
						CqlSession sessionIn= sessionBuilder.withKeyspace(cassandraSinkConfig.getKeyspace())
								.withConfigLoader(
										DriverConfigLoader.fromFile(new File(file)))
								.withAuthCredentials(credentials.get().get("username").asText(), credentials.get().get("password").asText())
								.build();
						return sessionIn;	
					}else {
						CqlSession sessionIn= sessionBuilder.withKeyspace(cassandraSinkConfig.getKeyspace())
								.withConfigLoader(
										DriverConfigLoader.fromFile(new File(file)))
								.build();
						return sessionIn;
					}
					
					
				}).filter(Objects::nonNull)
				.orElseGet(()->{
					return buildSessionFromDefaultConfig(sessionBuilder,hosts);
				});
		

        
		insertUpdate = session.prepare(MessageFormat.format(CASSANDRA_UPDATE_QUERY, cassandraSinkConfig.getKeyspace(),
				cassandraSinkConfig.getTableName()));
		log.info("Done Initializing for roots:{},datacenter:{},keyspace:{},table:{}", cassandraSinkConfig.getRoots(), cassandraSinkConfig.getDcName(),
				cassandraSinkConfig.getKeyspace(), cassandraSinkConfig.getTableName());

    }
    
	private CqlSession buildSessionFromDefaultConfig(CqlSessionBuilder sessionBuilder, String[] hosts) {
		log.info("Loader config not provided , building session config in classpath isKeyspace:{}",cassandraSinkConfig.isKeyspacedb());
		if (hosts.length <= 0) {
			throw new RuntimeException("Invalid cassandra roots");
		}
		if (cassandraSinkConfig.isKeyspacedb()) {
			return sessionBuilder.withKeyspace(cassandraSinkConfig.getKeyspace())
					.withConfigLoader(DriverConfigLoader.fromClasspath("application-keyspace.conf"))
					.withLocalDatacenter(cassandraSinkConfig.getRegion()).build();

		} else {
			return sessionBuilder.withKeyspace(cassandraSinkConfig.getKeyspace())
					.withConfigLoader(DriverConfigLoader.fromClasspath("application-cassandra.conf"))
					.withLocalDatacenter(cassandraSinkConfig.getDcName()).build();

		}
	}
	
	private Optional<JsonNode> getSecretFromConfig(String file) {
		DriverExecutionProfile profile=DriverConfigLoader.fromFile(new File(file)).getInitialConfig().getDefaultProfile();
		JsonNode node=null;
		if(profile.isDefined(SecretsManagerAuthProvider.SECRET_MANAGER_ID_OPTION)) {
			String secret=profile.getString(SecretsManagerAuthProvider.SECRET_MANAGER_ID_OPTION);
			String region=profile.getString(SecretsManagerAuthProvider.SECRET_MANAGER_REGION_OPTION);
			log.info("region:{},secret:{}",region,secret);
			node=SecretsManagerAuthProvider.getSecret(secret, region);	
		}
		
		return Optional.ofNullable(node);
	}
    
}
