package com.cassandra.samples;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.cql.BoundStatement;
import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import com.datastax.oss.driver.api.core.cql.Row;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.text.MessageFormat;
import java.util.logging.Logger;

public class GetDataFromCassandra {
    private String configPath;
    private String keyspace;
    private String table;
    private String SELECT_QUERY="SELECT JSON * FROM {0}.{1} WHERE id = :id";
    PreparedStatement selectStatement;
    private static final org.slf4j.Logger log= LoggerFactory.getLogger(GetDataFromCassandra.class);

    private CqlSession session=null;

    public GetDataFromCassandra(String configPath, String keyspace, String table) {
        this.configPath = configPath;
        this.keyspace = keyspace;
        this.table = table;
        buildCqlSession();
    }

    private void buildCqlSession(){
        CqlSessionBuilder sessionBuilder = CqlSession.builder();

        session= sessionBuilder.withKeyspace(keyspace)
                .withConfigLoader(
                        DriverConfigLoader.fromFile(new File(configPath)))
                .build();
        selectStatement = session.prepare(MessageFormat.format(SELECT_QUERY, keyspace, table));
        log.info("Done Initializing for keyspace:{},table:{}", keyspace,table);
    }

    public String executeForId(String id){
        BoundStatement boundStatement = selectStatement.bind();
        ResultSet resultSet= session.execute(boundStatement.setLong("id", Long.valueOf(id)));
        Row row=resultSet.one();
        String result="";
        if(row!=null){
            result=row.getString(0);
            log.debug(result);
        }

        return result;
    }

}
