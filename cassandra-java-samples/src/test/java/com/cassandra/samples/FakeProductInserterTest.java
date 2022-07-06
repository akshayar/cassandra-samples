package com.cassandra.samples;

import com.datastax.oss.driver.api.core.CqlSession;

import java.util.Random;
import java.util.stream.IntStream;

public class FakeProductInserterTest {
    @org.junit.Test
    public void insertFakeProduct() throws Exception{
        FakeProductInserter inserter=new FakeProductInserter("pocdb1");
        CassandraConnector client = new CassandraConnector("0.0.0.0", 9042);

        CqlSession session = client.getSession();
        Random random=new Random();

        IntStream.range(1,1000).parallel().forEach(i->{
            try{
                if(random.nextBoolean()){
                    System.out.println("inserting "+i);
                    inserter.insertProduct(session);

                    inserter.insertCustomer(session);
                }else{
                    System.out.println("updating " +i);
                    inserter.updateProduct(session);

                    inserter.updateCustomer(session);
                }

            }catch (Exception e){
                e.printStackTrace();
            }

        });

    }

}