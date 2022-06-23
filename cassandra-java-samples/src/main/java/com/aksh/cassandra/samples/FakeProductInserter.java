package com.aksh.cassandra.samples;

import com.datastax.driver.core.PreparedStatement;
import com.datastax.driver.core.Session;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;

public class FakeProductInserter {
    Faker faker=new Faker();
    private Map<String,PreparedStatement> map=new HashMap<>();
    private BlockingQueue<Long> prodIdQueue=new LinkedBlockingDeque<>();
    private BlockingQueue<Long> custIdQueue=new LinkedBlockingDeque<>();

    private static final String PRODUCT_INSERT_CQL="INSERT INTO #KEYSPACE#.#TABLE#(id,name,description,weight)" +
            " VALUES (?,?,?,?);";
    private static final String PRDUCT_IN_HAND_INSERT="INSERT INTO #KEYSPACE#.#TABLE#(product_id, quantity) VALUES (?,?);";
    public void insertProduct(Session session, String keyspace, String table) throws Exception{
        String queryUpdate=PRODUCT_INSERT_CQL.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","products");
        if(map.get(queryUpdate) ==null){
            map.put(queryUpdate,session.prepare(queryUpdate));
        }

        String queryUpdateInHand=PRDUCT_IN_HAND_INSERT.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","products_on_hand");
        if(map.get(queryUpdateInHand) ==null){
            map.put(queryUpdateInHand,session.prepare(queryUpdateInHand));
        }

        String name=faker.name().name();
        long id=faker.number().numberBetween(1l,10000l);
        boolean success=prodIdQueue.offer(id,5, TimeUnit.SECONDS);
        if(success){
            PreparedStatement preparedStatement=map.get(queryUpdate);
            session.execute(preparedStatement.bind(id,name,faker.regexify("[a-z1-9]{10} "+name),Float.parseFloat(faker.numerify("##"))));

            PreparedStatement preparedStatement2=map.get(queryUpdateInHand);
            session.execute(preparedStatement2.bind(id,faker.number().numberBetween(1l,10000l)));
        }

    }
    private static final String PRODUCT_UPDATE_CQL="UPDATE  #KEYSPACE#.#TABLE# SET description =?" +
            " WHERE id =?;";
    private static final String PRDUCT_IN_HAND_UPDATE="UPDATE  #KEYSPACE#.#TABLE# SET quantity =?" +
            " WHERE product_id =?;";

    public void updateProduct(Session session , String keyspace) throws InterruptedException{
        String queryUpdate=PRODUCT_UPDATE_CQL.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","products");
        if(map.get(queryUpdate) ==null){
            map.put(queryUpdate,session.prepare(queryUpdate));
        }
        String queryUpdateInHand=PRDUCT_IN_HAND_UPDATE.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","products_on_hand");
        if(map.get(queryUpdateInHand) ==null){
            map.put(queryUpdateInHand,session.prepare(queryUpdateInHand));
        }
        Long id=prodIdQueue.poll(5,TimeUnit.SECONDS);

        if(id!=null){

            session.execute(map.get(queryUpdate).bind(faker.regexify("[a-z1-9]{10} "),id));
            session.execute(map.get(queryUpdateInHand).bind(faker.number().numberBetween(1l,10000l),id));
        }
    }

    private static final String CUSTOMER_INSERT_CQL="INSERT INTO #KEYSPACE#.#TABLE#(id,first_name,last_name,email,insertdate)\n" +
            "  VALUES (?,?,?,?,?);";


    public void insertCustomer(Session session, String keyspace) throws  InterruptedException{
        String query=CUSTOMER_INSERT_CQL.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","customers");
        if(map.get(query) ==null){
            map.put(query,session.prepare(query));
        }
        PreparedStatement preparedStatement=map.get(query);
        Name name=faker.name();
        String email = faker.bothify("????##@gmail.com");
        String date=new Date()+"";
        long id=faker.number().numberBetween(1l,10000l);
        boolean success=custIdQueue.offer(id,5, TimeUnit.SECONDS);
        if(success){
            System.out.println(date);
            session.execute(preparedStatement.bind(id,name.firstName(),name.lastName(),email,date));
        }
        System.out.println("id:"+id);


    }

    private static final String CUSTOMER_UPDATE_CQL="UPDATE  #KEYSPACE#.#TABLE# set email =? " +
            "  WHERE id=?;";

    public void updateCustomer(Session session , String keyspace ) throws  InterruptedException{
        String query=CUSTOMER_UPDATE_CQL.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","customers");
        if(map.get(query) ==null){
            map.put(query,session.prepare(query));
        }


        Long id=custIdQueue.poll(5, TimeUnit.SECONDS);
        if(id!=null){
            String email=faker.bothify("????##@gmail.updated");
            System.out.println("id="+id+",email="+email);
            session.execute(map.get(query).bind(email,id));
        }

    }
}
