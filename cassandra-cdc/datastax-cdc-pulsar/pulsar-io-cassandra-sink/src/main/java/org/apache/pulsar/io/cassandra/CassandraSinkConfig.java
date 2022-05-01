/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.apache.pulsar.io.cassandra;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.Map;

import org.apache.pulsar.io.core.annotations.FieldDoc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

import lombok.Data;
import lombok.experimental.Accessors;

@Data
@Accessors(chain = true)
public class CassandraSinkConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "A comma-separated list of cassandra hosts to connect to")
    private String roots;
    
    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "The key space used for writing pulsar messages to")
    private String keyspace;
    
    @FieldDoc(
        required = true,
        defaultValue = "",
        help = "The cassandra table name")
    private String tableName;
    
    @FieldDoc(
            required = false,
            defaultValue = "",
            help = "The cassandra DC Name")
    private String dcName;
    
    @FieldDoc(
            required = false,
            defaultValue = "",
            help = "Keyspace DB region")
    private String region;
    
    @FieldDoc(
            required = false,
            defaultValue = "false",
            help = "If keyspace DB")
    private boolean keyspacedb;
    
    @FieldDoc(
            required = false,
            defaultValue = "",
            help = "Cassandra Config file full path")
    private String configFilePath;
    

    @FieldDoc(
            required = false,
            defaultValue = "IGNORE",
            help = "How to handle records with null values, possible options are IGNORE, DELETE or FAIL. Default is IGNORE the message."
    )
    private NullValueAction nullValueAction = NullValueAction.IGNORE;

    public enum NullValueAction {
        IGNORE,
        DELETE,
        FAIL
    }

    @FieldDoc(
            required = false,
            defaultValue = "false",
            help = "Enable the elasticsearch bulk processor to flush write requests based on the number or size of requests, or after a given period."
    )
    private boolean bulkEnabled = false;

    @FieldDoc(
            required = false,
            defaultValue = "FAIL",
            help = "How to handle elasticsearch rejected documents due to some malformation. Possible options are IGNORE, DELETE or FAIL. Default is FAIL the Elasticsearch document."
    )
    private MalformedDocAction malformedDocAction = MalformedDocAction.FAIL;

    public enum MalformedDocAction {
        IGNORE,
        WARN,
        FAIL
    }

    public static CassandraSinkConfig load(String yamlFile) throws IOException {
        ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
        return mapper.readValue(new File(yamlFile), CassandraSinkConfig.class);
    }

    public static CassandraSinkConfig load(Map<String, Object> map) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readValue(new ObjectMapper().writeValueAsString(map), CassandraSinkConfig.class);
    }
}