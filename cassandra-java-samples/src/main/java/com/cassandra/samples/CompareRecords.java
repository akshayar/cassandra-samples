package com.cassandra.samples;

import com.google.gson.Gson;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.StringUtils;
import org.junit.Assert;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;

public class CompareRecords {
    private static final org.slf4j.Logger log= LoggerFactory.getLogger(CompareRecords.class);
    /**
     * -sconf src/main/resources/source.conf -skey pocdb1 -stab customers -tconf src/main/resources/target.conf -tkey pocdb1 -ttab customers -id 1
     * @param args
     */

    public static void main(String[] args) {
        log.info(Arrays.asList(args)+"");
        // create Options object
        Options options = new Options();
        options.addOption("sconf",true,"Source Cassandra/keyspaces config")
                .addOption("skey",true,"Source keyspace")
                .addOption("stab",true,"Source table")
                .addOption("tconf",true,"Target Cassandra/keyspaces config")
                .addOption("tkey",true,"Target keyspace")
                .addOption("ttab",true,"Target table")
                .addOption("id",true,"Record Id")
                .addOption("idfile",true,"Record Id");

        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine cmd = parser.parse(options, args);
            GetDataFromCassandra source=new GetDataFromCassandra(cmd.getOptionValue("sconf"),
                    cmd.getOptionValue("skey"),
                    cmd.getOptionValue("stab"));
            GetDataFromCassandra target=new GetDataFromCassandra(cmd.getOptionValue("tconf"),
                    cmd.getOptionValue("tkey"),
                    cmd.getOptionValue("ttab"));
            if(Optional.ofNullable(cmd.getOptionValue("idfile")).filter(s-> !StringUtils.isEmpty(s)).isPresent()){
                BufferedReader file=new BufferedReader(new FileReader(cmd.getOptionValue("idfile")));
                String strCurrentLine;
                while ((strCurrentLine = file.readLine()) != null) {
                    compareIds(strCurrentLine, source, target);
                }
                System.exit(0);

            }else{
                String id= cmd.getOptionValue("id");
                compareIds(id, source, target);
                System.exit(0);
            }

        } catch (Throwable e) {
            e.printStackTrace();
            System.exit(1);
        }

    }

    private static void compareIds(String id, GetDataFromCassandra source, GetDataFromCassandra target) {
        try{
            String sourceJson= source.executeForId(id);
            String targetJson= target.executeForId(id);
            Gson gson=new Gson();
            Map sourceJsonMap=gson.fromJson(sourceJson, Map.class);
            Map targetJsonMap=gson.fromJson(targetJson, Map.class);
            Assert.assertEquals(sourceJsonMap,targetJsonMap);
            log.info("Records match for id "+id);
        }catch (Throwable t){
            log.error(t.getMessage());
        }
    }
}
