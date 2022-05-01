package com.aksh.cassandra.samples;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Session;
import org.junit.Assert;

public class CassandraConnectorTest {

    @org.junit.Test
    public void connect() throws Exception{
        CassandraConnector client = new CassandraConnector();
        client.connect("3.84.250.3", 9042);
        Session session = client.getSession();
        Assert.assertNotNull(session);
        client.executeScript("src/main/resources/schema.cql");

        ResultSet result =
                session.execute("SELECT * FROM pocdb1.customers;");
        result.all()
                .stream()
                .forEach(System.out::println);

    }

    @org.junit.Test
    public void getSession() {
    }

}