package com.cassandra.samples;

import com.datastax.oss.driver.api.core.CqlSession;
import com.datastax.oss.driver.api.core.cql.ResultSet;
import org.junit.Assert;

public class CassandraConnectorTest {

    @org.junit.Test
    public void connect() throws Exception{
        CassandraConnector client = new CassandraConnector("0.0.0.0", 9042);
        CqlSession session = client.getSession();
        Assert.assertNotNull(session);
        client.executeScript("src/main/resources/schema.cql");

        ResultSet result =
                session.execute("SELECT * FROM pocdb.customers;");
        result.all()
                .stream()
                .forEach(System.out::println);

    }

    @org.junit.Test
    public void getSession() {
    }

}