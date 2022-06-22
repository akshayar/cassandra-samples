package com.aksh.cassandra.samples;

import com.datastax.driver.core.Session;

import java.util.Random;
import java.util.stream.IntStream;

public class FakeProductInserterTest {
    @org.junit.Test
    public void insertFakeProduct() throws Exception{
        FakeProductInserter inserter=new FakeProductInserter();
        CassandraConnector client = new CassandraConnector();
        client.connect("52.202.210.103", 9042);
        Session session = client.getSession();
        Random random=new Random();

        IntStream.range(1,1000).parallel().forEach(i->{
            try{
                if(random.nextBoolean()){
                    System.out.println("inserting "+i);
                   // inserter.insertProduct(session);

                    inserter.insertCustomer(session);
                }else{
                    System.out.println("updating " +i);
                    //inserter.updateProduct(session);

                  //  inserter.updateCustomer(session);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        });

    }

}