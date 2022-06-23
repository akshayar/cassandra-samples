package com.aksh.cassandra.samples;

import com.datastax.driver.core.Session;

import java.util.Random;
import java.util.stream.IntStream;

public class FakeProductInserterTest {
    @org.junit.Test
    public void insertFakeProduct() throws Exception{
        FakeProductInserter inserter=new FakeProductInserter();
        CassandraConnector client = new CassandraConnector();
        client.connect("44.203.122.78", 9042);
        Session session = client.getSession();
        Random random=new Random();

        IntStream.range(1,1000).parallel().forEach(i->{
            try{
                if(random.nextBoolean()){
                    System.out.println("inserting "+i);
                   // inserter.insertProduct(session);

                    inserter.insertCustomer(session,"pocdb1");
                }else{
                    System.out.println("updating " +i);
                    inserter.updateProduct(session,"pocdb1");

                    inserter.updateCustomer(session,"pocdb1");
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        });

    }

}