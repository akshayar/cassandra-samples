package com.aksh.cassandra.samples;

import com.datastax.driver.core.Cluster;
import com.datastax.driver.core.Session;

import java.io.BufferedReader;
import java.io.FileReader;

public class CassandraConnector {

    private Cluster cluster;

    private Session session;

    public void connect(String node, Integer port) {
        Cluster.Builder b = Cluster.builder().addContactPoint(node);
        if (port != null) {
            b.withPort(port);
        }
        cluster = b.build();

        session = cluster.connect();
    }

    public Session getSession() {
        return this.session;
    }

    public void close() {
        session.close();
        cluster.close();

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
