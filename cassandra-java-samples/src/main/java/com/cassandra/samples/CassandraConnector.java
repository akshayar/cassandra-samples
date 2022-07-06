package com.cassandra.samples;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.CqlSessionBuilder;
import com.datastax.oss.driver.api.core.config.DefaultDriverOption;
import com.datastax.oss.driver.api.core.config.DriverConfigLoader;
import com.datastax.oss.driver.api.core.config.DriverOption;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.InetSocketAddress;
import java.text.MessageFormat;
import java.util.Arrays;

public class CassandraConnector {

    private CqlSession session;


    public CassandraConnector(String keysapce, String configPath) {
        buildCqlSession(keysapce,DriverConfigLoader.fromFile(new File(configPath)));
    }
    public CassandraConnector(String server,int port,String datacenter) {
        DriverConfigLoader configLoader=DriverConfigLoader.programmaticBuilder()
                .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(server+":"+port))
                .build();
        buildCqlSession(configLoader,datacenter);
    }

    public CassandraConnector(String server,int port) {
        DriverConfigLoader configLoader=DriverConfigLoader.programmaticBuilder()
                .withStringList(DefaultDriverOption.CONTACT_POINTS, Arrays.asList(server+":"+port))
                .build();
        buildCqlSession(configLoader,"datacenter1");
    }

    private void buildCqlSession(DriverConfigLoader configLoader,String datacenter){
        CqlSessionBuilder sessionBuilder = CqlSession.builder();

        session= sessionBuilder
                .withLocalDatacenter(datacenter)
                .withConfigLoader(configLoader)
                .build();
    }

    private void buildCqlSession(String keyspace, DriverConfigLoader configLoader){
        CqlSessionBuilder sessionBuilder = CqlSession.builder();

        session= sessionBuilder.withKeyspace(keyspace)
                .withConfigLoader(configLoader)
                .build();
    }

    public CqlSession getSession() {
        return this.session;
    }

    public void close() {
        session.close();
    }

    public void executeScript(String file) throws Exception{
        BufferedReader reader=new BufferedReader(new FileReader(file));
        String line=null;
        StringBuffer buffer=new StringBuffer();
        do{
            line=reader.readLine();
            if(line!=null) {
                if(line.trim().endsWith(";")){
                    buffer.append(line);
                    System.out.println(buffer);
                    session.execute(buffer.toString());
                    buffer=new StringBuffer();
                }else{
                    buffer.append(line);
                    buffer.append("\n");
                }
            }
        }
        while(line!=null);

    }



}
