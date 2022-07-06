package com.cassandra.samples;

import com.datastax.oss.driver.api.core.CqlSession;

import com.datastax.oss.driver.api.core.cql.PreparedStatement;
import com.github.javafaker.Faker;
import com.github.javafaker.Name;
import org.apache.commons.lang.StringUtils;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class FakeProductInserter {
    Faker faker=new Faker();
    private String keyspace;
    private Map<String, PreparedStatement> map=new HashMap<>();
    private BlockingQueue<Long> prodIdQueue=new LinkedBlockingDeque<>();
    private BlockingQueue<Long> custIdQueue=new LinkedBlockingDeque<>();
    PrintStream printId=null;
    private static final org.slf4j.Logger log= LoggerFactory.getLogger(FakeProductInserter.class);



    public FakeProductInserter(String _keyspace) throws FileNotFoundException {
        this.keyspace=_keyspace;
        printId=new PrintStream(new FileOutputStream("id.txt"));
    }

    private static final String PRODUCT_INSERT_CQL="INSERT INTO #KEYSPACE#.#TABLE#(id,name,description,weight)" +
            " VALUES (?,?,?,?);";
    private static final String PRDUCT_IN_HAND_INSERT="INSERT INTO #KEYSPACE#.#TABLE#(product_id, quantity) VALUES (?,?);";
    public void insertProduct(CqlSession session) throws Exception{
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
        System.out.println("Inserting Product id:"+id);
        printId.println(id);

    }
    private static final String PRODUCT_UPDATE_CQL="UPDATE  #KEYSPACE#.#TABLE# SET description =?" +
            " WHERE id =?;";
    private static final String PRDUCT_IN_HAND_UPDATE="UPDATE  #KEYSPACE#.#TABLE# SET quantity =?" +
            " WHERE product_id =?;";

    public void updateProduct(CqlSession session) throws InterruptedException{
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
            System.out.println("Updating Product id:"+id);
        }
    }

    private static final String CUSTOMER_INSERT_CQL="INSERT INTO #KEYSPACE#.#TABLE#(id,first_name,last_name,email,insertdate)\n" +
            "  VALUES (?,?,?,?,?);";


    public void insertCustomer(CqlSession session) throws  InterruptedException{
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
        System.out.println("Inserting Customer id:"+id);
        printId.println(id);


    }

    private static final String CUSTOMER_UPDATE_CQL="UPDATE  #KEYSPACE#.#TABLE# set email =? " +
            "  WHERE id=?;";

    public void updateCustomer(CqlSession session ) throws  InterruptedException{
        String query=CUSTOMER_UPDATE_CQL.replaceAll("#KEYSPACE#",keyspace).replaceAll("#TABLE#","customers");
        if(map.get(query) ==null){
            map.put(query,session.prepare(query));
        }


        Long id=custIdQueue.poll(5, TimeUnit.SECONDS);
        if(id!=null){
            String email=faker.bothify("????##@gmail.updated");
            System.out.println("Updating Customer id="+id+",email="+email);
            session.execute(map.get(query).bind(email,id));
        }

    }


    public static void main(String[] args) throws Exception {
        final String configPath=args[0];
        final String keyspace=args[1];
        CassandraConnector client = new CassandraConnector(keyspace,configPath);
        String schemaCreateScript=null;
        if(args.length > 2){
            schemaCreateScript=args[2];
            client.executeScript(schemaCreateScript);
        }

        FakeProductInserter inserter=new FakeProductInserter(keyspace);

        CqlSession session = client.getSession();
        Random random=new Random();
        System.out.println(System.getProperty("delay"));
        String delay=Optional.ofNullable(System.getProperty("delay"))
                .filter(s->!StringUtils.isEmpty(s))
                .orElse("10");
        String totalIterations=Optional.ofNullable(System.getProperty("count"))
                .filter(s->!StringUtils.isEmpty(s))
                .orElse("1000");
        System.out.println("Total Iterations "+totalIterations);
        IntStream.range(1,Integer.valueOf(totalIterations)).parallel().forEach(i->{
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
                sleep(Long.valueOf(delay));

            }catch (Exception e){
                e.printStackTrace();
            }

        });
        System.out.println("All Records inserted");
        System.exit(0);
    }

    private static void sleep(long duration){
        try {
            System.out.println("Waiting for "+duration);
            Thread.sleep(duration);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
