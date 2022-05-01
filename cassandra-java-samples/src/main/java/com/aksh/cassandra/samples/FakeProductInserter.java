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

    private static final String PRODUCT_INSERT_CQL="INSERT INTO pocdb1.products(id,name,description,weight)" +
            " VALUES (?,?,?,?);";
    private static final String PRDUCT_IN_HAND_INSERT="INSERT INTO pocdb1.products_on_hand(product_id, quantity) VALUES (?,?);";
    public void insertProduct(Session session) throws Exception{
        if(map.get(PRODUCT_INSERT_CQL) ==null){
            map.put(PRODUCT_INSERT_CQL,session.prepare(PRODUCT_INSERT_CQL));
        }

        if(map.get(PRDUCT_IN_HAND_INSERT) ==null){
            map.put(PRDUCT_IN_HAND_INSERT,session.prepare(PRDUCT_IN_HAND_INSERT));
        }

        String name=faker.name().name();
        long id=faker.number().numberBetween(1l,10000l);
        boolean success=prodIdQueue.offer(id,5, TimeUnit.SECONDS);
        if(success){
            PreparedStatement preparedStatement=map.get(PRODUCT_INSERT_CQL);
            session.execute(preparedStatement.bind(id,name,faker.regexify("[a-z1-9]{10} "+name),Float.parseFloat(faker.numerify("##"))));

            PreparedStatement preparedStatement2=map.get(PRDUCT_IN_HAND_INSERT);
            session.execute(preparedStatement2.bind(id,faker.number().numberBetween(1l,10000l)));
        }

    }
    private static final String PRODUCT_UPDATE_CQL="UPDATE  pocdb1.products SET description =?" +
            " WHERE id =?;";
    private static final String PRDUCT_IN_HAND_UPDATE="UPDATE  pocdb1.products_on_hand SET quantity =?" +
            " WHERE product_id =?;";

    public void updateProduct(Session session ) throws InterruptedException{
        if(map.get(PRODUCT_UPDATE_CQL) ==null){
            map.put(PRODUCT_UPDATE_CQL,session.prepare(PRODUCT_UPDATE_CQL));
        }

        if(map.get(PRDUCT_IN_HAND_UPDATE) ==null){
            map.put(PRDUCT_IN_HAND_UPDATE,session.prepare(PRDUCT_IN_HAND_UPDATE));
        }
        Long id=prodIdQueue.poll(5,TimeUnit.SECONDS);

        if(id!=null){

            session.execute(map.get(PRODUCT_UPDATE_CQL).bind(faker.regexify("[a-z1-9]{10} "),id));
            session.execute(map.get(PRDUCT_IN_HAND_UPDATE).bind(faker.number().numberBetween(1l,10000l),id));
        }
    }

    private static final String CUSTOMER_INSERT_CQL="INSERT INTO pocdb1.customers(id,first_name,last_name,email,insertdate)\n" +
            "  VALUES (?,?,?,?,?);";


    public void insertCustomer(Session session) throws  InterruptedException{
        if(map.get(CUSTOMER_INSERT_CQL) ==null){
            map.put(CUSTOMER_INSERT_CQL,session.prepare(CUSTOMER_INSERT_CQL));
        }
        PreparedStatement preparedStatement=map.get(CUSTOMER_INSERT_CQL);
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

    private static final String CUSTOMER_UPDATE_CQL="UPDATE  pocdb1.customers set email =? " +
            "  WHERE id=?;";

    public void updateCustomer(Session session ) throws  InterruptedException{
        if(map.get(CUSTOMER_UPDATE_CQL) ==null){
            map.put(CUSTOMER_UPDATE_CQL,session.prepare(CUSTOMER_UPDATE_CQL));
        }


        Long id=custIdQueue.poll(5, TimeUnit.SECONDS);
        if(id!=null){
            String email=faker.bothify("????##@gmail.updated");
            System.out.println("id="+id+",email="+email);
            session.execute(map.get(CUSTOMER_UPDATE_CQL).bind(email,id));
        }

    }
}
